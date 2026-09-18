package com.aryaxzell.keyglass.ime

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aryaxzell.keyglass.data.datastore.KeyboardBackgroundType
import com.aryaxzell.keyglass.data.datastore.KeyboardPresetTheme
import com.aryaxzell.keyglass.data.datastore.KeyGlassSettings
import com.aryaxzell.keyglass.ui.theme.HIGColorScheme
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import java.io.File

@Composable
fun KeyboardBackgroundRenderer(
    settings: KeyGlassSettings,
    modifier: Modifier = Modifier
) {
    val colors = HIGTheme.colors

    Box(modifier = modifier) {
        // 1. Base Wallpaper / Theme Render
        when (settings.backgroundType) {
            KeyboardBackgroundType.CUSTOM_IMAGE -> {
                if (settings.customBackgroundPath.isNotEmpty()) {
                    val file = remember(settings.customBackgroundPath) { File(settings.customBackgroundPath) }
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = "Custom Keyboard Background",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        DefaultGlassBackground(colors = colors)
                    }
                } else {
                    DefaultGlassBackground(colors = colors)
                }
            }
            KeyboardBackgroundType.PRESET -> {
                when (settings.presetTheme) {
                    KeyboardPresetTheme.GLASS_DARK -> {
                        DefaultDarkGlassBackground()
                    }
                    KeyboardPresetTheme.GLASS_LIGHT -> {
                        DefaultLightGlassBackground()
                    }
                    KeyboardPresetTheme.FOOTBALL -> {
                        FootballPitchBackground()
                    }
                    KeyboardPresetTheme.CYBERPUNK -> {
                        CyberpunkNeonBackground()
                    }
                    KeyboardPresetTheme.SUNSET -> {
                        SunsetSkyBackground()
                    }
                    KeyboardPresetTheme.OCEAN -> {
                        DeepOceanBackground()
                    }
                    KeyboardPresetTheme.EMERALD -> {
                        EmeraldGlassBackground()
                    }
                    KeyboardPresetTheme.AURORA -> {
                        AuroraBorealisBackground()
                    }
                    KeyboardPresetTheme.MIDNIGHT -> {
                        TokyoMidnightBackground()
                    }
                    KeyboardPresetTheme.PASTEL_BLOOM -> {
                        PastelBloomBackground()
                    }
                }
            }
        }

        // 2. Real-time Dimming / Darkness Slider Overlay
        if (settings.backgroundOverlayDim > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settings.backgroundOverlayDim.coerceIn(0f, 0.95f)))
            )
        }
    }
}

@Composable
private fun DefaultGlassBackground(colors: HIGColorScheme) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.keyboardBackground)
    )
}

@Composable
private fun DefaultDarkGlassBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF242426),
                    Color(0xFF1C1C1E),
                    Color(0xFF141416)
                )
            )
        )
    }
}

@Composable
private fun DefaultLightGlassBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFECEFF1),
                    Color(0xFFDCE1E6),
                    Color(0xFFD1D5DB)
                )
            )
        )
    }
}

@Composable
private fun FootballPitchBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Lush grass stripes base gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF104E27),
                    Color(0xFF1A6B37),
                    Color(0xFF14562C)
                )
            )
        )

        // 2. Alternating lawn mower grass stripes
        val stripeCount = 7
        val stripeHeight = h / stripeCount
        for (i in 0 until stripeCount) {
            if (i % 2 == 0) {
                drawRect(
                    color = Color(0xFF227D42).copy(alpha = 0.35f),
                    topLeft = Offset(0f, i * stripeHeight),
                    size = Size(w, stripeHeight)
                )
            }
        }

        // 3. Stadium floodlight glow from top corners
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF9C4).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = w * 0.45f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF9C4).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(w, 0f),
                radius = w * 0.45f
            )
        )

        // 4. White pitch field lines
        val linePaint = Color.White.copy(alpha = 0.45f)
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)

        // Field border
        val margin = 12.dp.toPx()
        drawRoundRect(
            color = linePaint,
            topLeft = Offset(margin, margin),
            size = Size(w - margin * 2, h - margin * 2),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            style = stroke
        )

        // Halfway center line
        val centerY = h / 2f
        drawLine(
            color = linePaint,
            start = Offset(margin, centerY),
            end = Offset(w - margin, centerY),
            strokeWidth = 2.dp.toPx()
        )

        // Center circle & center spot
        drawCircle(
            color = linePaint,
            radius = h * 0.22f,
            center = Offset(w / 2f, centerY),
            style = stroke
        )
        drawCircle(
            color = linePaint,
            radius = 3.dp.toPx(),
            center = Offset(w / 2f, centerY)
        )

        // Top goal area
        drawRect(
            color = linePaint,
            topLeft = Offset(w * 0.3f, margin),
            size = Size(w * 0.4f, h * 0.18f),
            style = stroke
        )

        // Bottom goal area
        drawRect(
            color = linePaint,
            topLeft = Offset(w * 0.3f, h - margin - h * 0.18f),
            size = Size(w * 0.4f, h * 0.18f),
            style = stroke
        )
    }
}

