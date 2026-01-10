package com.dinachi.passit.userinterface.chat


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dinachi.passit.datamodel.*
import com.dinachi.passit.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * ChatScreen - Displays conversation between buyer and seller
 * Now connected to ChatViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoomId: String,
    listingId: String,
    otherUserId: String,
    currentUserId: String,
    onBackPress: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()

    // Initialize chat when screen loads
    LaunchedEffect(chatRoomId) {
        viewModel.initializeChat(
            chatRoomId = chatRoomId,
            listingId = listingId,
            otherUserId = otherUserId,
            currentUserId = currentUserId
        )
    }

    // Show loading state
    if (uiState.isLoading && uiState.otherUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Show error state
    if (uiState.error != null && uiState.otherUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.error ?: "Failed to load chat",
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.initializeChat(chatRoomId, listingId, otherUserId, currentUserId)
                }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E),
        topBar = {
            ChatTopBar(
                user = uiState.otherUser,
                onBackPress = onBackPress,
                onMenuClick = { /* More options */ }
            )
        },
        bottomBar = {
            ChatInputBar(
                message = messageInput,
                onMessageChange = { viewModel.onMessageInputChange(it) },
                onSendMessage = { viewModel.sendMessage() },
                onAttachmentClick = { /* Handle attachment */ },
                isSending = uiState.isSending
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Listing Item Card
            if (uiState.listing != null) {
                item {
                    ListingItemCard(
                        listing = uiState.listing!!,
                        onMakeOfferClick = { /* Navigate to offer screen */ }
                    )
                }
            }

            // Safety Banner
            item {
                SafetyBanner()
            }

            // TODAY divider
            item {
                DateDivider(text = "TODAY")
            }

            // Messages
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    isFromMe = message.senderId == currentUserId,
                    senderAvatar = if (message.senderId == otherUserId) {
                        uiState.otherUser?.photoUrl ?: ""
                    } else {
                        ""
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Error Snackbar
        if (uiState.error != null && uiState.otherUser != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(uiState.error ?: "An error occurred")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    user: User?,
    onBackPress: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            if (user != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3A3A3C)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Name and Online Status
                    Column {
                        Text(
                            text = user.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )

                        if (user.isOnline) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF34C759))
                                )
                                Text(
                                    text = "Online",
                                    fontSize = 12.sp,
                                    color = Color(0xFF34C759)
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "Loading...",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF2C2416)
        )
    )
}

@Composable
fun ListingItemCard(
    listing: Listing,
    onMakeOfferClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2C2E))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image
            AsyncImage(
                model = listing.imageUrls.firstOrNull(),
                contentDescription = listing.title,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            // Title and Price
            Column {
                Text(
                    text = listing.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = listing.getDisplayPrice(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Make Offer Button
        Button(
            onClick = onMakeOfferClick,
            modifier = Modifier.height(36.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Text(
                text = "Make Offer",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SafetyBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2C2416))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = "Please keep conversations within PassIt to ensure safety. Never share financial info.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun DateDivider(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF2C2C2E)
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromMe: Boolean,
    senderAvatar: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromMe) {
            // Other user's avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C)),
                contentAlignment = Alignment.Center
            ) {
                if (senderAvatar.isNotEmpty()) {
                    AsyncImage(
                        model = senderAvatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // Message bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isFromMe) 16.dp else 4.dp,
                    topEnd = if (isFromMe) 4.dp else 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = if (isFromMe) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2E)
            ) {
                Text(
                    text = message.messageText,
                    fontSize = 15.sp,
                    color = if (isFromMe) Color.Black else Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Timestamp and read receipt
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.formatTimestamp(),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )

                if (isFromMe && message.isRead) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "Read",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (isFromMe) {
            Spacer(modifier = Modifier.width(40.dp))
        }
    }
}

@Composable
fun ChatInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachmentClick: () -> Unit,
    isSending: Boolean
) {
    Surface(
        color = Color(0xFF2C2416),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attachment button
            IconButton(
                onClick = onAttachmentClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3A3C))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Attach",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Message input field
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                placeholder = {
                    Text(
                        text = "Write a message...",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 15.sp
                    )
                },
                enabled = !isSending,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    disabledTextColor = Color.White.copy(alpha = 0.5f),
                    focusedContainerColor = Color(0xFF3A3A3C),
                    unfocusedContainerColor = Color(0xFF3A3A3C),
                    disabledContainerColor = Color(0xFF3A3A3C),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            // Send button
            IconButton(
                onClick = onSendMessage,
                enabled = message.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary
                        else Color(0xFF3A3A3C)
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (message.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ============ EXTENSION FUNCTIONS ============

/**
 * Format timestamp to readable time (e.g., "10:42 AM")
 */
fun ChatMessage.formatTimestamp(): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * Get display price with currency symbol
 */
fun Listing.getDisplayPrice(): String {
    return when (currency) {
        "CAD" -> "CA$${"%.2f".format(price)}"
        "USD" -> "$${"%.2f".format(price)}"
        "EUR" -> "€${"%.2f".format(price)}"
        else -> "$${"%.2f".format(price)}"
    }
}