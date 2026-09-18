package com.aryaxzell.keyglass.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.ui.theme.HIGTheme

@Composable
fun HIGNavBar(
    title: String,
    modifier: Modifier = Modifier,
    largeTitle: Boolean = false,
    collapseProgress: Float = 0f,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    backText: String = "Back",
    trailingAction: (@Composable () -> Unit)? = null
) {
    val colors = HIGTheme.colors
    val safeProgress = collapseProgress.coerceIn(0f, 1f)

    val largeTitleAlpha = if (largeTitle) (1f - safeProgress / 0.6f).coerceIn(0f, 1f) else 0f
    val inlineTitleAlpha = if (largeTitle) ((safeProgress - 0.6f) / 0.4f).coerceIn(0f, 1f) else 1f
    val largeTitleFontSize = (34f - (34f - 17f) * safeProgress).sp
    val separatorAlpha = (0.35f + 0.65f * safeProgress).coerceIn(0.35f, 1f)

    // Clear Glass Header without blur filter for clean readability & 60/120fps smooth performance
    val clearGlassBackground = colors.secondaryBackground.copy(alpha = 0.94f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(clearGlassBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Standard Bar (44.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button or Leading
                if (onBackClick != null) {
                    var isPressed by remember { mutableStateOf(false) }
                    val backScale by animateFloatAsState(
                        targetValue = if (isPressed) 0.92f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                        label = "navBackScale"
                    )
                    val backAlpha by animateFloatAsState(
                        targetValue = if (isPressed) 0.6f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                        label = "navBackAlpha"
                    )
                    Row(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = backScale
                                scaleY = backScale
                                alpha = backAlpha
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isPressed = true
                                        try {
                                            tryAwaitRelease()
                                        } finally {
                                            isPressed = false
                                        }
                                    },
                                    onTap = { onBackClick() }
                                )
                            }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HIGChevronLeftIcon(color = colors.accent)
                        Text(
                            text = backText,
                            style = HIGTheme.typography.body,
                            color = colors.accent
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Inline title
                Box(
                    modifier = Modifier.weight(2f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = HIGTheme.typography.headline,
                        color = colors.primaryLabel,
                        maxLines = 1,
                        modifier = Modifier.graphicsLayer { alpha = inlineTitleAlpha }
                    )
                }

                // Trailing Action
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    trailingAction?.invoke()
                }
            }

            // Large Title section
            if (largeTitle && safeProgress < 1f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = largeTitleAlpha
                            translationY = -12.dp.toPx() * safeProgress
                        }
                        .padding(start = 16.dp, end = 16.dp, bottom = (8 * (1f - safeProgress)).dp)
                ) {
                    Column {
                        Text(
                            text = title,
                            style = HIGTheme.typography.largeTitle.copy(fontSize = largeTitleFontSize),
                            color = colors.primaryLabel,
                            maxLines = 1
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = HIGTheme.typography.subhead,
                                color = colors.secondaryLabel
                            )
                        }
                    }
                }
            }

            // Hairline separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.separator.copy(alpha = separatorAlpha))
            )
        }
    }
}

