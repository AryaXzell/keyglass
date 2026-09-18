package com.aryaxzell.keyglass.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.ui.components.HIGAlert
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGButtonStyle
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
    var showInstallPermissionModal by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    var isIgnoringBattery by remember { mutableStateOf(true) }
    var canInstallPackages by remember { mutableStateOf(true) }

    fun checkPermissions() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isIgnoringBattery = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        canInstallPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

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
            // System Permissions & Reliability
            HIGGroupedSection(
                header = strings.systemPermissionsHeader,
                staggerIndex = 0
            ) {
                HIGRow(
                    title = strings.batteryOptimizationTitle,
                    subtitle = strings.batteryOptimizationSubtitle,
                    trailingAccessory = {
                        Text(
                            text = if (isIgnoringBattery) strings.statusExempted else strings.statusNotExempted,
                            style = HIGTheme.typography.caption1.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isIgnoringBattery) colors.success else colors.destructive
                        )
                    },
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(fallback)
                        }
                    }
                )

                HIGRow(
                    title = strings.installUnknownAppsTitle,
                    subtitle = strings.installUnknownAppsSubtitle,
                    showDivider = false,
                    trailingAccessory = {
                        Text(
                            text = if (canInstallPackages) strings.statusAllowed else strings.statusNotAllowed,
                            style = HIGTheme.typography.caption1.copy(fontWeight = FontWeight.SemiBold),
                            color = if (canInstallPackages) colors.success else colors.destructive
                        )
                    },
                    onClick = {
                        showInstallPermissionModal = true
                    }
                )
            }

            // App UI Language
            HIGGroupedSection(header = strings.companionLanguageHeader, staggerIndex = 1) {
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
                staggerIndex = 2
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
                staggerIndex = 3
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

    // Install Permission Explanation Modal
    HIGDialog(
        show = showInstallPermissionModal,
        onDismissRequest = { showInstallPermissionModal = false }
    ) {
        Column {
            Text(
                text = strings.installPermissionModalTitle,
                style = HIGTheme.typography.title2,
                color = colors.primaryLabel
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = strings.installPermissionModalDesc,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            HIGButton(
                text = strings.openSettingsBtn,
                onClick = {
                    showInstallPermissionModal = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                                data = Uri.parse("package:${context.packageName}")
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            HIGButton(
                text = strings.cancel,
                style = HIGButtonStyle.TINTED,
                onClick = { showInstallPermissionModal = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
