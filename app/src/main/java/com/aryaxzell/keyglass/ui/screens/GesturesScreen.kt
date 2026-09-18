package com.aryaxzell.keyglass.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.components.HIGSlider
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun GesturesScreen(
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
            title = strings.gesturesTitle,
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
            // Space Bar Cursor Sensitivity
            HIGGroupedSection(
                header = strings.trackpadHeader,
                footer = strings.trackpadDesc,
                staggerIndex = 0
            ) {
                val formattedSensitivity = String.format(Locale.US, "%.1fx", settings.spaceBarCursorSensitivity)
                HIGRow(
                    title = strings.trackpadSensitivity,
                    value = formattedSensitivity,
                    showDivider = true
                )
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    HIGSlider(
                        value = settings.spaceBarCursorSensitivity,
                        onValueChange = { sens ->
                            scope.launch { preferencesRepository.updateSpaceBarSensitivity(sens) }
                        },
                        valueRange = 0.5f..2.0f
                    )
                }
            }

            // Long-Press Duration
            HIGGroupedSection(
                header = strings.keyHoldTimingHeader,
                footer = strings.longPressDesc,
                staggerIndex = 1
            ) {
                HIGRow(
                    title = strings.longPressDelay,
                    value = "${settings.longPressDurationMs} ms",
                    showDivider = true
                )
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    HIGSlider(
                        value = settings.longPressDurationMs.toFloat(),
                        onValueChange = { duration ->
                            scope.launch { preferencesRepository.updateLongPressDuration(duration.toLong()) }
                        },
                        valueRange = 250f..700f
                    )
                }
            }
        }
    }
}
