package com.aryaxzell.keyglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.components.HIGSearchBar
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.navigation.Screen
import com.aryaxzell.keyglass.ui.theme.HIGTheme

data class SearchableSetting(
    val title: String,
    val category: String,
    val targetRoute: String,
    val keywords: List<String>
)

val SETTINGS_INDEX = listOf(
    SearchableSetting("Theme (Light / Dark)", "Appearance", Screen.Appearance.route, listOf("dark", "light", "mode", "theme", "tema", "gelap", "terang")),
    SearchableSetting("Key Corner Radius", "Appearance", Screen.Appearance.route, listOf("corner", "radius", "round", "shape", "sudut")),
    SearchableSetting("Accent Color", "Appearance", Screen.Appearance.route, listOf("color", "accent", "blue", "warna", "aksen")),
    SearchableSetting("Font Family", "Appearance", Screen.Appearance.route, listOf("font", "inter", "typography", "tipografi")),
    SearchableSetting("Predictive Text Bar", "Typing Behavior", Screen.TypingBehavior.route, listOf("predictive", "suggestion", "bar", "prediksi", "saran")),
    SearchableSetting("Typo Correction", "Typing Behavior", Screen.TypingBehavior.route, listOf("autocorrect", "typo", "correction", "koreksi")),
    SearchableSetting("Key Popup Bubble", "Typing Behavior", Screen.TypingBehavior.route, listOf("popup", "bubble", "preview", "gelembung")),
    SearchableSetting("Haptic Feedback", "Typing Behavior", Screen.TypingBehavior.route, listOf("vibration", "haptic", "intensity", "getar")),
    SearchableSetting("Sound Feedback", "Typing Behavior", Screen.TypingBehavior.route, listOf("sound", "audio", "volume", "suara")),
    SearchableSetting("Auto-Capitalization", "Typing Behavior", Screen.TypingBehavior.route, listOf("caps", "capitalize", "shift", "kapital")),
    SearchableSetting("Typing Languages", "Typing Behavior", Screen.TypingBehavior.route, listOf("language", "english", "indonesian", "kamus", "bahasa")),
    SearchableSetting("Trackpad Cursor Sensitivity", "Gestures", Screen.Gestures.route, listOf("space", "trackpad", "cursor", "kursor", "sensitivitas")),
    SearchableSetting("Long-Press Duration", "Gestures", Screen.Gestures.route, listOf("delay", "longpress", "tekan", "durasi")),
    SearchableSetting("Personal Dictionary", "Dictionary", Screen.PersonalDictionary.route, listOf("whitelist", "words", "slang", "kamus", "kata")),
    SearchableSetting("Companion App Language", "General", Screen.General.route, listOf("locale", "language", "ui", "bahasa")),
    SearchableSetting("Export / Import Settings", "General", Screen.General.route, listOf("backup", "export", "import", "cadangan")),
    SearchableSetting("Reset All Settings", "General", Screen.General.route, listOf("reset", "defaults", "atur ulang")),
    SearchableSetting("About KeyGlass & Updates", "About", Screen.About.route, listOf("about", "version", "github", "developer", "update", "pembaruan"))
)

@Composable
fun SettingsSearchScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            SETTINGS_INDEX
        } else {
            val q = searchQuery.trim().lowercase()
            SETTINGS_INDEX.filter { item ->
                item.title.lowercase().contains(q) ||
                        item.category.lowercase().contains(q) ||
                        item.keywords.any { it.contains(q) }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.search,
            onBackClick = onBack
        )

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            HIGSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = strings.searchPlaceholder
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
        ) {
            item {
                HIGGroupedSection(
                    header = if (searchQuery.isBlank()) strings.allSettingsHeader else "${filteredItems.size} ${strings.resultsHeader}"
                ) {
                    filteredItems.forEachIndexed { index, item ->
                        HIGRow(
                            title = item.title,
                            subtitle = item.category,
                            showChevron = true,
                            showDivider = index < filteredItems.size - 1,
                            onClick = { onNavigate(item.targetRoute) }
                        )
                    }
                }
            }
        }
    }
}
