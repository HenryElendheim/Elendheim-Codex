package com.elendheim.codex.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
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

// A higher contrast dark scheme for accessibility: pure white text, a brighter red
// and stronger hairlines, so everything stands out more against the near black.
private val HighContrastDarkColors = darkColorScheme(
    primary = Color(0xFFF05457),
    onPrimary = Color(0xFF000000),
    secondary = CodexRed,
    background = CodexBlack,
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1B1B20),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF26262E),
    onSurfaceVariant = Color(0xFFD8D8DE),
    outline = Color(0xFF5A5A66),
    error = Color(0xFFF05457)
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
    // Accessibility inputs. textScale multiplies every text size, highContrast swaps
    // in the stronger palette above.
    textScale: Float = 1f,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    // Dark is the intended experience, so treat anything that is not explicit light
    // as dark. In practice this keeps the app dark by default.
    val useDark = darkTheme || true
    val colors = when {
        !useDark -> LightColors
        highContrast -> HighContrastDarkColors
        else -> DarkColors
    }

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

    // Scale all text by adjusting the font scale on the density. This grows every sp
    // sized piece of text at once, which is exactly what a text size setting should do.
    val base = LocalDensity.current
    val scaledDensity = Density(density = base.density, fontScale = base.fontScale * textScale)

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}
