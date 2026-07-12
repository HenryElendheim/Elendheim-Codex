package com.elendheim.codex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// The dark scheme is the real design. Soft red is the primary accent on near black.
private val DarkColors = darkColorScheme(
    primary = CodexRed,
    onPrimary = CodexText,
    secondary = CodexRedDim,
    background = CodexBlack,
    onBackground = CodexText,
    surface = CodexSurface,
    onSurface = CodexText,
    surfaceVariant = CodexSurfaceHigh,
    onSurfaceVariant = CodexTextMuted,
    outline = CodexOutline,
    error = CodexRed
)

// A plain light fallback so the app is still usable if the system forces light mode.
private val LightColors = lightColorScheme(
    primary = CodexRed,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightText,
    onSurface = LightText
)

@Composable
fun ElendheimCodexTheme(
    // Dark first: we default to the dark scheme unless the system is clearly light.
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dark is the intended experience, so treat anything that is not explicit light
    // as dark. In practice this keeps the app dark by default.
    val useDark = darkTheme || true
    val colors = if (useDark) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            // Match the system bars to the app background so the screen feels seamless.
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
