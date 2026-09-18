package com.aryaxzell.keyglass.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HIGChevronRightIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8E8E93),
    size: Dp = 14.dp,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.2f)
            lineTo(w * 0.65f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.8f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun HIGChevronLeftIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 18.dp,
    strokeWidth: Dp = 2.5.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.65f, h * 0.2f)
            lineTo(w * 0.35f, h * 0.5f)
            lineTo(w * 0.65f, h * 0.8f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun HIGSearchIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8E8E93),
    size: Dp = 16.dp,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val r = w * 0.32f
        val center = Offset(w * 0.42f, h * 0.42f)
        drawCircle(
            color = color,
            radius = r,
            center = center,
            style = Stroke(width = strokeWidth.toPx())
        )
        drawLine(
            color = color,
            start = Offset(center.x + r * 0.7f, center.y + r * 0.7f),
            end = Offset(w * 0.85f, h * 0.85f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun HIGCheckmarkIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 16.dp,
    strokeWidth: Dp = 2.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.5f)
            lineTo(w * 0.42f, h * 0.75f)
            lineTo(w * 0.82f, h * 0.25f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun HIGClearIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8E8E93),
    size: Dp = 14.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawCircle(color = color.copy(alpha = 0.25f), radius = w / 2f)
        val inset = w * 0.32f
        drawLine(
            color = color,
            start = Offset(inset, inset),
            end = Offset(w - inset, h - inset),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w - inset, inset),
            end = Offset(inset, h - inset),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun HIGTrashIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF3B30),
    size: Dp = 18.dp,
    strokeWidth: Dp = 1.8.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.35f)
            lineTo(w * 0.3f, h * 0.85f)
            lineTo(w * 0.7f, h * 0.85f)
            lineTo(w * 0.75f, h * 0.35f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawLine(
            color = color,
            start = Offset(w * 0.18f, h * 0.35f),
            end = Offset(w * 0.82f, h * 0.35f),
            strokeWidth = strokeWidth.toPx(),
            cap = StrokeCap.Round
        )
        val handle = Path().apply {
            moveTo(w * 0.38f, h * 0.35f)
            lineTo(w * 0.38f, h * 0.22f)
            lineTo(w * 0.62f, h * 0.22f)
            lineTo(w * 0.62f, h * 0.35f)
        }
        drawPath(
            path = handle,
            color = color,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// -------------------------------------------------------------
// Custom HIG Vector Icons replacing all emojis in the standalone app
// -------------------------------------------------------------

@Composable
fun HIGPaletteIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Palette outline
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.1f)
            cubicTo(w * 0.85f, h * 0.1f, w * 0.95f, h * 0.4f, w * 0.9f, h * 0.65f)
            cubicTo(w * 0.86f, h * 0.85f, w * 0.7f, h * 0.9f, w * 0.55f, h * 0.88f)
            cubicTo(w * 0.45f, h * 0.86f, w * 0.42f, h * 0.72f, w * 0.35f, h * 0.72f)
            cubicTo(w * 0.28f, h * 0.72f, w * 0.22f, h * 0.82f, w * 0.14f, h * 0.75f)
            cubicTo(w * 0.05f, h * 0.66f, w * 0.05f, h * 0.45f, w * 0.15f, h * 0.25f)
            cubicTo(w * 0.25f, h * 0.1f, w * 0.4f, h * 0.1f, w * 0.5f, h * 0.1f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Little color blotches
        val dotRadius = w * 0.07f
        drawCircle(color = color, radius = dotRadius, center = Offset(w * 0.38f, h * 0.32f), style = Fill)
        drawCircle(color = color, radius = dotRadius, center = Offset(w * 0.62f, h * 0.32f), style = Fill)
        drawCircle(color = color, radius = dotRadius, center = Offset(w * 0.75f, h * 0.52f), style = Fill)
        drawCircle(color = color, radius = dotRadius * 1.3f, center = Offset(w * 0.36f, h * 0.58f), style = Stroke(width = 1.4.dp.toPx()))
    }
}

