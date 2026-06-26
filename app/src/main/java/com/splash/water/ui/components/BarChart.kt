package com.splash.water.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splash.water.ui.theme.MintGreen
import com.splash.water.ui.theme.WaterDeep
import com.splash.water.ui.theme.WaterLight
import com.splash.water.ui.theme.WaterMid
import androidx.compose.ui.graphics.Color
import kotlin.math.max

data class BarPoint(val label: String, val value: Int, val highlight: Boolean = false)

/**
 * A playful rounded bar chart. Bars that reach the daily goal are tinted green so goal-met days
 * are obvious at a glance, no separate goal line needed. Pure Canvas, no chart dependency.
 */
@Composable
fun BarChart(
    bars: List<BarPoint>,
    goalMl: Int,
    modifier: Modifier = Modifier,
) {
    val maxValue = max(bars.maxOfOrNull { it.value } ?: 0, goalMl).coerceAtLeast(1)
    // Under-goal days flow blue; goal-met days turn a celebratory green.
    val underBrush = Brush.verticalGradient(listOf(WaterLight, WaterMid, WaterDeep))
    val metBrush = Brush.verticalGradient(listOf(Color(0xFF7BE5B0), MintGreen, Color(0xFF0E9E6E)))
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp),
        ) {
            if (bars.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val n = bars.size
            val slot = w / n
            val barWidth = (slot * 0.55f).coerceAtMost(34.dp.toPx())

            bars.forEachIndexed { i, bar ->
                val cx = slot * i + slot / 2f
                val barH = (bar.value.toFloat() / maxValue) * h
                val top = h - barH
                val met = goalMl > 0 && bar.value >= goalMl
                // track
                drawRoundRect(
                    color = gridColor,
                    topLeft = Offset(cx - barWidth / 2f, 0f),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
                if (barH > 0f) {
                    drawRoundRect(
                        brush = if (met) metBrush else underBrush,
                        topLeft = Offset(cx - barWidth / 2f, top),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(barWidth / 2f),
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    modifier = Modifier.weight(1f),
                    color = if (bar.highlight) MaterialTheme.colorScheme.primary else labelColor,
                    fontWeight = if (bar.highlight) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}
