package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette - Professional Polish
val PolishPrimary = Color(0xFF0061A4)
val PolishOnPrimary = Color(0xFFFFFFFF)
val PolishPrimaryContainer = Color(0xFFD1E4FF)
val PolishOnPrimaryContainer = Color(0xFF001D36)

val PolishSecondary = Color(0xFF535F70)
val PolishOnSecondary = Color(0xFFFFFFFF)
val PolishSecondaryContainer = Color(0xFFD7E3F7)
val PolishOnSecondaryContainer = Color(0xFF101C2B)

val PolishTertiary = Color(0xFF6B5778)
val PolishOnTertiary = Color(0xFFFFFFFF)
val PolishTertiaryContainer = Color(0xFFF2DAFF)
val PolishOnTertiaryContainer = Color(0xFF251431)

val LightBackground = Color(0xFFFDFBFF)
val LightOnBackground = Color(0xFF1A1C1E)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A1C1E)
val LightSurfaceVariant = Color(0xFFF0F4FA)
val LightOnSurfaceVariant = Color(0xFF44474E)
val LightOutline = Color(0xFF74777F)
val LightOutlineVariant = Color(0xFFDDE2EA)

// Dark Theme Palette - Professional Polish Dark
val PolishDarkPrimary = Color(0xFF9ECAFF)
val PolishDarkOnPrimary = Color(0xFF003258)
val PolishDarkPrimaryContainer = Color(0xFF00497D)
val PolishDarkOnPrimaryContainer = Color(0xFFD1E4FF)

val PolishDarkSecondary = Color(0xFFBBC7DB)
val PolishDarkOnSecondary = Color(0xFF253140)
val PolishDarkSecondaryContainer = Color(0xFF3B4858)
val PolishDarkOnSecondaryContainer = Color(0xFFD7E3F7)

val PolishDarkTertiary = Color(0xFFD6BEE4)
val PolishDarkOnTertiary = Color(0xFF3B2947)
val PolishDarkTertiaryContainer = Color(0xFF523F5F)
val PolishDarkOnTertiaryContainer = Color(0xFFF2DAFF)

val DarkBackground = Color(0xFF111418)
val DarkOnBackground = Color(0xFFE2E2E6)
val DarkSurface = Color(0xFF191C20)
val DarkOnSurface = Color(0xFFE2E2E6)
val DarkSurfaceVariant = Color(0xFF242A32)
val DarkOnSurfaceVariant = Color(0xFFC4C6D0)
val DarkOutline = Color(0xFF8E9099)
val DarkOutlineVariant = Color(0xFF3B424C)

data class ColorPreset(val hex: Long, val name: String) {
    val composeColor: Color get() = Color(hex)
}

// Color Presets for QR Customization
val QrColorPresets = listOf(
    ColorPreset(0xFF000000L, "Classic Black"),
    ColorPreset(0xFF0061A4L, "Sapphire Blue"),
    ColorPreset(0xFF001E2FL, "Deep Navy"),
    ColorPreset(0xFF006874L, "Teal"),
    ColorPreset(0xFF196E3DL, "Emerald Forest"),
    ColorPreset(0xFFBA1A1AL, "Crimson Red"),
    ColorPreset(0xFF6750A4L, "Royal Purple"),
    ColorPreset(0xFF8B5000L, "Warm Bronze"),
    ColorPreset(0xFF44474EL, "Slate Gray")
)

val QrBgPresets = listOf(
    ColorPreset(0xFFFFFFFFL, "Pure White"),
    ColorPreset(0xFFFDFBFFL, "Soft Ivory"),
    ColorPreset(0xFFF0F4FAL, "Ice Light"),
    ColorPreset(0xFFD1E4FFL, "Pale Sky"),
    ColorPreset(0xFFF4F3F7L, "Warm Tint"),
    ColorPreset(0xFF191C20L, "Dark Surface"),
    ColorPreset(0xFF000000L, "Pitch Black")
)
