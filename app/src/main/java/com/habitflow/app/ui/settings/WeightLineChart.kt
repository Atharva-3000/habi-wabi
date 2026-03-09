package com.habitflow.app.ui.settings

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.*

private val WeightLineColor = Color(0xFF7B68EE)

/**
 * Smooth Bezier-curve weight history chart.
 * – Cubic spline interpolation (S-curves between points)
 * – Left-to-right draw-in animation (900 ms EaseOut)
 * – Vertical gradient fill under the line (purple → transparent)
 * – Pulsing ring on the latest data point
 * – Dashed horizontal grid lines + Y / X axis labels
 */
@Composable
fun WeightLineChart(
    points: List<WeightPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        EmptyChartPlaceholder(modifier, "Log your weight to see the trend")
        return
    }
    if (points.size == 1) {
        SinglePointChart(modifier, points.first().kg)
        return
    }

    val minKg      = points.minOf { it.kg }
    val maxKg      = points.maxOf { it.kg }
    val rangeKg    = (maxKg - minKg).coerceAtLeast(0.5f)
    val paddedMin  = minKg - rangeKg * 0.15f
    val paddedMax  = maxKg + rangeKg * 0.15f
    val paddedRange = paddedMax - paddedMin

    // Draw-in animation
    val drawProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "chart_draw"
    )

    // Pulsing dot on latest point
    val inf = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by inf.animateFloat(
        0.35f, 0f,
        infiniteRepeatable(tween(1200, easing = EaseOutCubic), RepeatMode.Restart),
        label = "pa"
    )
    val pulseScale by inf.animateFloat(
        1f, 2.6f,
        infiniteRepeatable(tween(1200, easing = EaseOutCubic), RepeatMode.Restart),
        label = "ps"
    )

    val yLabels    = listOf(paddedMax, (paddedMax + paddedMin) / 2f, paddedMin)
    val yAxisWidth = 40.dp
    val xAxisH     = 20.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .padding(top = 14.dp, start = 4.dp, end = 12.dp)
    ) {
        Row(Modifier.fillMaxSize()) {
            // ── Y labels ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .width(yAxisWidth)
                    .fillMaxHeight()
                    .padding(bottom = xAxisH),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yLabels.forEach { v ->
                    Text(
                        "%.1f".format(v),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextTertiary.copy(alpha = 0.6f),
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Spacer(Modifier.width(4.dp))

            // ── Chart + X labels ─────────────────────────────────────────────
            Column(Modifier.weight(1f).fillMaxHeight()) {
                // Canvas
                Canvas(Modifier.fillMaxWidth().weight(1f)) {
                    val w = size.width
                    val h = size.height
                    val n = points.size

                    fun xOf(i: Int) = i.toFloat() / (n - 1).toFloat() * w
                    fun yOf(kg: Float) = h - (kg - paddedMin) / paddedRange * h

                    val pts = points.mapIndexed { i, p -> Offset(xOf(i), yOf(p.kg)) }

                    // Dashed horizontal grid lines
                    listOf(0.25f, 0.5f, 0.75f).forEach { frac ->
                        drawLine(
                            color = Divider.copy(alpha = 0.45f),
                            start = Offset(0f, h * (1f - frac)),
                            end   = Offset(w, h * (1f - frac)),
                            strokeWidth = 0.7f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    }

                    // Build cubic Bezier path
                    val linePath = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val cx = (pts[i - 1].x + pts[i].x) / 2f
                            cubicTo(cx, pts[i - 1].y, cx, pts[i].y, pts[i].x, pts[i].y)
                        }
                    }

                    // Apply left-to-right draw animation via manual canvas clip
                    drawContext.canvas.save()
                    drawContext.canvas.clipRect(
                        androidx.compose.ui.geometry.Rect(0f, 0f, w * drawProgress, h)
                    )

                    // Gradient fill under line
                    drawPath(
                        path = Path().apply {
                            addPath(linePath)
                            lineTo(pts.last().x, h)
                            lineTo(pts.first().x, h)
                            close()
                        },
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WeightLineColor.copy(alpha = 0.32f),
                                WeightLineColor.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = 0f, endY = h
                        )
                    )
                    // Gradient stroke
                    drawPath(
                        path = linePath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                WeightLineColor.copy(alpha = 0.6f),
                                WeightLineColor,
                                WeightLineColor.copy(alpha = 0.85f)
                            ),
                            start = Offset(0f, 0f),
                            end   = Offset(w, h)
                        ),
                        style = Stroke(
                            width = 2.6.dp.toPx(),
                            cap   = StrokeCap.Round,
                            join  = StrokeJoin.Round
                        )
                    )

                    drawContext.canvas.restore()

                    // Pulsing ring + dot on latest point
                    if (drawProgress >= 0.99f) {
                        val last  = pts.last()
                        val dotR  = 5.dp.toPx()
                        drawCircle(WeightLineColor.copy(alpha = pulseAlpha), dotR * pulseScale, last)
                        drawCircle(Background, dotR + 2f.dp.toPx(), last)
                        drawCircle(WeightLineColor, dotR, last)
                    }
                }

                // X-axis date labels — first / mid / last
                val labelIdxs = listOf(0, points.size / 2, points.size - 1).distinct()
                Box(Modifier.fillMaxWidth().height(xAxisH)) {
                    labelIdxs.forEach { idx ->
                        val frac = idx.toFloat() / (points.size - 1).toFloat()
                        val align = when (idx) {
                            0               -> Alignment.CenterStart
                            points.size - 1 -> Alignment.CenterEnd
                            else            -> Alignment.Center
                        }
                        Box(Modifier.fillMaxSize(), contentAlignment = align) {
                            Text(
                                points[idx].dateLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = TextTertiary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(modifier: Modifier, message: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Text("〜", fontSize = 32.sp, color = TextTertiary.copy(alpha = 0.2f))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = TextTertiary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SinglePointChart(modifier: Modifier, kg: Float) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("%.1f".format(kg), style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold), color = WeightLineColor)
            Text("kg · log more days to see your trend", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}
