package com.aryaxzell.keyglass.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.ui.theme.HIGTheme

@Composable
fun HIGGroupedSection(
    modifier: Modifier = Modifier,
    header: String? = null,
    footer: String? = null,
    staggerIndex: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HIGTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (header != null) {
            Text(
                text = header.uppercase(),
                style = HIGTheme.typography.footnote.copy(fontSize = 12.sp),
                color = colors.secondaryLabel,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.groupedCard)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }

        if (footer != null) {
            Text(
                text = footer,
                style = HIGTheme.typography.footnote,
                color = colors.secondaryLabel,
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun HIGRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    value: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingAccessory: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    showDivider: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val colors = HIGTheme.colors
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "rowPressScale"
    )

    val pressAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.12f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "rowPressAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primaryLabel.copy(alpha = pressAlpha))
                .then(
                    if (onClick != null && enabled) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    try {
                                        tryAwaitRelease()
                                    } finally {
                                        isPressed = false
                                    }
                                },
                                onTap = { onClick() }
                            )
                        }
                    } else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 44.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = HIGTheme.typography.body,
                        color = if (enabled) colors.primaryLabel else colors.secondaryLabel
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = HIGTheme.typography.subhead,
                            color = colors.secondaryLabel
                        )
                    }
                }

                if (value != null) {
                    Text(
                        text = value,
                        style = HIGTheme.typography.body,
                        color = colors.secondaryLabel,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                if (trailingAccessory != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingAccessory()
                } else if (showChevron) {
                    Spacer(modifier = Modifier.width(8.dp))
                    HIGChevronRightIcon(color = colors.secondaryLabel)
                }
            }
        }

        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .padding(start = if (leadingIcon != null) 48.dp else 16.dp)
                    .background(colors.separator)
            )
        }
    }
}

