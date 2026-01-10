package com.dinachi.passit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dinachi.passit.AppWide.AppNavigation
import com.dinachi.passit.AppWide.MainScaffold
import com.dinachi.passit.ui.theme.PassItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PassItTheme {
                val navController = rememberNavController()

                // TODO: Get actual user ID from AuthViewModel after login
                val currentUserId = "temp_user_123" // Placeholder for now

                MainScaffold(
                    navController = navController,
                    currentUserId = currentUserId  // ← ADDED
                ) { controller ->
                    AppNavigation(
                        navController = controller,
                        isUserLoggedIn = false,
                        currentUserId = currentUserId  // ← ADDED
                    )
                }
            }
        }
    }

}


