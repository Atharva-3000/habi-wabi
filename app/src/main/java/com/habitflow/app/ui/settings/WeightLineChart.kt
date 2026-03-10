package com.habitflow.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.*
import kotlin.math.abs

private val WeightLineColor = Color(0xFF7B68EE)

@Composable
fun WeightLineChart(
    points: List<WeightPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        EmptyChartPlaceholder(modifier, "Log your weight to see the trend")
        return
    }

    val minKg      = points.minOf { it.kg }
    val maxKg      = points.maxOf { it.kg }
    // If all points are same value (or just 1 point), range is 0. Give it a visual range of 4kg (±2kg)
    val rangeKg    = if (points.size == 1 || maxKg == minKg) 4f else (maxKg - minKg).coerceAtLeast(0.5f)
    val paddedMin  = if (points.size == 1) minKg - 2f else minKg - rangeKg * 0.15f
    val paddedMax  = if (points.size == 1) maxKg + 2f else maxKg + rangeKg * 0.15f
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

    val density = androidx.compose.ui.platform.LocalDensity.current

    val yLabels    = listOf(paddedMax, (paddedMax + paddedMin) / 2f, paddedMin)
    val yAxisWidth = 40.dp
    val xAxisH     = 20.dp

    // Interaction state
    var selectedPoint by remember { mutableStateOf<WeightPoint?>(null) }
    var selectedX by remember { mutableStateOf(0f) }
    var selectedY by remember { mutableStateOf(0f) }

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
                Box(Modifier.fillMaxWidth().weight(1f)) {
                    Canvas(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        if (points.isNotEmpty()) {
                                            val w = size.width.toFloat()
                                            val xPositions = points.indices.map {
                                                if (points.size == 1) w / 2f else it.toFloat() / (points.size - 1).toFloat() * w
                                            }
                                            // Find closest X
                                            val closestIdx = xPositions.indexOfMinBy { abs(it - offset.x) }
                                            if (closestIdx != -1) {
                                                selectedPoint = points[closestIdx]
                                                selectedX = xPositions[closestIdx]
                                                selectedY = size.height.toFloat() - (points[closestIdx].kg - paddedMin) / paddedRange * size.height.toFloat()
                                            }
                                        }
                                        tryAwaitRelease()
                                        selectedPoint = null
                                    }
                                )
                            }
                            .pointerInput(points) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (points.isNotEmpty()) {
                                            val w = size.width.toFloat()
                                            val xPositions = points.indices.map {
                                                if (points.size == 1) w / 2f else it.toFloat() / (points.size - 1).toFloat() * w
                                            }
                                            val closestIdx = xPositions.indexOfMinBy { abs(it - offset.x) }
                                            if (closestIdx != -1) {
                                                selectedPoint = points[closestIdx]
                                                selectedX = xPositions[closestIdx]
                                                selectedY = size.height.toFloat() - (points[closestIdx].kg - paddedMin) / paddedRange * size.height.toFloat()
                                            }
                                        }
                                    },
                                    onDragEnd = { selectedPoint = null },
                                    onDragCancel = { selectedPoint = null },
                                    onDrag = { change, _ ->
                                        if (points.isNotEmpty()) {
                                            val w = size.width.toFloat()
                                            val xPositions = points.indices.map {
                                                if (points.size == 1) w / 2f else it.toFloat() / (points.size - 1).toFloat() * w
                                            }
                                            val closestIdx = xPositions.indexOfMinBy { abs(it - change.position.x) }
                                            if (closestIdx != -1) {
                                                selectedPoint = points[closestIdx]
                                                selectedX = xPositions[closestIdx]
                                                selectedY = size.height.toFloat() - (points[closestIdx].kg - paddedMin) / paddedRange * size.height.toFloat()
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        val w = size.width
                        val h = size.height
                        val n = points.size

                        fun xOf(i: Int) = if (n == 1) w / 2f else i.toFloat() / (n - 1).toFloat() * w
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

                        // Apply drawing animation
                        drawContext.canvas.save()
                        drawContext.canvas.clipRect(
                            androidx.compose.ui.geometry.Rect(0f, 0f, w * drawProgress, h)
                        )

                        if (n == 1) {
                            // Single point: draw a horizontal line and a single dot
                            val pt = pts.first()
                            drawLine(
                                color = WeightLineColor,
                                start = Offset(0f, pt.y),
                                end = Offset(w, pt.y),
                                strokeWidth = 2.6.dp.toPx()
                            )
                            drawCircle(Background, 7.dp.toPx(), pt)
                            drawCircle(WeightLineColor, 5.dp.toPx(), pt)
                        } else {
                            // Build cubic Bezier path
                            val linePath = Path().apply {
                                moveTo(pts[0].x, pts[0].y)
                                for (i in 1 until pts.size) {
                                    val cx = (pts[i - 1].x + pts[i].x) / 2f
                                    cubicTo(cx, pts[i - 1].y, cx, pts[i].y, pts[i].x, pts[i].y)
                                }
                            }

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
                        }

                        drawContext.canvas.restore()

                        // Pulsing ring + dot on latest point (if fully drawn and not interacting)
                        if (drawProgress >= 0.99f && selectedPoint == null) {
                            val last  = pts.last()
                            val dotR  = 5.dp.toPx()
                            drawCircle(WeightLineColor.copy(alpha = pulseAlpha), dotR * pulseScale, last)
                            drawCircle(Background, dotR + 2f.dp.toPx(), last)
                            drawCircle(WeightLineColor, dotR, last)
                        }

                        // Draw interaction tooltip
                        selectedPoint?.let { sp ->
                            // Vertical guide line
                            drawLine(
                                color = TextTertiary.copy(alpha = 0.5f),
                                start = Offset(selectedX, 0f),
                                end = Offset(selectedX, h),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                            )
                            // Dot on line
                            drawCircle(WeightLineColor, 6.dp.toPx(), Offset(selectedX, selectedY))
                            drawCircle(Background, 4.dp.toPx(), Offset(selectedX, selectedY))
                            drawCircle(WeightLineColor, 2.dp.toPx(), Offset(selectedX, selectedY))
                        }
                    }

                    // Tooltip Popup overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = selectedPoint != null,
                        enter = fadeIn(tween(150)),
                        exit = fadeOut(tween(150))
                    ) {
                        selectedPoint?.let { sp ->
                            // Calculate padding to keep tooltip within bounds
                            Box(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = with(density) { selectedX.toDp() } - 40.dp,
                                            y = with(density) { selectedY.toDp() } - 44.dp
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TextPrimary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "%.1f kg".format(sp.kg),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Background
                                        )
                                        Text(
                                            sp.dateLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Background.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // X-axis date labels — first / mid / last
                val labelIdxs = if (points.size == 1) listOf(0) else listOf(0, points.size / 2, points.size - 1).distinct()
                Box(Modifier.fillMaxWidth().height(xAxisH)) {
                    labelIdxs.forEach { idx ->
                        val align = when {
                            points.size == 1 -> Alignment.Center
                            idx == 0 -> Alignment.CenterStart
                            idx == points.size - 1 -> Alignment.CenterEnd
                            else -> Alignment.Center
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

private fun <T> Iterable<T>.indexOfMinBy(selector: (T) -> Float): Int {
    val iterator = iterator()
    if (!iterator.hasNext()) return -1
    var minElem = iterator.next()
    var minValue = selector(minElem)
    var minIndex = 0
    var index = 0
    while (iterator.hasNext()) {
        index++
        val elem = iterator.next()
        val v = selector(elem)
        if (v < minValue) {
            minElem = elem
            minValue = v
            minIndex = index
        }
    }
    return minIndex
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