@Composable
fun HIGBoltIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF9500),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.55f, h * 0.1f)
            lineTo(w * 0.25f, h * 0.52f)
            lineTo(w * 0.48f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.9f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.55f, h * 0.44f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HIGHandTapIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF34C759),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            // Index finger pointing up
            moveTo(w * 0.42f, h * 0.45f)
            lineTo(w * 0.42f, h * 0.16f)
            cubicTo(w * 0.42f, h * 0.1f, w * 0.54f, h * 0.1f, w * 0.54f, h * 0.16f)
            lineTo(w * 0.54f, h * 0.42f)
            // Middle finger folded
            lineTo(w * 0.65f, h * 0.42f)
            cubicTo(w * 0.72f, h * 0.42f, w * 0.72f, h * 0.52f, w * 0.65f, h * 0.52f)
            // Ring finger
            lineTo(w * 0.72f, h * 0.52f)
            cubicTo(w * 0.78f, h * 0.52f, w * 0.78f, h * 0.62f, w * 0.72f, h * 0.62f)
            // Hand palm down to wrist
            lineTo(w * 0.72f, h * 0.8f)
            cubicTo(w * 0.72f, h * 0.88f, w * 0.3f, h * 0.88f, w * 0.3f, h * 0.8f)
            // Thumb
            lineTo(w * 0.3f, h * 0.6f)
            lineTo(w * 0.22f, h * 0.54f)
            cubicTo(w * 0.16f, h * 0.48f, w * 0.24f, h * 0.4f, w * 0.32f, h * 0.46f)
            lineTo(w * 0.42f, h * 0.55f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HIGHandGestureIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF34C759),
    size: Dp = 20.dp
) {
    HIGHandTapIcon(modifier = modifier, color = color, size = size)
}

@Composable
fun HIGBookIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFAF52DE),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Left page
        val leftPage = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.4f, h * 0.2f, w * 0.25f, h * 0.22f, w * 0.15f, h * 0.26f)
            lineTo(w * 0.15f, h * 0.78f)
            cubicTo(w * 0.25f, h * 0.74f, w * 0.4f, h * 0.72f, w * 0.5f, h * 0.78f)
            close()
        }
        drawPath(
            path = leftPage,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Right page
        val rightPage = Path().apply {
            moveTo(w * 0.5f, h * 0.25f)
            cubicTo(w * 0.6f, h * 0.2f, w * 0.75f, h * 0.22f, w * 0.85f, h * 0.26f)
            lineTo(w * 0.85f, h * 0.78f)
            cubicTo(w * 0.75f, h * 0.74f, w * 0.6f, h * 0.72f, w * 0.5f, h * 0.78f)
            close()
        }
        drawPath(
            path = rightPage,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Spine
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.25f),
            end = Offset(w * 0.5f, h * 0.78f),
            strokeWidth = 1.6.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun HIGGearIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8E8E93),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val r = w * 0.32f
        // Outer cog ring
        drawCircle(
            color = color,
            radius = r,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        // Inner hole
        drawCircle(
            color = color,
            radius = r * 0.42f,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // 6 teeth
        for (i in 0 until 6) {
            val angle = (i * 60f) * (Math.PI / 180f).toFloat()
            val x1 = center.x + (r * 0.9f) * Math.cos(angle.toDouble()).toFloat()
            val y1 = center.y + (r * 0.9f) * Math.sin(angle.toDouble()).toFloat()
            val x2 = center.x + (r * 1.25f) * Math.cos(angle.toDouble()).toFloat()
            val y2 = center.y + (r * 1.25f) * Math.sin(angle.toDouble()).toFloat()
            drawLine(
                color = color,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun HIGInfoIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val r = w * 0.42f
        drawCircle(
            color = color,
            radius = r,
            center = center,
            style = Stroke(width = 1.6.dp.toPx())
        )
        // Dot
        drawCircle(
            color = color,
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.32f),
            style = Fill
        )
        // Stem
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.45f),
            end = Offset(w * 0.5f, h * 0.7f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Base serif
        drawLine(
            color = color,
            start = Offset(w * 0.42f, h * 0.7f),
            end = Offset(w * 0.58f, h * 0.7f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun HIGKeyboardIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Keyboard body frame
        val frameRect = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.1f,
                    top = h * 0.22f,
                    right = w * 0.9f,
                    bottom = h * 0.78f,
                    radiusX = 3.dp.toPx(),
                    radiusY = 3.dp.toPx()
                )
            )
        }
        drawPath(path = frameRect, color = color, style = Stroke(width = 1.6.dp.toPx()))
        // Key grid dots/dashes
        // Row 1 keys
        val keyW = w * 0.08f
        val keyH = h * 0.06f
        for (i in 0..4) {
            val kx = w * 0.22f + i * (w * 0.13f)
            drawRoundRect(
                color = color,
                topLeft = Offset(kx, h * 0.34f),
                size = Size(keyW, keyH),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
        // Row 2 keys
        for (i in 0..4) {
            val kx = w * 0.22f + i * (w * 0.13f)
            drawRoundRect(
                color = color,
                topLeft = Offset(kx, h * 0.47f),
                size = Size(keyW, keyH),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
        // Space bar
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.32f, h * 0.61f),
            size = Size(w * 0.36f, keyH),
            cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
        )
    }
}

@Composable
fun HIGBatteryIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF9500),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Battery case
        val casePath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = w * 0.12f,
                    top = h * 0.26f,
                    right = w * 0.78f,
                    bottom = h * 0.74f,
                    radiusX = 2.5.dp.toPx(),
                    radiusY = 2.5.dp.toPx()
                )
            )
        }
        drawPath(path = casePath, color = color, style = Stroke(width = 1.6.dp.toPx()))
        // Positive terminal nipple
        val nipple = Path().apply {
            moveTo(w * 0.78f, h * 0.4f)
            lineTo(w * 0.86f, h * 0.43f)
            lineTo(w * 0.86f, h * 0.57f)
            lineTo(w * 0.78f, h * 0.6f)
        }
        drawPath(path = nipple, color = color, style = Fill)
        // Energy charge level (half full)
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.34f),
            size = Size(w * 0.32f, h * 0.32f),
            cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
        )
    }
}

