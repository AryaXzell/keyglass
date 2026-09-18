package com.aryaxzell.keyglass.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS-style edge swipe back gesture detector.
 * Only triggers if the touch starts within [edgeThreshold] from the start (left) edge
 * and is dragged rightwards past [swipeThreshold].
 */
fun Modifier.higEdgeSwipeBack(
    enabled: Boolean = true,
    edgeThreshold: Dp = 36.dp,
    swipeThreshold: Dp = 80.dp,
    onBack: () -> Unit
): Modifier = if (!enabled) this else pointerInput(Unit) {
    val edgeThresholdPx = edgeThreshold.toPx()
    val swipeThresholdPx = swipeThreshold.toPx()

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.position.x <= edgeThresholdPx) {
            var totalDragX = 0f
            var triggered = false

            drag(down.id) { change: PointerInputChange ->
                val dragAmount = change.positionChange().x
                if (dragAmount > 0) {
                    totalDragX += dragAmount
                    change.consume()
                    if (totalDragX >= swipeThresholdPx && !triggered) {
                        triggered = true
                        onBack()
                    }
                }
            }
        }
    }
}
