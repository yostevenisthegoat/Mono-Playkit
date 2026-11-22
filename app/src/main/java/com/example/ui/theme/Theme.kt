package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class ThemeMode {
    DARK,
    LIGHT,
    SYSTEM,
}

private val MonoDarkColorScheme = darkColorScheme(
    primary = MonoAccentPrimary,
    onPrimary = Color(0xFF0C0C0C),
    primaryContainer = MonoCardElevated,
    onPrimaryContainer = MonoTextPrimary,
    secondary = MonoTextSecondary,
    onSecondary = Color(0xFF101010),
    secondaryContainer = MonoSurfaceSecondary,
    onSecondaryContainer = MonoTextPrimary,
    tertiary = MonoTextMuted,
    onTertiary = MonoTextPrimary,
    background = MonoBgMain,
    onBackground = MonoTextPrimary,
    surface = MonoCardPrimary,
    onSurface = MonoTextPrimary,
    surfaceVariant = MonoCardElevated,
    onSurfaceVariant = MonoTextSecondary,
    outline = MonoBorder,
    outlineVariant = MonoDivider,
    error = StatusError,
    onError = Color.White,
)

private val MonoLightColorScheme = lightColorScheme(
    primary = MonoLightAccentPrimary,
    onPrimary = Color.White,
    primaryContainer = MonoLightCardElevated,
    onPrimaryContainer = MonoLightTextPrimary,
    secondary = MonoLightTextSecondary,
    onSecondary = Color.White,
    secondaryContainer = MonoLightSurfaceSecondary,
    onSecondaryContainer = MonoLightTextPrimary,
    tertiary = MonoLightTextMuted,
    onTertiary = Color.White,
    background = MonoLightBgMain,
    onBackground = MonoLightTextPrimary,
    surface = MonoLightCardPrimary,
    onSurface = MonoLightTextPrimary,
    surfaceVariant = MonoLightCardElevated,
    onSurfaceVariant = MonoLightTextSecondary,
    outline = MonoLightBorder,
    outlineVariant = MonoLightDivider,
    error = StatusError,
    onError = Color.White,
)

val MonoShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

object MonoTheme {
    val palette: MonoPalette
        @Composable get() = LocalMonoPalette.current
}

@Composable
fun MonoPlayKitTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val palette = if (isDark) DarkMonoPalette else LightMonoPalette
    val colorScheme = if (isDark) MonoDarkColorScheme else MonoLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalMonoPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MonoTypography,
            shapes = MonoShapes,
            content = content,
        )
    }
}
