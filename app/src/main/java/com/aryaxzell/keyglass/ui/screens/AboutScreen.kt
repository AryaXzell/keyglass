package com.aryaxzell.keyglass.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aryaxzell.keyglass.data.github.GitHubProfile
import com.aryaxzell.keyglass.data.github.GitHubRepository
import com.aryaxzell.keyglass.data.github.NightlyUpdateInfo
import com.aryaxzell.keyglass.data.github.UpdateDownloadState
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGButtonStyle
import com.aryaxzell.keyglass.ui.components.HIGCheckmarkIcon
import com.aryaxzell.keyglass.ui.components.HIGDialog
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGKeyboardGlyphIcon
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val collapseProgress = (scrollState.value / 180f).coerceIn(0f, 1f)
    val gitHubRepository = remember { GitHubRepository() }

    val deviceArch = remember { gitHubRepository.getDeviceArchitecture() }
    var profile by remember { mutableStateOf<GitHubProfile?>(null) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Update modal states
    var showUpdateModal by remember { mutableStateOf(false) }
    var showInstallPermissionDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<NightlyUpdateInfo?>(null) }
    var downloadState by remember { mutableStateOf<UpdateDownloadState>(UpdateDownloadState.Idle) }

    val installedVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    LaunchedEffect(Unit) {
        val res = gitHubRepository.getDeveloperProfile()
        if (res.isSuccess) {
            profile = res.getOrNull()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.aboutTitle,
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
            // Card 1: App Info
            HIGGroupedSection(header = strings.appInfoHeader, staggerIndex = 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        HIGKeyboardGlyphIcon(
                            color = colors.accent,
                            size = 40.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "KeyGlass",
                        style = HIGTheme.typography.title1,
                        color = colors.primaryLabel
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.appVersionLabel} (1.0.0)",
                            style = HIGTheme.typography.footnote,
                            color = colors.secondaryLabel
                        )

                        // Nightly build badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFF9500).copy(alpha = 0.18f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = strings.nightlyBadge,
                                style = HIGTheme.typography.caption2.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFFF9500)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Architecture info
                    Text(
                        text = "${strings.architectureLabel}: $deviceArch",
                        style = HIGTheme.typography.caption2,
                        color = colors.secondaryLabel
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HIGButton(
                        text = if (isCheckingUpdates) strings.checkingUpdates else strings.checkForUpdates,
                        enabled = !isCheckingUpdates,
                        style = HIGButtonStyle.FILLED,
                        onClick = {
                            isCheckingUpdates = true
                            scope.launch {
                                val checkResult = gitHubRepository.checkNightlyUpdates(installedVersion)
                                isCheckingUpdates = false
                                val info = checkResult.getOrNull()
                                if (info != null) {
                                    updateInfo = info
                                    if (info.hasUpdate) {
                                        downloadState = UpdateDownloadState.Idle
                                        showUpdateModal = true
                                    } else {
                                        Toast.makeText(context, strings.noUpdateAvailable, Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, strings.updateError, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }

                HIGRow(
                    title = strings.whatsNewTitle,
                    showChevron = true,
                    onClick = { showWhatsNewDialog = true }
                )

                HIGRow(
                    title = strings.privacyPromiseTitle,
                    showChevron = true,
                    showDivider = false,
                    onClick = { showPrivacyDialog = true }
                )
            }

            // Card 2: Developer
            HIGGroupedSection(header = strings.developerHeader, staggerIndex = 1) {
                val currentProfile = profile ?: GitHubProfile()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = currentProfile.avatarUrl,
                        contentDescription = "Arya Xzell avatar",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(colors.separator)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentProfile.name,
                            style = HIGTheme.typography.headline,
                            color = colors.primaryLabel
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "@${currentProfile.login}",
                            style = HIGTheme.typography.subhead,
                            color = colors.accent
                        )
                    }
                }

                HIGRow(
                    title = strings.githubProfile,
                    subtitle = "github.com/aryaxzell",
                    showChevron = true,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aryaxzell")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                )

                HIGRow(
                    title = strings.sourceCodeTitle,
                    subtitle = "github.com/aryaxzell/keyglass",
                    showChevron = true,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aryaxzell/keyglass")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                )

                HIGRow(
                    title = strings.reportIssueTitle,
                    subtitle = "github.com/aryaxzell/keyglass/issues",
                    showChevron = true,
                    showDivider = false,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/aryaxzell/keyglass/issues")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                )
            }
        }
    }

    // Nightly Update Download & Install Modal
    if (showUpdateModal && updateInfo != null) {
        val info = updateInfo!!
        HIGDialog(
            show = showUpdateModal,
            onDismissRequest = {
                if (downloadState !is UpdateDownloadState.Downloading && downloadState !is UpdateDownloadState.Extracting) {
                    showUpdateModal = false
                }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (val state = downloadState) {
                    is UpdateDownloadState.Idle -> {
                        Text(
                            text = strings.updateModalTitle,
                            style = HIGTheme.typography.title2,
                            color = colors.primaryLabel
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${info.artifactName} (${info.versionLabel}) • $deviceArch",
                            style = HIGTheme.typography.footnote,
                            color = colors.secondaryLabel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = strings.updateAvailableDesc,
                            style = HIGTheme.typography.body,
                            color = colors.primaryLabel,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HIGButton(
                            text = "Unduh Pembaruan ($deviceArch)",
                            style = HIGButtonStyle.FILLED,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                downloadState = UpdateDownloadState.Downloading(0f, 0L, 0L)
                                scope.launch {
                                    val dlResult = gitHubRepository.downloadAndExtractUpdate(
                                        context = context,
                                        updateInfo = info,
                                        onProgress = { p, bytes, total ->
                                            downloadState = UpdateDownloadState.Downloading(p, bytes, total)
                                        },
                                        onExtracting = { status ->
                                            downloadState = UpdateDownloadState.Extracting(status)
                                        }
                                    )

                                    if (dlResult.isSuccess) {
                                        val apkFile = dlResult.getOrThrow()
                                        val apkUri = androidx.core.content.FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            apkFile
                                        )
                                        downloadState = UpdateDownloadState.ReadyToInstall(apkFile, apkUri)
                                    } else {
                                        downloadState = UpdateDownloadState.Error(
                                            message = dlResult.exceptionOrNull()?.message ?: "Gagal mengunduh pembaruan.",
                                            fallbackUrl = info.htmlUrl
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HIGButton(
                            text = strings.openGitHubActionsBtn,
                            style = HIGButtonStyle.TINTED,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                gitHubRepository.openGitHubActions(context, info.htmlUrl)
                                showUpdateModal = false
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HIGButton(
                            text = strings.cancel,
                            style = HIGButtonStyle.PLAIN,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showUpdateModal = false }
                        )
                    }

                    is UpdateDownloadState.Downloading -> {
                        val percent = (state.progress * 100).toInt()

                        Text(
                            text = strings.downloadingUpdate,
                            style = HIGTheme.typography.title2,
                            color = colors.primaryLabel
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${info.artifactName} ($percent%)",
                            style = HIGTheme.typography.footnote,
                            color = colors.secondaryLabel
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.accent,
                            trackColor = colors.groupedCard
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (state.totalBytes > 0) {
                            val mbDownloaded = state.bytesDownloaded / (1024f * 1024f)
                            val mbTotal = state.totalBytes / (1024f * 1024f)
                            Text(
                                text = "%.1f MB / %.1f MB".format(mbDownloaded, mbTotal),
                                style = HIGTheme.typography.caption1,
                                color = colors.secondaryLabel
                            )
                        } else {
                            Text(
                                text = "Mengunduh file paket...",
                                style = HIGTheme.typography.caption1,
                                color = colors.secondaryLabel
                            )
                        }
                    }

                    is UpdateDownloadState.Extracting -> {
                        Text(
                            text = "Memproses Paket",
                            style = HIGTheme.typography.title2,
                            color = colors.primaryLabel
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(36.dp),
                            color = colors.accent
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = strings.extractingUpdate,
                            style = HIGTheme.typography.subhead,
                            color = colors.secondaryLabel
                        )
                    }

                    is UpdateDownloadState.ReadyToInstall -> {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(colors.success.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            HIGCheckmarkIcon(color = colors.success)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = strings.updateReadyTitle,
                            style = HIGTheme.typography.title2,
                            color = colors.primaryLabel
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = strings.updateReadyDesc,
                            style = HIGTheme.typography.body,
                            color = colors.secondaryLabel,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HIGButton(
                            text = strings.installUpdateBtn,
                            style = HIGButtonStyle.FILLED,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                                    showInstallPermissionDialog = true
                                } else {
                                    gitHubRepository.launchPackageInstaller(context, state.apkFile)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HIGButton(
                            text = strings.close,
                            style = HIGButtonStyle.TINTED,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showUpdateModal = false }
                        )
                    }

                    is UpdateDownloadState.Error -> {
                        Text(
                            text = "Gagal Mengunduh Pembaruan",
                            style = HIGTheme.typography.title2,
                            color = colors.destructive
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = state.message,
                            style = HIGTheme.typography.body,
                            color = colors.primaryLabel,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HIGButton(
                            text = "Coba Lagi",
                            style = HIGButtonStyle.FILLED,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { downloadState = UpdateDownloadState.Idle }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HIGButton(
                            text = strings.openGitHubActionsBtn,
                            style = HIGButtonStyle.TINTED,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                gitHubRepository.openGitHubActions(context, state.fallbackUrl)
                                showUpdateModal = false
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        HIGButton(
                            text = strings.close,
                            style = HIGButtonStyle.PLAIN,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showUpdateModal = false }
                        )
                    }
                }
            }
        }
    }

    // What's New Dialog
    HIGDialog(
        show = showWhatsNewDialog,
        onDismissRequest = { showWhatsNewDialog = false }
    ) {
        Column {
            Text(
                text = strings.whatsNewTitle,
                style = HIGTheme.typography.title2,
                color = colors.primaryLabel
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.whatsNewText,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            HIGButton(
                text = strings.done,
                onClick = { showWhatsNewDialog = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Privacy Policy Dialog
    HIGDialog(
        show = showPrivacyDialog,
        onDismissRequest = { showPrivacyDialog = false }
    ) {
        Column {
            Text(
                text = strings.privacyPromiseTitle,
                style = HIGTheme.typography.title2,
                color = colors.primaryLabel
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.privacyPromiseText,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            HIGButton(
                text = strings.close,
                onClick = { showPrivacyDialog = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Install Unknown Apps Permission Dialog
    HIGDialog(
        show = showInstallPermissionDialog,
        onDismissRequest = { showInstallPermissionDialog = false }
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
                    showInstallPermissionDialog = false
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
                onClick = { showInstallPermissionDialog = false },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
