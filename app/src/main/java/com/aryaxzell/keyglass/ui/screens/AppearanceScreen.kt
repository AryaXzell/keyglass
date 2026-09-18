package com.aryaxzell.keyglass.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.data.datastore.FontSource
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.data.datastore.ThemeMode
import com.aryaxzell.keyglass.ui.components.HIGColorPicker
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGSegmentedControl
import com.aryaxzell.keyglass.ui.components.HIGSlider
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch

@Composable
fun AppearanceScreen(
    settings: KeyGlassSettings,
    preferencesRepository: PreferencesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val collapseProgress = (scrollState.value / 180f).coerceIn(0f, 1f)

    val animatedRadius by animateDpAsState(
        targetValue = settings.keyCornerRadiusDp.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "animatedRadius"
    )
    val animatedAccentColor by animateColorAsState(
        targetValue = colors.accent,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "animatedAccent"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.appearanceTitle,
            largeTitle = true,
            collapseProgress = collapseProgress,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Live Preview Card
            HIGGroupedSection(header = strings.realtimePreview, staggerIndex = 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Q", "W", "E", "R", "T").forEach { letter ->
                            Box(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(44.dp)
                                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(animatedRadius))
                                    .clip(RoundedCornerShape(animatedRadius))
                                    .background(colors.keyLetterBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letter,
                                    style = HIGTheme.typography.keyLabel,
                                    color = colors.primaryLabel
                                )
                            }
                        }

                        // Return key sample in accent
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .padding(horizontal = 4.dp)
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(animatedRadius))
                                .clip(RoundedCornerShape(animatedRadius))
                                .background(animatedAccentColor)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "return",
                                style = HIGTheme.typography.headline.copy(fontSize = 14.sp),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Theme Mode
            HIGGroupedSection(
                header = strings.interfaceTheme,
                staggerIndex = 1
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    HIGSegmentedControl(
                        items = listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK),
                        selectedItem = settings.themeMode,
                        onItemSelected = { mode ->
                            scope.launch { preferencesRepository.updateThemeMode(mode) }
                        },
                        labelProvider = {
                            when (it) {
                                ThemeMode.SYSTEM -> strings.themeSystem
                                ThemeMode.LIGHT -> strings.themeLight
                                ThemeMode.DARK -> strings.themeDark
                            }
                        }
                    )
                }
            }

            // Key Corner Radius Slider
            HIGGroupedSection(
                header = "${strings.cornerRadius} (${settings.keyCornerRadiusDp.toInt()} dp)",
                staggerIndex = 2
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    HIGSlider(
                        value = settings.keyCornerRadiusDp,
                        onValueChange = { radius ->
                            scope.launch { preferencesRepository.updateKeyCornerRadius(radius) }
                        },
                        valueRange = 0f..12f
                    )
                }
            }

            // Accent Color Swatches
            HIGGroupedSection(
                header = strings.accentColor,
                staggerIndex = 3
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HIGColorPicker(
                        selectedHex = settings.accentColorHex,
                        onColorSelected = { hex ->
                            scope.launch { preferencesRepository.updateAccentColor(hex) }
                        }
                    )
                }
            }

            // Font Source
            HIGGroupedSection(
                header = strings.typographyHeader,
                footer = strings.useInterFontDesc,
                staggerIndex = 4
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    HIGSegmentedControl(
                        items = listOf(FontSource.APP_FONT, FontSource.SYSTEM_FONT),
                        selectedItem = settings.fontSource,
                        onItemSelected = { source ->
                            scope.launch { preferencesRepository.updateFontSource(source) }
                        },
                        labelProvider = {
                            when (it) {
                                FontSource.APP_FONT -> "SF Pro Bold"
                                FontSource.SYSTEM_FONT -> "System Font"
                            }
                        }
                    )
                }
            }
        }
    }
}
