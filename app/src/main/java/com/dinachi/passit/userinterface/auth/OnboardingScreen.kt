package com.dinachi.passit.userinterface.auth


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dinachi.passit.R
import com.dinachi.passit.ui.theme.GoldPrimary
import com.dinachi.passit.ui.theme.PassItTheme
import com.dinachi.passit.viewmodel.AuthNavigationEvent
import com.dinachi.passit.viewmodel.AuthViewModel

/**
 * Onboarding Screen - First screen users see
 */
@Composable
fun OnboardingScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToWelcome: () -> Unit = {},
    onNavigateToHome: () -> Unit = {}
) {
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    // Handle navigation events
    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is AuthNavigationEvent.NavigateToWelcome -> {
                onNavigateToWelcome()
                viewModel.clearNavigationEvent()
            }
            is AuthNavigationEvent.NavigateToHome -> {
                onNavigateToHome()
                viewModel.clearNavigationEvent()
            }
            else -> { /* Do nothing */ }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.onboarding_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay gradient (makes text readable)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar - JOIN badge and SKIP button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // JOIN badge (left)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Small avatar circle
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                        Text(
                            text = "JOIN 24.7K",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // SKIP button (right)
                TextButton(onClick = { viewModel.skipOnboarding() }) {
                    Text(
                        text = "SKIP",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center content - Logo and text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shopping bag icon in orange box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GoldPrimary,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "PassIt Logo",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }
                }

                // "PassIt" text
                Text(
                    text = "PassIt",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // "Turn Clutter into Cash" - with Cash in gold
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White)) {
                            append("Turn Clutter into ")
                        }
                        withStyle(style = SpanStyle(color = GoldPrimary)) {
                            append("Cash")
                        }
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Description text
                Text(
                    text = "Snap a photo, set a price, and sell your unused items to neighbors in seconds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Progress indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == 0) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == 0) GoldPrimary
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Get Started button with arrow
            Button(
                onClick = { viewModel.getStarted() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log In text button
            TextButton(onClick = { viewModel.navigateToLogin() }) {
                Text(
                    text = "Log In",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms and Privacy Policy text
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                        append("By continuing, you agree to our ")
                    }
                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                        append("Terms")
                    }
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                        append(" and ")
                    }
                    withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                        append("Privacy Policy")
                    }
                },
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    PassItTheme {
        OnboardingScreen()
    }
}