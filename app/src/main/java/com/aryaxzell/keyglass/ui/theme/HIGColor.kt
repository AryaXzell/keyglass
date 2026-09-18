package com.aryaxzell.keyglass.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class HIGColorScheme(
    val isDark: Boolean,
    val background: Color,
    val secondaryBackground: Color,
    val tertiaryBackground: Color,
    val groupedBackground: Color,
    val groupedCard: Color,
    val keyboardBackground: Color,
    val keyLetterBackground: Color,
    val keyDarkBackground: Color,
    val keyPressedBackground: Color,
    val keyShadow: Color,
    val primaryLabel: Color,
    val secondaryLabel: Color,
    val tertiaryLabel: Color,
    val separator: Color,
    val accent: Color,
    val success: Color = Color(0xFF34C759),
    val destructive: Color = Color(0xFFFF3B30)
)

val DarkHIGColorScheme = HIGColorScheme(
    isDark = true,
    background = Color(0xFF000000),
    secondaryBackground = Color(0xFF1C1C1E),
    tertiaryBackground = Color(0xFF2C2C2E),
    groupedBackground = Color(0xFF000000),
    groupedCard = Color(0xFF1C1C1E),
    keyboardBackground = Color(0xE61E1E22),
    keyLetterBackground = Color(0xFF484A4E),
    keyDarkBackground = Color(0xFF2E2F32),
    keyPressedBackground = Color(0xFF6B6C72),
    keyShadow = Color(0x66000000),
    primaryLabel = Color(0xFFFFFFFF),
    secondaryLabel = Color(0xFF8E8E93),
    tertiaryLabel = Color(0xFF48484A),
    separator = Color(0xFF38383A),
    accent = Color(0xFF007AFF)
)

val LightHIGColorScheme = HIGColorScheme(
    isDark = false,
    background = Color(0xFFF2F2F7),
    secondaryBackground = Color(0xFFFFFFFF),
    tertiaryBackground = Color(0xFFE5E5EA),
    groupedBackground = Color(0xFFF2F2F7),
    groupedCard = Color(0xFFFFFFFF),
    keyboardBackground = Color(0xE6D1D3D9),
    keyLetterBackground = Color(0xFFFFFFFF),
    keyDarkBackground = Color(0xFFB1B5BD),
    keyPressedBackground = Color(0xFFD6D7DC),
    keyShadow = Color(0x33000000),
    primaryLabel = Color(0xFF000000),
    secondaryLabel = Color(0xFF8E8E93),
    tertiaryLabel = Color(0xFFC7C7CC),
    separator = Color(0xFFC6C6C8),
    accent = Color(0xFF007AFF)
)

val ACCENT_PRESETS = listOf(
    "#007AFF" to "Blue",
    "#34C759" to "Green",
    "#AF52DE" to "Purple",
    "#FF9500" to "Orange",
    "#FF2D55" to "Pink",
    "#5AC8FA" to "Teal",
    "#FFCC00" to "Yellow",
    "#5856D6" to "Indigo"
)

fun parseHexColor(hex: String, fallback: Color = Color(0xFF007AFF)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(0xFF000000 or colorInt)
        } else if (cleanHex.length == 8) {
            Color(colorInt)
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}
