package com.dinachi.passit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme for PassIt
 * Primary theme - dark background with gold accent
 */
private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,              // Gold buttons, accents
    onPrimary = DarkBackground,         // Black text on gold
    primaryContainer = GoldSecondary,   // Lighter gold container
    onPrimaryContainer = DarkBackground,

    secondary = GoldSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = GoldTertiary,
    onSecondaryContainer = DarkBackground,

    tertiary = GoldTertiary,
    onTertiary = DarkBackground,

    background = DarkBackground,        // Main background
    onBackground = TextPrimary,         // White text

    surface = DarkSurface,              // Cards, sheets
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = Error,
    onError = TextPrimary,

    outline = TextTertiary,
    outlineVariant = DarkSurfaceVariant
)

/**
 * Light color scheme (optional - for users who prefer light mode)
 */
private val LightColorScheme = lightColorScheme(
    primary = GoldPrimary,
    onPrimary = DarkBackground,
    primaryContainer = GoldSecondary,
    onPrimaryContainer = DarkBackground,

    secondary = GoldSecondary,
    onSecondary = DarkBackground,

    background = LightBackground,
    onBackground = LightTextPrimary,

    surface = LightSurface,
    onSurface = LightTextPrimary,

    error = Error,
    onError = TextPrimary
)

/**
 * Main PassIt Theme
 * Wraps your entire app
 */
@Composable
fun PassItTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detect system theme
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to always use PassIt colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}