@Composable
fun HIGSparkleIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        // 4-point sparkle star
        val star = Path().apply {
            moveTo(center.x, h * 0.1f)
            cubicTo(center.x + w * 0.05f, center.y - h * 0.08f, center.x + w * 0.08f, center.y - h * 0.05f, w * 0.9f, center.y)
            cubicTo(center.x + w * 0.08f, center.y + h * 0.05f, center.x + w * 0.05f, center.y + h * 0.08f, center.x, h * 0.9f)
            cubicTo(center.x - w * 0.05f, center.y + h * 0.08f, center.x - w * 0.08f, center.y + h * 0.05f, w * 0.1f, center.y)
            cubicTo(center.x - w * 0.08f, center.y - h * 0.05f, center.x - w * 0.05f, center.y - h * 0.08f, center.x, h * 0.1f)
            close()
        }
        drawPath(path = star, color = color, style = Fill)
    }
}

@Composable
fun HIGSparklesIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 22.dp
) {
    HIGSparkleIcon(modifier = modifier, color = color, size = size)
}

@Composable
fun HIGLockIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF8E8E93),
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Shackle
        val shackle = Path().apply {
            moveTo(w * 0.32f, h * 0.45f)
            lineTo(w * 0.32f, h * 0.28f)
            cubicTo(w * 0.32f, h * 0.12f, w * 0.68f, h * 0.12f, w * 0.68f, h * 0.28f)
            lineTo(w * 0.68f, h * 0.45f)
        }
        drawPath(path = shackle, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
        // Body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.45f),
            size = Size(w * 0.56f, h * 0.46f),
            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx())
        )
        // Keyhole
        drawCircle(
            color = Color.White,
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.62f)
        )
    }
}

@Composable
fun HIGSmileyIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w * 0.5f, h * 0.5f)
        val r = w * 0.42f
        // Face outline
        drawCircle(color = color, radius = r, center = center, style = Stroke(width = 1.6.dp.toPx()))
        // Eyes
        drawCircle(color = color, radius = 1.2.dp.toPx(), center = Offset(w * 0.36f, h * 0.38f))
        drawCircle(color = color, radius = 1.2.dp.toPx(), center = Offset(w * 0.64f, h * 0.38f))
        // Smile curve
        val smile = Path().apply {
            moveTo(w * 0.32f, h * 0.58f)
            cubicTo(w * 0.38f, h * 0.72f, w * 0.62f, h * 0.72f, w * 0.68f, h * 0.58f)
        }
        drawPath(path = smile, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun HIGPhotoIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Outer rounded picture frame
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.1f, h * 0.15f),
            size = Size(w * 0.8f, h * 0.7f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = 1.6.dp.toPx())
        )
        // Sun/Moon circle
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = Offset(w * 0.32f, h * 0.38f)
        )
        // Mountains path
        val mountains = Path().apply {
            moveTo(w * 0.15f, h * 0.75f)
            lineTo(w * 0.42f, h * 0.5f)
            lineTo(w * 0.58f, h * 0.65f)
            lineTo(w * 0.72f, h * 0.55f)
            lineTo(w * 0.85f, h * 0.75f)
        }
        drawPath(
            path = mountains,
            color = color,
            style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HIGKeyboardGlyphIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF),
    size: Dp = 36.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        // Rounded keyboard frame
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.08f, h * 0.18f),
            size = Size(w * 0.84f, h * 0.64f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )
        // Key dots row 1
        for (i in 0..4) {
            val cx = w * (0.24f + i * 0.13f)
            val cy = h * 0.36f
            drawCircle(color = color, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
        }
        // Key dots row 2
        for (i in 0..3) {
            val cx = w * (0.30f + i * 0.13f)
            val cy = h * 0.52f
            drawCircle(color = color, radius = 1.8.dp.toPx(), center = Offset(cx, cy))
        }
        // Spacebar line
        drawLine(
            color = color,
            start = Offset(w * 0.32f, h * 0.68f),
            end = Offset(w * 0.68f, h * 0.68f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
