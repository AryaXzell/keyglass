package com.aryaxzell.keyglass.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.data.datastore.PreferencesRepository
import com.aryaxzell.keyglass.ui.components.HIGBatteryIcon
import com.aryaxzell.keyglass.ui.components.HIGBoltIcon
import com.aryaxzell.keyglass.ui.components.HIGBookIcon
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGButtonStyle
import com.aryaxzell.keyglass.ui.components.HIGCheckmarkIcon
import com.aryaxzell.keyglass.ui.components.HIGClearIcon
import com.aryaxzell.keyglass.ui.components.HIGGearIcon
import com.aryaxzell.keyglass.ui.components.HIGGroupedSection
import com.aryaxzell.keyglass.ui.components.HIGSwitch
import com.aryaxzell.keyglass.ui.components.HIGHandTapIcon
import com.aryaxzell.keyglass.ui.components.HIGInfoIcon
import com.aryaxzell.keyglass.ui.components.HIGNavBar
import com.aryaxzell.keyglass.ui.components.HIGPaletteIcon
import com.aryaxzell.keyglass.ui.components.HIGRow
import com.aryaxzell.keyglass.ui.components.HIGSearchIcon
import com.aryaxzell.keyglass.ui.components.HIGSparklesIcon
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.navigation.Screen
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    settings: KeyGlassSettings,
    preferencesRepository: PreferencesRepository,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val collapseProgress = (scrollState.value / 180f).coerceIn(0f, 1f)

    var isEnabled by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }
    var isIgnoringBattery by remember { mutableStateOf(true) }
    var testInputText by remember { mutableStateOf("") }
    var isFieldFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun checkStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            val enabledList = imm.enabledInputMethodList
            isEnabled = enabledList.any { it.packageName == context.packageName }
            val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            isActive = defaultIme?.contains(context.packageName) == true
        }
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isIgnoringBattery = pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        checkStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.groupedBackground)
    ) {
        HIGNavBar(
            title = strings.appTitle,
            largeTitle = true,
            collapseProgress = collapseProgress,
            subtitle = strings.appSubtitle,
            trailingAction = {
                var isSearchPressed by remember { mutableStateOf(false) }
                val searchScale by animateFloatAsState(
                    targetValue = if (isSearchPressed) 0.88f else 1.0f,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "searchScale"
                )
                Box(
                    modifier = Modifier
                        .scale(searchScale)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = if (isSearchPressed) 0.15f else 0.08f))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isSearchPressed = true
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        isSearchPressed = false
                                    }
                                },
                                onTap = { onNavigate(Screen.Search.route) }
                            )
                        }
                        .padding(8.dp)
                ) {
                    HIGSearchIcon(color = colors.accent, size = 20.dp)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // 1. Battery Optimization Banner (PRD §7.14)
            AnimatedVisibility(
                visible = !isIgnoringBattery,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BatteryOptimizationBanner(
                    onDismiss = {
                        scope.launch { preferencesRepository.incrementBatteryReminderCount() }
                    },
                    onRequestExemption = {
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
            }

            // 2. Status Card (PRD §7.3)
            StatusCard(
                isEnabled = isEnabled,
                isActive = isActive,
                temporaryDisabled = settings.temporaryDisabled,
                staggerIndex = 0,
                onToggleChange = { turnOn ->
                    scope.launch {
                        preferencesRepository.updateTemporaryDisabled(!turnOn)
                        if (!turnOn) {
                            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                            imm?.showInputMethodPicker()
                        } else {
                            if (!isEnabled) {
                                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } else if (!isActive) {
                                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showInputMethodPicker()
                            }
                        }
                    }
                },
                onEnable = {
                    val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                },
                onSwitch = {
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showInputMethodPicker()
                }
            )

            // 3. Try Keyboard Section (Interactive live text box)
            HIGGroupedSection(
                header = strings.tryKeyboardHeader,
                footer = strings.tryKeyboardFooter,
                staggerIndex = 1
            ) {
                val animatedBorderColor by animateColorAsState(
                    targetValue = if (isFieldFocused) colors.accent else Color.Transparent,
                    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                    label = "testFieldBorder"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, animatedBorderColor, RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (testInputText.isEmpty()) {
                                Text(
                                    text = strings.tryKeyboardPlaceholder,
                                    style = HIGTheme.typography.body,
                                    color = colors.secondaryLabel
                                )
                            }
                            BasicTextField(
                                value = testInputText,
                                onValueChange = { testInputText = it },
                                textStyle = HIGTheme.typography.body.copy(color = colors.primaryLabel),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                                    .onFocusChanged { isFieldFocused = it.isFocused }
                            )
                        }

                        AnimatedVisibility(
                            visible = testInputText.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            var isClearPressed by remember { mutableStateOf(false) }
                            val clearScale by animateFloatAsState(
                                targetValue = if (isClearPressed) 0.85f else 1.0f,
                                animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                                label = "clearBtnScale"
                            )
                            Box(
                                modifier = Modifier
                                    .scale(clearScale)
                                    .clip(CircleShape)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = {
                                                isClearPressed = true
                                                try {
                                                    tryAwaitRelease()
                                                } finally {
                                                    isClearPressed = false
                                                }
                                            },
                                            onTap = { testInputText = "" }
                                        )
                                    }
                                    .padding(4.dp)
                            ) {
                                HIGClearIcon(color = colors.secondaryLabel)
                            }
                        }
                    }
                }
            }

            // 4. Navigation Settings Sections
            HIGGroupedSection(header = strings.customizationHeader, staggerIndex = 2) {
                HIGRow(
                    title = strings.keyboardThemesTitle,
                    subtitle = strings.keyboardThemesSubtitle,
                    leadingIcon = { HIGSparklesIcon(color = Color(0xFFFF2D55)) },
                    showChevron = true,
                    onClick = { onNavigate(Screen.KeyboardCustomization.route) }
                )
                HIGRow(
                    title = strings.appearanceTitle,
                    subtitle = strings.appearanceSubtitle,
                    leadingIcon = { HIGPaletteIcon(color = colors.accent) },
                    showChevron = true,
                    onClick = { onNavigate(Screen.Appearance.route) }
                )
                HIGRow(
                    title = strings.typingBehaviorTitle,
                    subtitle = strings.typingBehaviorSubtitle,
                    leadingIcon = { HIGBoltIcon(color = Color(0xFFFF9500)) },
                    showChevron = true,
                    onClick = { onNavigate(Screen.TypingBehavior.route) }
                )
                HIGRow(
                    title = strings.gesturesTitle,
                    subtitle = strings.gesturesSubtitle,
                    leadingIcon = { HIGHandTapIcon(color = colors.success) },
                    showChevron = true,
                    onClick = { onNavigate(Screen.Gestures.route) }
                )
                HIGRow(
                    title = strings.personalDictionaryTitle,
                    subtitle = strings.personalDictionarySubtitle,
                    leadingIcon = { HIGBookIcon(color = Color(0xFFAF52DE)) },
                    showChevron = true,
                    showDivider = false,
                    onClick = { onNavigate(Screen.PersonalDictionary.route) }
                )
            }

            HIGGroupedSection(header = strings.systemSupportHeader, staggerIndex = 3) {
                HIGRow(
                    title = strings.generalTitle,
                    subtitle = strings.generalSubtitle,
                    leadingIcon = { HIGGearIcon(color = colors.secondaryLabel) },
                    showChevron = true,
                    onClick = { onNavigate(Screen.General.route) }
                )
                HIGRow(
                    title = strings.aboutTitle,
                    subtitle = strings.aboutSubtitle,
                    leadingIcon = { HIGInfoIcon(color = colors.accent) },
                    showChevron = true,
                    showDivider = false,
                    onClick = { onNavigate(Screen.About.route) }
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    isEnabled: Boolean,
    isActive: Boolean,
    temporaryDisabled: Boolean,
    staggerIndex: Int = 0,
    onToggleChange: (Boolean) -> Unit,
    onEnable: () -> Unit,
    onSwitch: () -> Unit
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current

    val isCurrentlyActive = isActive && !temporaryDisabled

    HIGGroupedSection(header = strings.keyboardStatus) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val statusColor = if (temporaryDisabled) Color(0xFFFF9500)
                else if (isActive) colors.success
                else if (isEnabled) Color(0xFFFF9500)
                else colors.destructive

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (temporaryDisabled) strings.keyboardPausedStatus
                        else if (isActive) strings.statusActive
                        else if (isEnabled) strings.statusEnabled
                        else strings.statusDisabled,
                        style = HIGTheme.typography.headline,
                        color = colors.primaryLabel
                    )
                    Text(
                        text = if (temporaryDisabled) strings.keyboardPausedSub
                        else if (isActive) strings.statusActiveSub
                        else if (isEnabled) strings.statusEnabledSub
                        else strings.statusDisabledSub,
                        style = HIGTheme.typography.footnote,
                        color = colors.secondaryLabel
                    )
                }

                HIGSwitch(
                    checked = isCurrentlyActive,
                    onCheckedChange = { onToggleChange(it) }
                )
            }

            if (!isCurrentlyActive) {
                Spacer(modifier = Modifier.height(14.dp))
                if (!isEnabled) {
                    HIGButton(
                        text = strings.enableInSettings,
                        onClick = onEnable,
                        style = HIGButtonStyle.FILLED,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (temporaryDisabled) {
                    HIGButton(
                        text = strings.enableKeyGlass,
                        onClick = { onToggleChange(true) },
                        style = HIGButtonStyle.FILLED,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    HIGButton(
                        text = strings.switchToKeyGlass,
                        onClick = onSwitch,
                        style = HIGButtonStyle.FILLED,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryOptimizationBanner(
    onDismiss: () -> Unit,
    onRequestExemption: () -> Unit
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFF9500).copy(alpha = 0.15f))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HIGBatteryIcon(color = Color(0xFFFF9500), size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.batteryBannerTitle,
                    style = HIGTheme.typography.headline.copy(fontSize = 15.sp),
                    color = Color(0xFFFF9500),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(4.dp)
                ) {
                    HIGClearIcon(color = colors.secondaryLabel)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = strings.batteryBannerDesc,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel
            )
            Spacer(modifier = Modifier.height(10.dp))
            HIGButton(
                text = strings.disableOptimization,
                onClick = onRequestExemption,
                style = HIGButtonStyle.TINTED,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
