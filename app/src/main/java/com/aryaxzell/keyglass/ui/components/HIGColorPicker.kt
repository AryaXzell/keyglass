package com.aryaxzell.keyglass.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryaxzell.keyglass.ui.theme.ACCENT_PRESETS
import com.aryaxzell.keyglass.ui.theme.HIGTheme
import com.aryaxzell.keyglass.ui.theme.parseHexColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HIGColorPicker(
    selectedHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = HIGTheme.colors

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ACCENT_PRESETS.forEach { (hex, name) ->
            val isSelected = selectedHex.equals(hex, ignoreCase = true)
            val swatchColor = parseHexColor(hex)

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium),
                label = "colorSwatchScale"
            )

            val interactionSource = remember { MutableInteractionSource() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .then(
                            if (isSelected) {
                                Modifier.border(2.5.dp, Color.White, CircleShape)
                            } else Modifier
                        )
                        .clickable(interactionSource = interactionSource, indication = null) {
                            onColorSelected(hex)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        HIGCheckmarkIcon(color = Color.White, size = 14.dp)
                    }
                }

                Text(
                    text = name,
                    style = HIGTheme.typography.caption2.copy(fontSize = 10.sp),
                    color = if (isSelected) colors.primaryLabel else colors.secondaryLabel
                )
            }
        }
    }
}
