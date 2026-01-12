package com.dinachi.passit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinachi.passit.datamodel.ChatMessage
import com.dinachi.passit.datamodel.Listing
import com.dinachi.passit.datamodel.User
import com.dinachi.passit.storage.RepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatRepo = RepositoryProvider.provideChatRepo()
    private val listingRepo = RepositoryProvider.provideListingRepo()
    private val userRepo = RepositoryProvider.provideUserRepo()

    // ==================== CHAT ROOM (for ChatScreen) ====================
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messageInput = MutableStateFlow("")
    val messageInput: StateFlow<String> = _messageInput.asStateFlow()

    private var messagesJob: Job? = null

    fun initializeChat(
        chatRoomId: String,
        listingId: String,
        otherUserId: String,
        currentUserId: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val listing = listingRepo.getListing(listingId)
                val otherUser = userRepo.getUser(otherUserId)

                _uiState.update {
                    it.copy(
                        chatRoomId = chatRoomId,
                        listing = listing,
                        otherUser = otherUser,
                        currentUserId = currentUserId,
                        isLoading = false
                    )
                }

                observeMessages(chatRoomId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load chat"
                    )
                }
            }
        }
    }

    fun observeMessages(chatRoomId: String) {
        if (chatRoomId.isBlank()) return

        // Avoid duplicate collectors
        if (_uiState.value.chatRoomId == chatRoomId && messagesJob?.isActive == true) return

        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            chatRepo.observeMessages(chatRoomId)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load messages") }
                }
                .collectLatest { messages ->
                    val sorted = messages.sortedBy { it.timestamp }
                    _uiState.update { it.copy(isLoading = false, messages = sorted, error = null) }
                }
        }
    }

    fun onMessageInputChange(text: String) {
        _messageInput.value = text
    }

    fun sendMessage() {
        val text = _messageInput.value.trim()
        if (text.isEmpty()) return

        val chatRoomId = _uiState.value.chatRoomId ?: return
        val senderId = _uiState.value.currentUserId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, error = null) }

            runCatching {
                val message = ChatMessage(
                    id = "",
                    chatRoomId = chatRoomId,
                    senderId = senderId,
                    messageText = text,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    imageUrl = null
                )
                chatRepo.sendMessage(message)
            }
                .onSuccess {
                    _messageInput.value = ""
                    _uiState.update { it.copy(isSending = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSending = false, error = e.message ?: "Failed to send message") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ==================== CHAT LIST (for ChatListScreen) ====================
    private val _listUiState = MutableStateFlow(ChatListUiState())
    val listUiState: StateFlow<ChatListUiState> = _listUiState.asStateFlow()

    private var listJob: Job? = null

    /**
     * Start observing all chat threads for the current user
     * Call this in ChatListScreen's LaunchedEffect
     */
    fun startChatList(currentUserId: String) {
        if (currentUserId.isBlank()) return

        if (_listUiState.value.currentUserId == currentUserId && listJob?.isActive == true) return

        _listUiState.update { it.copy(isLoading = true, error = null, currentUserId = currentUserId) }

        listJob?.cancel()
        listJob = viewModelScope.launch {
            chatRepo.observeChatThreads(currentUserId)
                .catch { e ->
                    _listUiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load chats")
                    }
                }
                .collectLatest { threads ->
                    // Convert repo threads -> UI-friendly items
                    val items = threads
                        .sortedByDescending { it.lastMessageTimestamp ?: 0L }
                        .map { t ->
                            ChatListItemUi(
                                chatRoomId = t.chatRoomId,
                                userName = t.otherUserName ?: "Unknown",
                                userPhoto = t.otherUserPhotoUrl ?: "",
                                lastMessage = t.lastMessageText ?: "",
                                timestamp = formatRelativeTime(t.lastMessageTimestamp ?: 0L),
                                unreadCount = t.unreadCount ?: 0,
                                itemTitle = t.listingTitle ?: "Item"
                            )
                        }

                    _listUiState.update { it.copy(isLoading = false, items = items, error = null) }
                }
        }
    }

    fun clearListError() {
        _listUiState.update { it.copy(error = null) }
    }

    // ==================== HELPERS ====================
    private fun formatRelativeTime(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val now = System.currentTimeMillis()
        val diff = (now - timestamp).coerceAtLeast(0L)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> "${days}d ago"
        }
    }
}

// ==================== UI STATES ====================

/**
 * Chat room UI state (for ChatScreen)
 */
data class ChatUiState(
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null,
    val chatRoomId: String? = null,
    val listing: Listing? = null,
    val otherUser: User? = null,
    val currentUserId: String? = null,
    val messages: List<ChatMessage> = emptyList()
)

/**
 * Chat list UI state (for ChatListScreen)
 */
data class ChatListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String? = null,
    val items: List<ChatListItemUi> = emptyList()
)

/**
 * UI model for chat list items
 */
data class ChatListItemUi(
    val chatRoomId: String,
    val userName: String,
    val userPhoto: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int,
    val itemTitle: String
)

/**
 * Chat thread data from repository
 */
data class ChatThread(
    val chatRoomId: String,
    val listingId: String? = null,
    val listingTitle: String? = null,
    val otherUserId: String? = null,
    val otherUserName: String? = null,
    val otherUserPhotoUrl: String? = null,
    val lastMessageText: String? = null,
    val lastMessageTimestamp: Long? = null,
    val unreadCount: Int? = 0
)