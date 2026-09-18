package com.aryaxzell.keyglass.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.ui.components.HIGAlert
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGCheckmarkIcon
import com.aryaxzell.keyglass.ui.components.HIGDialog
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch

@Composable
fun GeneralScreen(
    settings: KeyGlassSettings,
    preferencesRepository: PreferencesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val collapseProgress = (scrollState.value / 180f).coerceIn(0f, 1f)

    var showResetDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.generalTitle,
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
            // App UI Language
            HIGGroupedSection(header = strings.companionLanguageHeader, staggerIndex = 0) {
                val langs = listOf(
                    "system" to strings.languageSystem,
                    "en" to strings.languageEnglish,
                    "in" to strings.languageIndonesian
                )
                langs.forEachIndexed { index, (key, label) ->
                    val isSelected = settings.appLanguage == key
                    HIGRow(
                        title = label,
                        showDivider = index < langs.size - 1,
                        trailingAccessory = {
                            if (isSelected) HIGCheckmarkIcon(color = colors.accent)
                        },
                        onClick = {
                            scope.launch { preferencesRepository.updateAppLanguage(key) }
                        }
                    )
                }
            }

            // Backup & Migration
            HIGGroupedSection(
                header = strings.backupHeader,
                staggerIndex = 1
            ) {
                HIGRow(
                    title = strings.exportJsonTitle,
                    subtitle = strings.exportJsonSubtitle,
                    onClick = {
                        scope.launch {
                            val json = preferencesRepository.exportSettingsJson(settings)
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("KeyGlassSettings", json))
                            Toast.makeText(context, strings.toastCopied, Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HIGRow(
                    title = strings.importJsonTitle,
                    subtitle = strings.importJsonSubtitle,
                    showDivider = false,
                    onClick = {
                        importJsonText = ""
                        showImportDialog = true
                    }
                )
            }

            // Reset Section
            HIGGroupedSection(
                header = strings.resetHeader,
                staggerIndex = 2
            ) {
                HIGRow(
                    title = strings.resetAllSettingsTitle,
                    showDivider = false,
                    onClick = { showResetDialog = true }
                )
            }
        }
    }

    // Reset Confirmation Alert
    if (showResetDialog) {
        HIGAlert(
            title = strings.resetDialogTitle,
            message = strings.resetDialogMessage,
            confirmText = strings.resetConfirmBtn,
            isDestructive = true,
            onConfirm = {
                showResetDialog = false
                scope.launch {
                    preferencesRepository.resetToDefaults()
                    Toast.makeText(context, strings.toastResetDone, Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Import JSON Dialog
    HIGDialog(
        show = showImportDialog,
        onDismissRequest = { showImportDialog = false }
    ) {
        Column {
            Text(
                text = strings.importJsonTitle,
                style = HIGTheme.typography.headline,
                color = colors.primaryLabel
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.importJsonSubtitle,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background)
                    .padding(10.dp)
            ) {
                BasicTextField(
                    value = importJsonText,
                    onValueChange = { importJsonText = it },
                    textStyle = HIGTheme.typography.footnote.copy(color = colors.primaryLabel),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HIGButton(
                text = strings.applySettingsBtn,
                onClick = {
                    scope.launch {
                        val success = preferencesRepository.importSettingsJson(importJsonText)
                        if (success) {
                            showImportDialog = false
                            Toast.makeText(context, strings.toastImportDone, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, strings.toastInvalidJson, Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
