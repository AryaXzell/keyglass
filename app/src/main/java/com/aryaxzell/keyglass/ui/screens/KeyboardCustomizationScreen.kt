package com.aryaxzell.keyglass.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aryaxzell.keyglass.data.datastore.KeyboardBackgroundType
import com.aryaxzell.keyglass.data.datastore.KeyboardPresetTheme
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.ime.AlphaKeyboardLayout
import com.aryaxzell.keyglass.ime.KeyboardBackgroundRenderer
import com.aryaxzell.keyglass.ime.ShiftState
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGButtonStyle
import com.aryaxzell.keyglass.ui.components.HIGCheckmarkIcon
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGPhotoIcon
import com.aryaxzell.keyglass.ui.components.HIGSlider
import com.aryaxzell.keyglass.ui.components.HIGSparklesIcon
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

@Composable
fun KeyboardCustomizationScreen(
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

    // Local staging state for real-time preview before saving
    var previewBackgroundType by remember(settings.backgroundType) { mutableStateOf(settings.backgroundType) }
    var previewPresetTheme by remember(settings.presetTheme) { mutableStateOf(settings.presetTheme) }
    var previewCustomPath by remember(settings.customBackgroundPath) { mutableStateOf(settings.customBackgroundPath) }
    var previewOverlayDim by remember(settings.backgroundOverlayDim) { mutableFloatStateOf(settings.backgroundOverlayDim) }

    // Shift state and key feedback for interactive real-time preview keyboard
    var previewShiftState by remember { mutableStateOf(ShiftState.SHIFT_ONCE) }
    var showSavedBanner by remember { mutableStateOf(false) }

    // Custom preview settings instance for live rendering
    val livePreviewSettings = remember(settings, previewBackgroundType, previewPresetTheme, previewCustomPath, previewOverlayDim) {
        settings.copy(
            backgroundType = previewBackgroundType,
            presetTheme = previewPresetTheme,
            customBackgroundPath = previewCustomPath,
            backgroundOverlayDim = previewOverlayDim
        )
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val savedPath = saveImageToInternalStorage(context, uri)
                if (savedPath != null) {
                    previewCustomPath = savedPath
                    previewBackgroundType = KeyboardBackgroundType.CUSTOM_IMAGE
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.keyboardThemesTitle,
            largeTitle = true,
            collapseProgress = collapseProgress,
            onBackClick = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 40.dp)
        ) {
            // 1. Full Real-Time Interactive Keyboard Preview (Live before/after confirmation)
            HIGGroupedSection(
                header = strings.previewBeforeConfirm,
                staggerIndex = 0
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Badge indicating real-time live preview
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent)
                            )
                            Text(
                                text = strings.realtimePreview,
                                style = HIGTheme.typography.caption1.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.accent
                            )
                        }

                        Text(
                            text = when (previewBackgroundType) {
                                KeyboardBackgroundType.CUSTOM_IMAGE -> strings.customImageHeader
                                KeyboardBackgroundType.PRESET -> getPresetName(previewPresetTheme, strings)
                            },
                            style = HIGTheme.typography.caption2,
                            color = colors.secondaryLabel
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Keyboard Live Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
                    ) {
                        // Render full live keyboard view layout
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            // Background renderer
                            KeyboardBackgroundRenderer(settings = livePreviewSettings)

                            // Interactive Alpha keys preview
                            AlphaKeyboardLayout(
                                settings = livePreviewSettings,
                                shiftState = previewShiftState,
                                cornerRadius = livePreviewSettings.keyCornerRadiusDp.dp,
                                enterLabel = "return",
                                onCharTyped = { /* preview interaction */ },
                                onBackspace = { /* preview interaction */ },
                                onSpace = { /* preview interaction */ },
                                onEnter = { /* preview interaction */ },
                                onShiftClick = {
                                    previewShiftState = when (previewShiftState) {
                                        ShiftState.LOWERCASE -> ShiftState.SHIFT_ONCE
                                        ShiftState.SHIFT_ONCE -> ShiftState.LOWERCASE
                                        ShiftState.CAPS_LOCK -> ShiftState.LOWERCASE
                                    }
                                },
                                onShiftDoubleClick = { previewShiftState = ShiftState.CAPS_LOCK },
                                onSwitchToSymbols = {},
                                onSwitchToEmoji = {},
                                onLanguageSwitch = {},
                                onKeyFeedback = {},
                                onCursorMoved = {},
                                onShowPopup = { _, _ -> },
                                onDismissPopup = {},
                                onShowAccents = { _, _ -> },
                                onAccentSelected = {},
                                onDismissAccents = {}
                            )
                        }
                    }
                }
            }

            // 2. Background Dimming / Darkness Slider
            HIGGroupedSection(
                header = strings.backgroundDarknessHeader,
                footer = strings.backgroundDarknessDesc,
                staggerIndex = 1
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dim: ${(previewOverlayDim * 100).roundToInt()}%",
                            style = HIGTheme.typography.body.copy(fontWeight = FontWeight.Medium),
                            color = colors.primaryLabel
                        )

                        Text(
                            text = if (previewOverlayDim < 0.2f) "Terang (Bright)" else if (previewOverlayDim > 0.6f) "Gelap (Dark)" else "Seimbang (Optimal)",
                            style = HIGTheme.typography.footnote,
                            color = colors.secondaryLabel
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    HIGSlider(
                        value = previewOverlayDim,
                        onValueChange = { previewOverlayDim = it },
                        valueRange = 0.0f..0.85f,
                        steps = 17
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "0%", style = HIGTheme.typography.caption2, color = colors.secondaryLabel)
                        Text(text = "85%", style = HIGTheme.typography.caption2, color = colors.secondaryLabel)
                    }
                }
            }

            // 3. Custom Image Upload Section
            HIGGroupedSection(
                header = strings.customImageHeader,
                staggerIndex = 2
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (previewCustomPath.isNotEmpty() && File(previewCustomPath).exists()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Thumbnail Preview
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (previewBackgroundType == KeyboardBackgroundType.CUSTOM_IMAGE) 2.5.dp else 1.dp,
                                        color = if (previewBackgroundType == KeyboardBackgroundType.CUSTOM_IMAGE) colors.accent else colors.separator,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        previewBackgroundType = KeyboardBackgroundType.CUSTOM_IMAGE
                                    }
                            ) {
                                AsyncImage(
                                    model = File(previewCustomPath),
                                    contentDescription = "Custom Image Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (previewBackgroundType == KeyboardBackgroundType.CUSTOM_IMAGE) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        HIGCheckmarkIcon(color = Color.White)
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HIGButton(
                                    text = strings.changeImageBtn,
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    style = HIGButtonStyle.TINTED,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                HIGButton(
                                    text = strings.removeCustomImageBtn,
                                    onClick = {
                                        previewCustomPath = ""
                                        previewBackgroundType = KeyboardBackgroundType.PRESET
                                    },
                                    style = HIGButtonStyle.PLAIN,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Empty state: Choose photo button
                        HIGButton(
                            text = strings.chooseImageBtn,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            style = HIGButtonStyle.FILLED,
                            leadingIcon = { HIGPhotoIcon(color = Color.White) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 4. Preset Themes Grid Section
            HIGGroupedSection(
                header = strings.presetThemesHeader,
                staggerIndex = 3
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val presets = listOf(
                        KeyboardPresetTheme.GLASS_DARK,
                        KeyboardPresetTheme.GLASS_LIGHT,
                        KeyboardPresetTheme.FOOTBALL,
                        KeyboardPresetTheme.CYBERPUNK,
                        KeyboardPresetTheme.SUNSET,
                        KeyboardPresetTheme.OCEAN,
                        KeyboardPresetTheme.EMERALD,
                        KeyboardPresetTheme.AURORA,
                        KeyboardPresetTheme.MIDNIGHT,
                        KeyboardPresetTheme.PASTEL_BLOOM
                    )

                    // 2-column visual cards
                    for (chunk in presets.chunked(2)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            chunk.forEach { preset ->
                                val isSelected = previewBackgroundType == KeyboardBackgroundType.PRESET && previewPresetTheme == preset
                                ThemeCard(
                                    preset = preset,
                                    isSelected = isSelected,
                                    title = getPresetName(preset, strings),
                                    onClick = {
                                        previewBackgroundType = KeyboardBackgroundType.PRESET
                                        previewPresetTheme = preset
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (chunk.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 5. Action Buttons & Save Confirmation
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Success Toast Banner
                AnimatedVisibility(
                    visible = showSavedBanner,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.success.copy(alpha = 0.15f))
                            .border(1.dp, colors.success.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HIGCheckmarkIcon(color = colors.success)
                            Text(
                                text = strings.themeAppliedSuccess,
                                style = HIGTheme.typography.subhead.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.success
                            )
                        }
                    }
                }

                HIGButton(
                    text = strings.applyThemeBtn,
                    onClick = {
                        scope.launch {
                            preferencesRepository.updateKeyboardBackgroundConfig(
                                type = previewBackgroundType,
                                preset = previewPresetTheme,
                                customPath = previewCustomPath,
                                dim = previewOverlayDim
                            )
                            showSavedBanner = true
                            Toast.makeText(context, strings.themeAppliedSuccess, Toast.LENGTH_SHORT).show()
                        }
                    },
                    style = HIGButtonStyle.FILLED,
                    modifier = Modifier.fillMaxWidth()
                )

                HIGButton(
                    text = strings.resetToDefaultTheme,
                    onClick = {
                        previewBackgroundType = KeyboardBackgroundType.PRESET
                        previewPresetTheme = KeyboardPresetTheme.GLASS_DARK
                        previewOverlayDim = 0.35f
                        previewCustomPath = ""
                        scope.launch {
                            preferencesRepository.updateKeyboardBackgroundConfig(
                                type = KeyboardBackgroundType.PRESET,
                                preset = KeyboardPresetTheme.GLASS_DARK,
                                customPath = "",
                                dim = 0.35f
                            )
                            showSavedBanner = true
                        }
                    },
                    style = HIGButtonStyle.PLAIN,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    preset: KeyboardPresetTheme,
    isSelected: Boolean,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HIGTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.accent else Color.Transparent,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "themeBorder"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Thumbnail Graphic Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) colors.accent else colors.separator.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            // Mini Preset Preview Render
            when (preset) {
                KeyboardPresetTheme.GLASS_DARK -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C1E)))
                }
                KeyboardPresetTheme.GLASS_LIGHT -> {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB)))
                }
                KeyboardPresetTheme.FOOTBALL -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF104E27), Color(0xFF1A6B37))))
                        drawCircle(color = Color.White.copy(alpha = 0.4f), radius = size.height * 0.3f, center = Offset(size.width / 2f, size.height / 2f))
                    }
                }
                KeyboardPresetTheme.CYBERPUNK -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF090314), Color(0xFF1D053A))))
                        drawLine(color = Color(0xFF00F0FF).copy(alpha = 0.4f), start = Offset(0f, size.height / 2f), end = Offset(size.width, size.height / 2f))
                    }
                }
                KeyboardPresetTheme.SUNSET -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF18082B), Color(0xFFC83A5A), Color(0xFFFDC830))))
                    }
                }
                KeyboardPresetTheme.OCEAN -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF021024), Color(0xFF0A4480), Color(0xFF1572B6))))
                    }
                }
                KeyboardPresetTheme.EMERALD -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.linearGradient(listOf(Color(0xFF022C22), Color(0xFF047857))))
                    }
                }
                KeyboardPresetTheme.AURORA -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF030712), Color(0xFF1C2541))))
                        drawCircle(color = Color(0xFF00FFA3).copy(alpha = 0.45f), radius = size.width * 0.4f, center = Offset(size.width * 0.3f, size.height * 0.4f))
                    }
                }
                KeyboardPresetTheme.MIDNIGHT -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(0xFF090D16), Color(0xFF1E1B4B))))
                        drawCircle(color = Color(0xFFEC4899).copy(alpha = 0.3f), radius = size.height * 0.3f, center = Offset(size.width * 0.7f, size.height * 0.4f))
                    }
                }
                KeyboardPresetTheme.PASTEL_BLOOM -> {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(brush = Brush.linearGradient(listOf(Color(0xFFFFDFD3), Color(0xFFF3E8FF), Color(0xFFDCFCE7))))
                    }
                }
            }

            // Key sample mockups over thumbnail
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(14.dp)
                            .height(18.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = if (preset == KeyboardPresetTheme.GLASS_LIGHT || preset == KeyboardPresetTheme.PASTEL_BLOOM) 0.5f else 0.2f))
                    )
                }
            }

            // Checkmark indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center
                ) {
                    HIGCheckmarkIcon(color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = title,
            style = HIGTheme.typography.caption1.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            color = if (isSelected) colors.primaryLabel else colors.secondaryLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun getPresetName(preset: KeyboardPresetTheme, strings: com.aryaxzell.keyglass.ui.localization.Strings): String {
    return when (preset) {
        KeyboardPresetTheme.GLASS_DARK -> strings.presetGlassDark
        KeyboardPresetTheme.GLASS_LIGHT -> strings.presetGlassLight
        KeyboardPresetTheme.FOOTBALL -> strings.presetFootball
        KeyboardPresetTheme.CYBERPUNK -> strings.presetCyberpunk
        KeyboardPresetTheme.SUNSET -> strings.presetSunset
        KeyboardPresetTheme.OCEAN -> strings.presetOcean
        KeyboardPresetTheme.EMERALD -> strings.presetEmerald
        KeyboardPresetTheme.AURORA -> strings.presetAurora
        KeyboardPresetTheme.MIDNIGHT -> strings.presetMidnight
        KeyboardPresetTheme.PASTEL_BLOOM -> strings.presetPastelBloom
    }
}

private suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            val destinationFile = File(context.filesDir, "custom_keyboard_bg_${System.currentTimeMillis()}.png")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
