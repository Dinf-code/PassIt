package com.dinachi.passit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dinachi.passit.storage.RepositoryProvider
import com.dinachi.passit.storage.remote.FirebaseSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel - Handles authentication logic for Onboarding and Login/SignUp screens
 */
class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepo = RepositoryProvider.provideAuthRepo()

    // UI State
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Navigation Events
    private val _navigationEvent = MutableStateFlow<AuthNavigationEvent?>(null)
    val navigationEvent: StateFlow<AuthNavigationEvent?> = _navigationEvent.asStateFlow()

    /**
     * Update email field
     */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null // Clear error when user types
        )
    }

    /**
     * Update password field
     */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    /**
     * Update confirm password (for signup)
     */
    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    /**
     * Update name field (for signup)
     */
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }

    /**
     * Toggle password visibility
     */
    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    /**
     * Toggle confirm password visibility
     */
    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible
        )
    }

    /**
     * Switch between Login and SignUp tabs
     */
    fun setAuthMode(isLogin: Boolean) {
        _uiState.value = _uiState.value.copy(
            isLoginMode = isLogin,
            errorMessage = null
        )
    }

    /**
     * Handle Login
     */
    fun login() {
        if (!validateLoginInputs()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Actually call Firebase Auth
                authRepo.signInWithEmail(_uiState.value.email, _uiState.value.password)

                // 🔥 SEED DATA (run once, then remove this line)
                FirebaseSeeder.seedFirebaseData()

                // On success, navigate to home
                _navigationEvent.value = AuthNavigationEvent.NavigateToHome
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Login failed. Please try again."
                )
            }
        }
    }

    /**
     * Handle Sign Up
     */
    fun signUp() {
        if (!validateSignUpInputs()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Actually call Firebase Auth
                authRepo.signUpWithEmail(
                    email = _uiState.value.email,
                    password = _uiState.value.password,
                    name = _uiState.value.name
                )

                // On success, navigate to home
                _navigationEvent.value = AuthNavigationEvent.NavigateToHome
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Sign up failed. Please try again."
                )
            }
        }
    }
    /**
     * Handle Google Sign In
     */
    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // TODO: Call AuthRepo.signInWithGoogle()
                kotlinx.coroutines.delay(1000)

                _navigationEvent.value = AuthNavigationEvent.NavigateToHome
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in failed. Please try again."
                )
            }
        }
    }

    /**
     * Handle Apple Sign In
     */
    fun signInWithApple() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // TODO: Call AuthRepo.signInWithApple()
                kotlinx.coroutines.delay(1000)

                _navigationEvent.value = AuthNavigationEvent.NavigateToHome
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Apple sign-in failed. Please try again."
                )
            }
        }
    }

    /**
     * Handle Forgot Password
     */
    fun forgotPassword() {
        if (_uiState.value.email.isBlank()) {
            _uiState.value = _uiState.value.copy(
                emailError = "Please enter your email first"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // TODO: Call AuthRepo.sendPasswordResetEmail(email)
                kotlinx.coroutines.delay(1000)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Password reset email sent! Check your inbox."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to send reset email. Please try again."
                )
            }
        }
    }

    /**
     * Handle Skip Onboarding
     */
    fun skipOnboarding() {
        _navigationEvent.value = AuthNavigationEvent.NavigateToWelcome
    }

    /**
     * Handle Get Started from Onboarding
     */
    fun getStarted() {
        _navigationEvent.value = AuthNavigationEvent.NavigateToWelcome
    }

    /**
     * Handle Login from Onboarding
     */
    fun navigateToLogin() {
        _navigationEvent.value = AuthNavigationEvent.NavigateToWelcome
    }

    /**
     * Clear navigation event after handling
     */
    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // ==================== PRIVATE VALIDATION METHODS ====================

    private fun validateLoginInputs(): Boolean {
        val email = _uiState.value.email
        val password = _uiState.value.password

        var hasError = false

        // Validate email
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            hasError = true
        } else if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            hasError = true
        }

        // Validate password
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            hasError = true
        } else if (password.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters")
            hasError = true
        }

        return !hasError
    }

    private fun validateSignUpInputs(): Boolean {
        val name = _uiState.value.name
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        var hasError = false

        // Validate name
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            hasError = true
        }

        // Validate email
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            hasError = true
        } else if (!isValidEmail(email)) {
            _uiState.value = _uiState.value.copy(emailError = "Invalid email format")
            hasError = true
        }

        // Validate password
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            hasError = true
        } else if (password.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters")
            hasError = true
        }

        // Validate confirm password
        if (confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Please confirm your password")
            hasError = true
        } else if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Passwords do not match")
            hasError = true
        }

        return !hasError
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * UI State for Auth screens
 */
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoginMode: Boolean = true, // true = login, false = signup
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nameError: String? = null
)

/**
 * Navigation Events
 */
sealed class AuthNavigationEvent {
    object NavigateToHome : AuthNavigationEvent()
    object NavigateToWelcome : AuthNavigationEvent()
    object NavigateToSignUp : AuthNavigationEvent()
}