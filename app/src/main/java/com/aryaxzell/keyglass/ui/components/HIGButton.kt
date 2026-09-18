package com.aryaxzell.keyglass.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aryaxzell.keyglass.ui.theme.HIGTheme

enum class HIGButtonStyle {
    FILLED,
    TINTED,
    PLAIN,
    DESTRUCTIVE
}

@Composable
fun HIGButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: HIGButtonStyle = HIGButtonStyle.FILLED,
    enabled: Boolean = true,
    cornerRad: Dp = 10.dp,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "btnScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "btnAlpha"
    )

    val colors = HIGTheme.colors
    val (bgColor, textColor) = when (style) {
        HIGButtonStyle.FILLED -> colors.accent to Color.White
        HIGButtonStyle.TINTED -> colors.accent.copy(alpha = 0.15f) to colors.accent
        HIGButtonStyle.PLAIN -> Color.Transparent to colors.accent
        HIGButtonStyle.DESTRUCTIVE -> colors.destructive.copy(alpha = 0.15f) to colors.destructive
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(cornerRad))
            .background(if (enabled) bgColor.copy(alpha = bgColor.alpha * alpha) else colors.separator)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            try {
                                tryAwaitRelease()
                            } finally {
                                isPressed = false
                            }
                        },
                        onTap = { onClick() }
                    )
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Text(
                text = text,
                style = HIGTheme.typography.headline,
                color = if (enabled) textColor.copy(alpha = textColor.alpha * alpha) else colors.secondaryLabel
            )
        }
    }
}
