package com.splash.water.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Theme-aware background gradient so screens render correctly in both light and dark mode. */
@Immutable
data class AppGradient(val top: Color, val bottom: Color)

val LocalAppGradient = staticCompositionLocalOf { AppGradient(BgGradientTop, BgGradientBottom) }

private val LightColors = lightColorScheme(
    primary = AquaPrimary,
    onPrimary = Color.White,
    secondary = AquaSecondary,
    onSecondary = Color.White,
    tertiary = MintGreen,
    background = AquaSurface,
    onBackground = Color(0xFF0A2230),
    surface = Color.White,
    onSurface = Color(0xFF0A2230),
    surfaceVariant = Color(0xFFE2F3FB),
    primaryContainer = Color(0xFFCDEBFF),
    onPrimaryContainer = AquaDeep,
)

private val DarkColors = darkColorScheme(
    primary = DarkAqua,
    onPrimary = Color(0xFF00263B),
    secondary = DarkTeal,
    onSecondary = Color(0xFF00363B),
    tertiary = DarkMint,
    onTertiary = Color(0xFF053124),
    background = Color(0xFF0A1B33),
    onBackground = Color(0xFFEAF4FF),
    surface = Color(0xFF15294A),
    onSurface = Color(0xFFEAF4FF),
    surfaceVariant = Color(0xFF203A63),
    onSurfaceVariant = Color(0xFFB7C8E6),
    primaryContainer = Color(0xFF124A80),
    onPrimaryContainer = Color(0xFFCDEBFF),
    secondaryContainer = Color(0xFF134A57),
    onSecondaryContainer = Color(0xFFBEF3F6),
    outline = Color(0xFF40557E),
)

@Composable
fun SplashTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val gradient = if (darkTheme) {
        AppGradient(BgGradientTopDark, BgGradientBottomDark)
    } else {
        AppGradient(BgGradientTop, BgGradientBottom)
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    CompositionLocalProvider(LocalAppGradient provides gradient) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
