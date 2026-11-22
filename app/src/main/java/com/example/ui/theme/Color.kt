package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Official Mono Monochromatic Palette
val MonoBgMain = Color(0xFF080808)
val MonoBgSecondary = Color(0xFF111111)
val MonoCardPrimary = Color(0xFF181818)
val MonoCardPrimaryTop = Color(0xFF1E1E1E)
val MonoCardPrimaryBottom = Color(0xFF141414)
val MonoCardElevated = Color(0xFF222222)
val MonoSurfaceSecondary = Color(0xFF292929)
val MonoBorder = Color(0xFF2E2E2E)
val MonoDivider = Color(0xFF232323)

val MonoTextPrimary = Color(0xFFFFFFFF)
val MonoTextSecondary = Color(0xFFA5A5A5)
val MonoTextMuted = Color(0xFF707070)
val MonoAccentPrimary = Color(0xFFE8E8E8)

// Light Mono Optional Palette
val MonoLightBgMain = Color(0xFFF3F3F3)
val MonoLightBgSecondary = Color(0xFFEAEAEA)
val MonoLightCardPrimary = Color(0xFFFFFFFF)
val MonoLightCardTop = Color(0xFFFFFFFF)
val MonoLightCardBottom = Color(0xFFF7F7F7)
val MonoLightCardElevated = Color(0xFFEFEFEF)
val MonoLightSurfaceSecondary = Color(0xFFE2E2E2)
val MonoLightBorder = Color(0xFFD5D5D5)
val MonoLightDivider = Color(0xFFE6E6E6)
val MonoLightTextPrimary = Color(0xFF0C0C0C)
val MonoLightTextSecondary = Color(0xFF555555)
val MonoLightTextMuted = Color(0xFF888888)
val MonoLightAccentPrimary = Color(0xFF181818)

// Strictly Scoped Functional Accents (Scoreboards, Cube, Status)
val TeamRedAccent = Color(0xFFDC4444)
val TeamRedSurface = Color(0xFF271314)
val TeamRedBorder = Color(0xFF5A2525)

val TeamBlueAccent = Color(0xFF3B82F6)
val TeamBlueSurface = Color(0xFF111D2E)
val TeamBlueBorder = Color(0xFF24436E)

val StatusSuccess = Color(0xFF22C55E)
val StatusWarning = Color(0xFFF59E0B)
val StatusError = Color(0xFFEF4444)

@Immutable
data class MonoPalette(
    val isDark: Boolean,
    val bgMain: Color,
    val bgSecondary: Color,
    val cardTop: Color,
    val cardBottom: Color,
    val cardPrimary: Color,
    val cardElevated: Color,
    val surfaceSecondary: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentPrimary: Color,
    val wavePrimary: Color,
    val waveSecondary: Color,
    val navBarBg: Color,
)

val DarkMonoPalette = MonoPalette(
    isDark = true,
    bgMain = MonoBgMain,
    bgSecondary = MonoBgSecondary,
    cardTop = MonoCardPrimaryTop,
    cardBottom = MonoCardPrimaryBottom,
    cardPrimary = MonoCardPrimary,
    cardElevated = MonoCardElevated,
    surfaceSecondary = MonoSurfaceSecondary,
    border = MonoBorder,
    divider = MonoDivider,
    textPrimary = MonoTextPrimary,
    textSecondary = MonoTextSecondary,
    textMuted = MonoTextMuted,
    accentPrimary = MonoAccentPrimary,
    wavePrimary = Color(0xFF141414),
    waveSecondary = Color(0xFF1A1A1A),
    navBarBg = Color(0xFF121212),
)

val LightMonoPalette = MonoPalette(
    isDark = false,
    bgMain = MonoLightBgMain,
    bgSecondary = MonoLightBgSecondary,
    cardTop = MonoLightCardTop,
    cardBottom = MonoLightCardBottom,
    cardPrimary = MonoLightCardPrimary,
    cardElevated = MonoLightCardElevated,
    surfaceSecondary = MonoLightSurfaceSecondary,
    border = MonoLightBorder,
    divider = MonoLightDivider,
    textPrimary = MonoLightTextPrimary,
    textSecondary = MonoLightTextSecondary,
    textMuted = MonoLightTextMuted,
    accentPrimary = MonoLightAccentPrimary,
    wavePrimary = Color(0xFFE7E7E7),
    waveSecondary = Color(0xFFDFDFDF),
    navBarBg = Color(0xFFFFFFFF),
)

val LocalMonoPalette = staticCompositionLocalOf { DarkMonoPalette }
