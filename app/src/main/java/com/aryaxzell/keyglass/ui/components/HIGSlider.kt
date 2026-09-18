package com.aryaxzell.keyglass.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.aryaxzell.keyglass.ui.theme.HIGTheme

@Composable
fun HIGSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true
) {
    val colors = HIGTheme.colors
    val density = LocalDensity.current.density

    var localValue by remember(value) { mutableFloatStateOf(value) }
    var isPressed by remember { mutableStateOf(false) }
    var lastEmitTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(value) {
        if (!isPressed) {
            localValue = value
        }
    }

    val normalizedTarget = ((localValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)

    // Spring animation for thumb position (smooth slide on tap-to-jump)
    val animatedNormValue by animateFloatAsState(
        targetValue = normalizedTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sliderThumbOffset"
    )

    // Scale feedback on thumb when pressed
    val thumbScale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "sliderThumbScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val thumbSize = 28.dp
        val availableTrackWidthPx = constraints.maxWidth.toFloat()
        val trackHeight = 6.dp

        // Background Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(if (colors.isDark) Color(0xFF38383A) else Color(0xFFE5E5EA))
        )

        // Active Track
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedNormValue)
                .height(trackHeight)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.accent)
        )

        // Draggable Thumb with Spring Physics & Scale
        val maxOffsetPx = (availableTrackWidthPx - thumbSize.value * density).coerceAtLeast(0f)
        val thumbOffsetPx = (animatedNormValue * maxOffsetPx).coerceAtLeast(0f)
        val thumbOffsetDp = (thumbOffsetPx / density).dp

        Box(
            modifier = Modifier
                .offset(x = thumbOffsetDp)
                .size(thumbSize)
                .scale(thumbScale)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        )

        // Touch handling layer with debounced persistence
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .pointerInput(enabled, valueRange) {
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
                            onTap = { offset ->
                                val newNorm = (offset.x / availableTrackWidthPx).coerceIn(0f, 1f)
                                val newVal = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                                localValue = newVal
                                onValueChange(newVal)
                            }
                        )
                    }
                }
                .pointerInput(enabled, valueRange) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isPressed = true
                                val newNorm = (offset.x / availableTrackWidthPx).coerceIn(0f, 1f)
                                val newVal = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                                localValue = newVal
                            },
                            onDragEnd = {
                                isPressed = false
                                onValueChange(localValue)
                            },
                            onDragCancel = {
                                isPressed = false
                                onValueChange(localValue)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val newNorm = (change.position.x / availableTrackWidthPx).coerceIn(0f, 1f)
                                val newVal = valueRange.start + newNorm * (valueRange.endInclusive - valueRange.start)
                                localValue = newVal

                                // Throttle persistence calls to DataStore (≥100ms apart during active dragging)
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastEmitTime >= 100L) {
                                    lastEmitTime = currentTime
                                    onValueChange(newVal)
                                }
                            }
                        )
                    }
                }
        )
    }
}
