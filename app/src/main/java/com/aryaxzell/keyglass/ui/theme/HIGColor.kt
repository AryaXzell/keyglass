package com.aryaxzell.keyglass.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.aryaxzell.keyglass.data.datastore.KeyboardBackgroundType
import com.aryaxzell.keyglass.data.datastore.KeyboardPresetTheme
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.KeyToneStyle
import com.aryaxzell.keyglass.data.datastore.ThemeMode

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
    val keyBorder: Color = Color.Transparent,
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
    keyBorder = Color(0x33FFFFFF),
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
    keyBorder = Color(0x4DFFFFFF),
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

fun getKeyboardColorScheme(
    settings: KeyGlassSettings,
    systemDark: Boolean = true
): HIGColorScheme {
    val isDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val accent = parseHexColor(settings.accentColorHex)
    val baseScheme = if (isDark) DarkHIGColorScheme else LightHIGColorScheme

    val opacity = settings.keyOpacity.coerceIn(0.05f, 1.0f)

    return when (settings.keyToneStyle) {
        KeyToneStyle.GLASS_DARK -> {
            baseScheme.copy(
                accent = accent,
                keyLetterBackground = Color(0xFF3A3A3C).copy(alpha = (opacity * 0.85f).coerceIn(0.05f, 0.98f)),
                keyDarkBackground = Color(0xFF242426).copy(alpha = (opacity * 0.90f).coerceIn(0.08f, 0.98f)),
                keyPressedBackground = Color(0xFF636366).copy(alpha = 0.8f),
                keyBorder = if (settings.keyBorderEnabled) Color.White.copy(alpha = (opacity * 0.45f + 0.2f).coerceIn(0.25f, 0.8f)) else Color.Transparent,
                primaryLabel = Color.White,
                secondaryLabel = Color(0xFFB0B0B5)
            )
        }
        KeyToneStyle.GLASS_LIGHT -> {
            LightHIGColorScheme.copy(
                accent = accent,
                keyLetterBackground = Color.White.copy(alpha = (opacity * 0.85f).coerceIn(0.05f, 0.98f)),
                keyDarkBackground = Color(0xFFE5E7EB).copy(alpha = (opacity * 0.90f).coerceIn(0.08f, 0.98f)),
                keyPressedBackground = Color(0xFFD1D5DB).copy(alpha = 0.85f),
                keyBorder = if (settings.keyBorderEnabled) Color(0xFF9CA3AF).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.9f)) else Color.Transparent,
                primaryLabel = Color(0xFF111827),
                secondaryLabel = Color(0xFF6B7280)
            )
        }
        KeyToneStyle.ACCENT_TINT -> {
            val customCol = parseHexColor(settings.customKeyColorHex, fallback = accent)
            baseScheme.copy(
                accent = customCol,
                keyLetterBackground = customCol.copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                keyDarkBackground = customCol.copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.98f)),
                keyPressedBackground = Color.White.copy(alpha = 0.5f),
                keyBorder = if (settings.keyBorderEnabled) Color.White.copy(alpha = (opacity * 0.45f + 0.25f).coerceIn(0.25f, 0.85f)) else Color.Transparent,
                primaryLabel = Color.White,
                secondaryLabel = Color.White.copy(alpha = 0.8f)
            )
        }
        KeyToneStyle.AUTO_ADAPTIVE -> {
            if (settings.backgroundType == KeyboardBackgroundType.CUSTOM_IMAGE) {
                baseScheme.copy(
                    accent = accent,
                    keyLetterBackground = Color(0xFF2C2C2E).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                    keyDarkBackground = Color(0xFF1C1C1E).copy(alpha = (opacity * 0.85f).coerceIn(0.08f, 0.98f)),
                    keyPressedBackground = Color(0xFF636366).copy(alpha = 0.75f),
                    keyBorder = if (settings.keyBorderEnabled) Color.White.copy(alpha = (opacity * 0.45f + 0.2f).coerceIn(0.25f, 0.8f)) else Color.Transparent,
                    primaryLabel = Color.White,
                    secondaryLabel = Color(0xFFD1D5DB)
                )
            } else {
                when (settings.presetTheme) {
                    KeyboardPresetTheme.GLASS_DARK -> {
                        baseScheme.copy(
                            accent = accent,
                            keyLetterBackground = Color(0xFF484A4E).copy(alpha = (opacity * 0.85f).coerceIn(0.05f, 0.98f)),
                            keyDarkBackground = Color(0xFF2E2F32).copy(alpha = (opacity * 0.90f).coerceIn(0.08f, 0.98f)),
                            keyPressedBackground = Color(0xFF6B6C72).copy(alpha = 0.85f),
                            keyBorder = if (settings.keyBorderEnabled) Color.White.copy(alpha = (opacity * 0.45f + 0.2f).coerceIn(0.25f, 0.8f)) else Color.Transparent,
                            primaryLabel = Color.White,
                            secondaryLabel = Color(0xFF8E8E93)
                        )
                    }
                    KeyboardPresetTheme.GLASS_LIGHT -> {
                        LightHIGColorScheme.copy(
                            accent = accent,
                            keyLetterBackground = Color.White.copy(alpha = (opacity * 0.85f).coerceIn(0.05f, 0.98f)),
                            keyDarkBackground = Color(0xFFD1D5DB).copy(alpha = (opacity * 0.90f).coerceIn(0.08f, 0.98f)),
                            keyPressedBackground = Color(0xFFE5E7EB).copy(alpha = 0.9f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFF9CA3AF).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.9f)) else Color.Transparent,
                            primaryLabel = Color(0xFF111827),
                            secondaryLabel = Color(0xFF6B7280)
                        )
                    }
                    KeyboardPresetTheme.FOOTBALL -> {
                        baseScheme.copy(
                            accent = Color(0xFF22C55E),
                            keyLetterBackground = Color(0xFF15803D).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.9f)),
                            keyDarkBackground = Color(0xFF0F3D1F).copy(alpha = (opacity * 0.85f).coerceIn(0.08f, 0.95f)),
                            keyPressedBackground = Color(0xFF22C55E).copy(alpha = 0.6f),
                            keyBorder = if (settings.keyBorderEnabled) Color.White.copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.9f)) else Color.Transparent,
                            primaryLabel = Color.White,
                            secondaryLabel = Color(0xFFE2E8F0)
                        )
                    }
                    KeyboardPresetTheme.CYBERPUNK -> {
                        baseScheme.copy(
                            accent = Color(0xFF00F0FF),
                            keyLetterBackground = Color(0xFF2A0854).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                            keyDarkBackground = Color(0xFF120326).copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.98f)),
                            keyPressedBackground = Color(0xFFFF007F).copy(alpha = 0.7f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFF00F0FF).copy(alpha = (opacity * 0.5f + 0.3f).coerceIn(0.35f, 0.95f)) else Color.Transparent,
                            primaryLabel = Color(0xFF00F0FF),
                            secondaryLabel = Color(0xFFFF007F)
                        )
                    }
                    KeyboardPresetTheme.SUNSET -> {
                        baseScheme.copy(
                            accent = Color(0xFFF97316),
                            keyLetterBackground = Color(0xFF581832).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                            keyDarkBackground = Color(0xFF2B0718).copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.95f)),
                            keyPressedBackground = Color(0xFFF97316).copy(alpha = 0.65f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFFFDBA74).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.85f)) else Color.Transparent,
                            primaryLabel = Color(0xFFFFF1F2),
                            secondaryLabel = Color(0xFFFED7AA)
                        )
                    }
                    KeyboardPresetTheme.OCEAN -> {
                        baseScheme.copy(
                            accent = Color(0xFF0EA5E9),
                            keyLetterBackground = Color(0xFF0C3B78).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                            keyDarkBackground = Color(0xFF041B3E).copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.95f)),
                            keyPressedBackground = Color(0xFF0EA5E9).copy(alpha = 0.65f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFF38BDF8).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.85f)) else Color.Transparent,
                            primaryLabel = Color(0xFFF0F9FF),
                            secondaryLabel = Color(0xFF7DD3FC)
                        )
                    }
                    KeyboardPresetTheme.EMERALD -> {
                        baseScheme.copy(
                            accent = Color(0xFF10B981),
                            keyLetterBackground = Color(0xFF065F46).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                            keyDarkBackground = Color(0xFF02382B).copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.95f)),
                            keyPressedBackground = Color(0xFF10B981).copy(alpha = 0.65f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFF6EE7B7).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.85f)) else Color.Transparent,
                            primaryLabel = Color(0xFFECFDF5),
                            secondaryLabel = Color(0xFFA7F3D0)
                        )
                    }
                    KeyboardPresetTheme.AURORA -> {
                        baseScheme.copy(
                            accent = Color(0xFF2DD4BF),
                            keyLetterBackground = Color(0xFF1F2937).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                            keyDarkBackground = Color(0xFF111827).copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.98f)),
                            keyPressedBackground = Color(0xFF2DD4BF).copy(alpha = 0.65f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFF34D399).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.85f)) else Color.Transparent,
                            primaryLabel = Color(0xFFF0FDFA),
                            secondaryLabel = Color(0xFF99F6E4)
                        )
                    }
                    KeyboardPresetTheme.MIDNIGHT -> {
                        baseScheme.copy(
                            accent = Color(0xFFA855F7),
                            keyLetterBackground = Color(0xFF2E2A72).copy(alpha = (opacity * 0.75f).coerceIn(0.05f, 0.95f)),
                            keyDarkBackground = Color(0xFF181543).copy(alpha = (opacity * 0.88f).coerceIn(0.08f, 0.98f)),
                            keyPressedBackground = Color(0xFFA855F7).copy(alpha = 0.65f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFFC084FC).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.85f)) else Color.Transparent,
                            primaryLabel = Color(0xFFFAF5FF),
                            secondaryLabel = Color(0xFFE9D5FF)
                        )
                    }
                    KeyboardPresetTheme.PASTEL_BLOOM -> {
                        LightHIGColorScheme.copy(
                            accent = Color(0xFF8B5CF6),
                            keyLetterBackground = Color.White.copy(alpha = (opacity * 0.85f).coerceIn(0.05f, 0.98f)),
                            keyDarkBackground = Color(0xFFEDE9FE).copy(alpha = (opacity * 0.90f).coerceIn(0.08f, 0.98f)),
                            keyPressedBackground = Color(0xFFDDD6FE).copy(alpha = 0.85f),
                            keyBorder = if (settings.keyBorderEnabled) Color(0xFF8B5CF6).copy(alpha = (opacity * 0.5f + 0.25f).coerceIn(0.3f, 0.85f)) else Color.Transparent,
                            primaryLabel = Color(0xFF1E1B4B),
                            secondaryLabel = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}
