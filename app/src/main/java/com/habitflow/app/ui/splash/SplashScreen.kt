package com.habitflow.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.habitflow.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Splash screen with a three-part reveal animation:
 *  1. 0–400ms   — background fades in (already same color so seamless)
 *  2. 200–700ms — two golden arcs (forming an incomplete enso circle) draw in
 *  3. 600–900ms — wordmark "habi wabi" fades + scales up from 0.85
 *  4. 1400ms    — onComplete fires → nav to Onboarding or Home
 */
@Composable
fun SplashScreen(onComplete: () -> Unit) {
    // Arc draw progress: 0 → 1 over 500ms, easing out
    val arcAnim = remember { Animatable(0f) }
    // Wordmark fade: 0 → 1
    val textAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0.85f) }
    // Dot pulse for the · between words
    val dotAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Arc draws in
        arcAnim.animateTo(1f, tween(600, 0, EaseOutCubic))
        // Wordmark appears
        delay(80)
        textAlpha.animateTo(1f, tween(400, easing = EaseOutCubic))
        textScale.animateTo(1f, tween(400, easing = EaseOutCubic))
        // Dot sparkle
        dotAnim.animateTo(1f, tween(300, easing = EaseOutCubic))
        // Hold
        delay(600)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Enso arc ring ────────────────────────────────────────────────
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawEnso(arcProgress = arcAnim.value)
                }
            }

            // ── Wordmark ─────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "habi wabi",
                    fontFamily = PlayfairFamily,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp,
                    color = TextPrimary
                )
                Text(
                    "小さな習慣、大きな変化",   // "small habits, big changes" in Japanese
                    fontFamily = InterFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.sp,
                    color = GoldAccent.copy(alpha = dotAnim.value * 0.8f)
                )
            }
        }
    }
}

/**
 * Draws an open enso (zen circle) — a brush stroke that almost completes, leaving
 * a deliberate gap. This embodies wabi-sabi: beauty in imperfection.
 */
private fun DrawScope.drawEnso(arcProgress: Float) {
    val strokeW = 6.dp.toPx()
    val inset   = strokeW / 2f
    val arcSize = Size(size.width - strokeW, size.height - strokeW)
    val topLeft = Offset(inset, inset)

    // Outer glow ring
    drawArc(
        color = GoldAccent.copy(alpha = 0.06f * arcProgress),
        startAngle = -200f,
        sweepAngle = 330f * arcProgress,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            strokeW * 3f,
            cap = StrokeCap.Round
        ),
        topLeft = topLeft,
        size = arcSize
    )

    // Main gold arc — sweeps 310° leaving a 50° deliberate gap (wabi-sabi gap)
    drawArc(
        brush = Brush.sweepGradient(
            colorStops = arrayOf(
                0.0f to GoldAccent.copy(alpha = 0.3f),
                0.5f to GoldAccent,
                1.0f to GoldAccent.copy(alpha = 0.6f)
            ),
            center = Offset(size.width / 2f, size.height / 2f)
        ),
        startAngle = -200f,
        sweepAngle = 310f * arcProgress,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            strokeW,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        topLeft = topLeft,
        size = arcSize
    )
}
