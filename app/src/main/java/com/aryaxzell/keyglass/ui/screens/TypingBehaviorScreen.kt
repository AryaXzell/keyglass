package com.aryaxzell.keyglass.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.ui.components.HIGCheckmarkIcon
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.components.HIGSlider
import com.aryaxzell.keyglass.ui.components.HIGSwitch
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch

@Composable
fun TypingBehaviorScreen(
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.typingBehaviorTitle,
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
            // Section 1: Intelligence
            HIGGroupedSection(
                header = strings.intelligenceHeader,
                staggerIndex = 0
            ) {
                HIGRow(
                    title = strings.predictiveBar,
                    subtitle = strings.predictiveBarDesc,
                    trailingAccessory = {
                        HIGSwitch(
                            checked = settings.predictiveTextEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { preferencesRepository.updatePredictiveText(checked) }
                            }
                        )
                    }
                )

                HIGRow(
                    title = strings.typoCorrection,
                    subtitle = strings.typoCorrectionDesc,
                    trailingAccessory = {
                        HIGSwitch(
                            checked = settings.typoCorrectionEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { preferencesRepository.updateTypoCorrection(checked) }
                            }
                        )
                    }
                )

                HIGRow(
                    title = strings.keyPopupBubble,
                    subtitle = strings.keyPopupBubbleDesc,
                    showDivider = false,
                    trailingAccessory = {
                        HIGSwitch(
                            checked = settings.keyPopupBubbleEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { preferencesRepository.updateKeyPopupBubble(checked) }
                            }
                        )
                    }
                )
            }

            // Section 2: Feedback
            HIGGroupedSection(header = strings.hapticSoundHeader, staggerIndex = 1) {
                HIGRow(
                    title = strings.hapticFeedback,
                    subtitle = strings.hapticFeedbackDesc,
                    trailingAccessory = {
                        HIGSwitch(
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { preferencesRepository.updateHapticFeedback(checked) }
                            }
                        )
                    }
                )

                AnimatedVisibility(
                    visible = settings.hapticFeedbackEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        HIGRow(
                            title = "${strings.vibrationIntensity} (${(settings.hapticIntensity * 100).toInt()}%)",
                            trailingAccessory = null,
                            showDivider = true
                        )
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            HIGSlider(
                                value = settings.hapticIntensity,
                                onValueChange = { intensity ->
                                    scope.launch { preferencesRepository.updateHapticIntensity(intensity) }
                                },
                                valueRange = 0.1f..1.0f
                            )
                        }
                    }
                }

                HIGRow(
                    title = strings.soundFeedback,
                    subtitle = strings.soundFeedbackDesc,
                    trailingAccessory = {
                        HIGSwitch(
                            checked = settings.soundFeedbackEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { preferencesRepository.updateSoundFeedback(checked) }
                            }
                        )
                    }
                )

                AnimatedVisibility(
                    visible = settings.soundFeedbackEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        HIGRow(
                            title = "${strings.soundVolume} (${(settings.soundVolume * 100).toInt()}%)",
                            trailingAccessory = null,
                            showDivider = false
                        )
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            HIGSlider(
                                value = settings.soundVolume,
                                onValueChange = { volume ->
                                    scope.launch { preferencesRepository.updateSoundVolume(volume) }
                                },
                                valueRange = 0.1f..1.0f
                            )
                        }
                    }
                }
            }

            // Section 3: Capitalization & Languages
            HIGGroupedSection(header = strings.capitalizationHeader, staggerIndex = 2) {
                HIGRow(
                    title = strings.autoCapitalization,
                    subtitle = strings.autoCapitalizationDesc,
                    showDivider = false,
                    trailingAccessory = {
                        HIGSwitch(
                            checked = settings.autoCapitalizeEnabled,
                            onCheckedChange = { checked ->
                                scope.launch { preferencesRepository.updateAutoCapitalize(checked) }
                            }
                        )
                    }
                )
            }

            HIGGroupedSection(
                header = strings.typingLanguagesHeader,
                staggerIndex = 3
            ) {
                val isEnSelected = settings.typingLanguages.contains("en")
                val isIdSelected = settings.typingLanguages.contains("in")

                HIGRow(
                    title = strings.englishLanguage,
                    subtitle = "QWERTY (US)",
                    trailingAccessory = {
                        if (isEnSelected) HIGCheckmarkIcon(color = colors.accent)
                    },
                    onClick = {
                        if (isEnSelected && settings.typingLanguages.size > 1) {
                            val newSet = settings.typingLanguages - "en"
                            scope.launch { preferencesRepository.updateTypingLanguages(newSet) }
                        } else if (!isEnSelected) {
                            val newSet = settings.typingLanguages + "en"
                            scope.launch { preferencesRepository.updateTypingLanguages(newSet) }
                        }
                    }
                )

                HIGRow(
                    title = strings.indonesianLanguage,
                    subtitle = "QWERTY (Kamus Indonesia)",
                    showDivider = false,
                    trailingAccessory = {
                        if (isIdSelected) HIGCheckmarkIcon(color = colors.accent)
                    },
                    onClick = {
                        if (isIdSelected && settings.typingLanguages.size > 1) {
                            val newSet = settings.typingLanguages - "in"
                            scope.launch { preferencesRepository.updateTypingLanguages(newSet) }
                        } else if (!isIdSelected) {
                            val newSet = settings.typingLanguages + "in"
                            scope.launch { preferencesRepository.updateTypingLanguages(newSet) }
                        }
                    }
                )
            }
        }
    }
}
