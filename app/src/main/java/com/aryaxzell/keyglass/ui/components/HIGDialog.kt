package com.aryaxzell.keyglass.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import kotlinx.coroutines.launch

@Composable
fun HIGDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!show) return

    val colors = HIGTheme.colors
    val scaleAnim = remember { Animatable(0.88f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
                .clip(RoundedCornerShape(14.dp))
                .background(colors.secondaryBackground)
                .padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun HIGAlert(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String = "Cancel",
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    val colors = HIGTheme.colors
    val scope = rememberCoroutineScope()

    val scaleAnim = remember { Animatable(0.85f) }
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    fun dismissWithAnimation(onComplete: () -> Unit) {
        scope.launch {
            launch {
                scaleAnim.animateTo(
                    targetValue = 0.9f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
            launch {
                alphaAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 150)
                )
            }
            onComplete()
        }
    }

    Dialog(onDismissRequest = { dismissWithAnimation(onDismiss) }) {
        Box(
            modifier = Modifier
                .width(270.dp)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
                .clip(RoundedCornerShape(14.dp))
                .background(colors.secondaryBackground)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = HIGTheme.typography.headline.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.primaryLabel,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = message,
                        style = HIGTheme.typography.footnote,
                        color = colors.secondaryLabel,
                        textAlign = TextAlign.Center
                    )
                }

                // Horizontal separator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(colors.separator)
                )

                // Button Row
                Row(modifier = Modifier.fillMaxWidth()) {
                    val cancelSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(interactionSource = cancelSource, indication = null) {
                                dismissWithAnimation(onDismiss)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dismissText,
                            style = HIGTheme.typography.body,
                            color = colors.accent
                        )
                    }

                    // Vertical separator
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(44.dp)
                            .background(colors.separator)
                    )

                    val confirmSource = remember { MutableInteractionSource() }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(interactionSource = confirmSource, indication = null) {
                                dismissWithAnimation(onConfirm)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = confirmText,
                            style = HIGTheme.typography.body.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isDestructive) colors.destructive else colors.accent
                        )
                    }
                }
            }
        }
    }
}
