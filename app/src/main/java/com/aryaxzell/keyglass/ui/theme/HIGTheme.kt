package com.aryaxzell.keyglass.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import com.aryaxzell.keyglass.data.datastore.FontSource
import com.aryaxzell.keyglass.data.datastore.ThemeMode

val LocalHIGColors = staticCompositionLocalOf { DarkHIGColorScheme }
val LocalHIGTypography = staticCompositionLocalOf { getHIGTypography(SFProFontFamily) }

object HIGTheme {
    val colors: HIGColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalHIGColors.current

    val typography: HIGTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHIGTypography.current
}

@Composable
fun KeyGlassTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentColorHex: String = "#007AFF",
    fontSource: FontSource = FontSource.APP_FONT,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val accent = parseHexColor(accentColorHex)
    val baseScheme = if (isDark) DarkHIGColorScheme else LightHIGColorScheme
    val colorScheme = baseScheme.copy(accent = accent)

    val fontFamily = if (fontSource == FontSource.APP_FONT) SFProFontFamily else FontFamily.Default
    val typography = getHIGTypography(fontFamily)

    CompositionLocalProvider(
        LocalHIGColors provides colorScheme,
        LocalHIGTypography provides typography,
        content = content
    )
}
