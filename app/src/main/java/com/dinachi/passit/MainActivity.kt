package com.dinachi.passit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dinachi.passit.AppWide.AppNavigation
import com.dinachi.passit.AppWide.MainScaffold
import com.dinachi.passit.ui.theme.PassItTheme
import com.google.firebase.auth.FirebaseAuth  // ADD THIS

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PassItTheme {
                val navController = rememberNavController()

                // ✅ GET REAL USER ID FROM FIREBASE AUTH
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val isUserLoggedIn = currentUserId.isNotEmpty()

                MainScaffold(
                    navController = navController,
                    currentUserId = currentUserId
                ) { controller ->
                    AppNavigation(
                        navController = controller,
                        isUserLoggedIn = isUserLoggedIn,  // ✅ FIXED
                        currentUserId = currentUserId
                    )
                }
            }
        }
    }
}