@Composable
private fun CyberpunkNeonBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Dark digital background
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF090314),
                    Color(0xFF140827),
                    Color(0xFF1D053A),
                    Color(0xFF090014)
                )
            )
        )

        // Glowing horizon ambient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF007F).copy(alpha = 0.4f), Color(0xFF00F0FF).copy(alpha = 0.2f), Color.Transparent),
                center = Offset(w / 2f, h * 0.4f),
                radius = w * 0.6f
            )
        )

        // Cyberpunk perspective grid lines
        val gridColor = Color(0xFF00F0FF).copy(alpha = 0.25f)
        val stroke = Stroke(width = 1.dp.toPx())

        // Horizontal lines
        for (i in 1..8) {
            val y = h * (i / 9f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Perspective vertical lines converging slightly
        val cols = 10
        for (i in 0..cols) {
            val startX = (w / cols) * i
            val endX = (startX - w / 2f) * 1.3f + w / 2f
            drawLine(
                color = Color(0xFFFF007F).copy(alpha = 0.2f),
                start = Offset(startX, 0f),
                end = Offset(endX, h),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun SunsetSkyBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Sunset gradient: Indigo -> Violet -> Coral -> Amber Gold
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF18082B),
                    Color(0xFF38104E),
                    Color(0xFF7A1C68),
                    Color(0xFFC83A5A),
                    Color(0xFFF37335),
                    Color(0xFFFDC830)
                )
            )
        )

        // Setting Sun Warm Radial Flare
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFE082).copy(alpha = 0.65f),
                    Color(0xFFFF8A65).copy(alpha = 0.35f),
                    Color.Transparent
                ),
                center = Offset(w / 2f, h * 0.65f),
                radius = w * 0.38f
            )
        )
    }
}

@Composable
private fun DeepOceanBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep oceanic blue
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF021024),
                    Color(0xFF052659),
                    Color(0xFF0A4480),
                    Color(0xFF0D5C9E),
                    Color(0xFF1572B6)
                )
            )
        )

        // Underwater caustic ripples & ray glows
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00E5FF).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.2f, h * 0.25f),
                radius = w * 0.45f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00B0FF).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(w * 0.8f, h * 0.7f),
                radius = w * 0.45f
            )
        )
    }
}

@Composable
private fun EmeraldGlassBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Rich luxurious emerald green
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF022C22),
                    Color(0xFF064E3B),
                    Color(0xFF065F46),
                    Color(0xFF047857),
                    Color(0xFF059669)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        // Crystalline diagonal light reflections
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6EE7B7).copy(alpha = 0.3f), Color.Transparent),
                center = Offset(w * 0.75f, h * 0.3f),
                radius = w * 0.5f
            )
        )
    }
}

@Composable
private fun AuroraBorealisBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Night arctic dark sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF030712),
                    Color(0xFF0B132B),
                    Color(0xFF1C2541),
                    Color(0xFF0A1128)
                )
            )
        )

        // Iridescent Aurora curtains: neon green, electric teal, purple
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00FFA3).copy(alpha = 0.45f), Color(0xFF00E5FF).copy(alpha = 0.25f), Color.Transparent),
                center = Offset(w * 0.3f, h * 0.3f),
                radius = w * 0.55f
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFB5179E).copy(alpha = 0.4f), Color(0xFF7209B7).copy(alpha = 0.2f), Color.Transparent),
                center = Offset(w * 0.75f, h * 0.5f),
                radius = w * 0.5f
            )
        )
    }
}

@Composable
private fun TokyoMidnightBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Tokyo night indigo
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF090D16),
                    Color(0xFF111827),
                    Color(0xFF1E1B4B),
                    Color(0xFF311042)
                )
            )
        )

        // Neon bokeh lights
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.15f, h * 0.7f),
                radius = w * 0.4f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEC4899).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.85f, h * 0.3f),
                radius = w * 0.4f
            )
        )
    }
}

@Composable
private fun PastelBloomBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Soft pastel gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFDFD3),
                    Color(0xFFFFE4E1),
                    Color(0xFFF3E8FF),
                    Color(0xFFE0E7FF),
                    Color(0xFFDCFCE7)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }
}
