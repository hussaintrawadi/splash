package com.splash.water.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splash.water.ui.theme.WaterDeep
import com.splash.water.ui.theme.WaterLight
import com.splash.water.ui.theme.WaterMid
import kotlin.math.PI
import kotlin.math.sin

/**
 * The hero visual: a circular "glass" that fills with animated water (two overlapping sine waves)
 * plus a circular progress ring around it. [progress] is 0f..1f.
 */
@Composable
fun WaterOrb(
    progress: Float,
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 260.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "progress",
    )
    val infinite = rememberInfiniteTransition(label = "waves")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "phase",
    )
    val phase2 by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "phase2",
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringBrush = Brush.sweepGradient(listOf(WaterLight, WaterMid, WaterDeep, WaterLight))
    val onWater = Color.White

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = 18.dp.toPx()
            val inset = stroke / 2f + 2.dp.toPx()
            val diameter = this.size.minDimension - inset * 2f
            val topLeft = Offset(inset, inset)
            val arcSize = Size(diameter, diameter)

            // Track ring
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            // Water-filled circle inside the ring
            val waterInset = inset + stroke / 2f + 4.dp.toPx()
            val waterDiameter = this.size.minDimension - waterInset * 2f
            val waterRadius = waterDiameter / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val circle = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        Offset(cx - waterRadius, cy - waterRadius),
                        Size(waterDiameter, waterDiameter),
                    )
                )
            }
            clipPath(circle) {
                // empty backdrop
                drawRect(color = trackColor.copy(alpha = 0.25f))
                if (animatedProgress > 0f) {
                    val top = cy + waterRadius - waterDiameter * animatedProgress
                    val amp = (waterRadius * 0.06f) * fade(animatedProgress)
                    drawWave(cx, cy, waterRadius, top, amp, phase, 1f, WaterMid.copy(alpha = 0.55f))
                    drawWave(cx, cy, waterRadius, top, amp * 0.7f, phase2, 1.4f, WaterDeep)
                }
            }

            // Progress arc on top of the ring
            drawArc(
                brush = ringBrush,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = if (animatedProgress > 0.55f) onWater else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
            )
            Text(
                text = "$currentMl / $goalMl ml",
                color = if (animatedProgress > 0.55f) onWater.copy(alpha = 0.9f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

/** Reduce wave amplitude as the orb nears empty/full so the surface settles. */
private fun fade(p: Float): Float = (1f - (2f * p - 1f) * (2f * p - 1f)).coerceIn(0.15f, 1f)

private fun DrawScope.drawWave(
    cx: Float,
    cy: Float,
    radius: Float,
    surfaceY: Float,
    amplitude: Float,
    phase: Float,
    frequency: Float,
    color: Color,
) {
    val left = cx - radius
    val right = cx + radius
    val bottom = cy + radius
    val path = Path()
    path.moveTo(left, bottom)
    path.lineTo(left, surfaceY)
    val step = 6f
    var x = left
    while (x <= right) {
        val t = (x - left) / (right - left)
        val y = surfaceY + amplitude * sin(t * frequency * 2f * PI.toFloat() + phase)
        path.lineTo(x, y)
        x += step
    }
    path.lineTo(right, bottom)
    path.close()
    drawPath(path, color)
}
