package com.aryaxzell.keyglass.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aryaxzell.keyglass.ui.components.HIGButton
import com.aryaxzell.keyglass.ui.components.HIGButtonStyle
import com.aryaxzell.keyglass.ui.components.HIGCheckmarkIcon
import com.aryaxzell.keyglass.ui.components.HIGGearIcon
import com.aryaxzell.keyglass.ui.components.HIGKeyboardIcon
import com.aryaxzell.keyglass.ui.components.HIGLockIcon
import com.aryaxzell.keyglass.ui.components.HIGSparkleIcon
import com.aryaxzell.keyglass.ui.localization.LocalStrings
import com.aryaxzell.keyglass.ui.theme.HIGTheme

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    var currentStep by remember { mutableIntStateOf(1) }

    var isEnabled by remember { mutableStateOf(false) }
    var isActive by remember { mutableStateOf(false) }

    fun checkImeStatus() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            val enabledList = imm.enabledInputMethodList
            isEnabled = enabledList.any { it.packageName == context.packageName }
            val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            isActive = defaultIme?.contains(context.packageName) == true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkImeStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        checkImeStatus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step indicator dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        ) {
            for (step in 1..3) {
                val isCurrent = step == currentStep
                val isCompleted = step < currentStep
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCurrent) colors.accent
                            else if (isCompleted) colors.success
                            else colors.separator
                        )
                )
            }
        }

        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f)
        ) { step ->
            when (step) {
                1 -> StepWelcome()
                2 -> StepEnable(
                    isEnabled = isEnabled,
                    onEnableClick = {
                        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )
                3 -> StepActivate(
                    isActive = isActive,
                    onSwitchClick = {
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.showInputMethodPicker()
                    }
                )
            }
        }

        // Bottom action button
        Spacer(modifier = Modifier.height(16.dp))
        when (currentStep) {
            1 -> {
                HIGButton(
                    text = strings.getStartedBtn,
                    onClick = { currentStep = 2 },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            2 -> {
                HIGButton(
                    text = if (isEnabled) "Next" else "Skip to Next Step",
                    onClick = { currentStep = 3 },
                    modifier = Modifier.fillMaxWidth(),
                    style = if (isEnabled) HIGButtonStyle.FILLED else HIGButtonStyle.TINTED
                )
            }
            3 -> {
                HIGButton(
                    text = if (isActive) "Start Typing" else "Complete Setup",
                    onClick = { onFinishOnboarding() },
                    modifier = Modifier.fillMaxWidth(),
                    style = HIGButtonStyle.FILLED
                )
            }
        }
    }
}

@Composable
private fun StepWelcome() {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(colors.secondaryBackground),
            contentAlignment = Alignment.Center
        ) {
            HIGKeyboardIcon(color = colors.accent, size = 48.dp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = strings.welcomeTitle,
            style = HIGTheme.typography.largeTitle,
            color = colors.primaryLabel,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = strings.welcomeDesc,
            style = HIGTheme.typography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun StepEnable(
    isEnabled: Boolean,
    onEnableClick: () -> Unit
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isEnabled) colors.success.copy(alpha = 0.15f) else colors.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (isEnabled) {
                HIGCheckmarkIcon(color = colors.success, size = 36.dp)
            } else {
                HIGGearIcon(color = colors.accent, size = 36.dp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isEnabled) strings.statusEnabled else strings.step1Title,
            style = HIGTheme.typography.title1,
            color = colors.primaryLabel,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = strings.step1Desc,
            style = HIGTheme.typography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        HIGButton(
            text = if (isEnabled) "Enabled in Settings" else strings.enableInSettings,
            onClick = onEnableClick,
            style = if (isEnabled) HIGButtonStyle.TINTED else HIGButtonStyle.FILLED,
            modifier = Modifier.fillMaxWidth(0.85f)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.secondaryBackground)
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HIGLockIcon(color = colors.primaryLabel, size = 16.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Privacy Notice",
                        style = HIGTheme.typography.headline.copy(fontSize = 14.sp),
                        color = colors.primaryLabel
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Android displays a standard security prompt when enabling any third-party keyboard. KeyGlass never collects, stores, or transmits your keystrokes. All predictive text runs 100% locally on your device.",
                    style = HIGTheme.typography.footnote,
                    color = colors.secondaryLabel
                )
            }
        }
    }
}

@Composable
private fun StepActivate(
    isActive: Boolean,
    onSwitchClick: () -> Unit
) {
    val colors = HIGTheme.colors
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(if (isActive) colors.success.copy(alpha = 0.15f) else colors.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                HIGCheckmarkIcon(color = colors.success, size = 36.dp)
            } else {
                HIGSparkleIcon(color = colors.accent, size = 36.dp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isActive) strings.statusActive else strings.step2Title,
            style = HIGTheme.typography.title1,
            color = colors.primaryLabel,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = strings.step2Desc,
            style = HIGTheme.typography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        HIGButton(
            text = if (isActive) "Active Keyboard" else strings.switchToKeyGlass,
            onClick = onSwitchClick,
            style = if (isActive) HIGButtonStyle.TINTED else HIGButtonStyle.FILLED,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
    }
}
