package com.habitflow.app.ui.water

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.ui.theme.*

// Water accent color — muted periwinkle-teal that fits the dark theme
private val WaterColor = Color(0xFF6EC6E8)
private val WaterDim = Color(0xFF3A6A7E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterTrackingScreen(
    onNavigateBack: () -> Unit,
    viewModel: WaterViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showGoalSheet by remember { mutableStateOf(false) }
    var showTip by remember { mutableStateOf(false) }
    var goalSlider by remember { mutableFloatStateOf(2000f) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Snackbar after adding water
    LaunchedEffect(state.animTrigger) {
        if (state.animTrigger > 0) {
            val message = if (state.lastAddedMl > 0) "+${state.lastAddedMl}ml added" else "${state.lastAddedMl}ml removed"
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undoLast()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceVariant,
                    contentColor = TextPrimary,
                    actionColor = WaterColor,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                }
                Spacer(Modifier.width(4.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "habi wabi",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Light),
                        color = TextTertiary.copy(alpha = 0.6f)
                    )
                    Text("Water", style = MaterialTheme.typography.displayLarge)
                }
                // Goal button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(WaterColor.copy(alpha = 0.12f))
                        .clickable { goalSlider = state.goalMl.toFloat(); showGoalSheet = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Goal: ${state.goalMl / 1000f}L",
                        style = MaterialTheme.typography.labelLarge,
                        color = WaterColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Animated ring progress ──────────────────────────────────────
            WaterRing(
                progress = state.progressFraction,
                todayMl = state.todayMl,
                goalMl = state.goalMl,
                animTrigger = state.animTrigger,
                isGoalReached = state.isGoalReached
            )

            Spacer(Modifier.height(28.dp))

            // ── Vessel selector ─────────────────────────────────────────────
            Text(
                "Add water",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(WaterVessel.values().toList()) { vessel ->
                    VesselChip(vessel = vessel, onTap = { viewModel.addVessel(vessel) })
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Remove water",
                style = MaterialTheme.typography.titleSmall,
                color = TextTertiary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WaterVessel.values().take(4)) { vessel ->
                    RemoveChip(vessel = vessel, onTap = { viewModel.removeVessel(vessel) })
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── 7-day history ───────────────────────────────────────────────
            if (state.weekHistory.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("This week", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(14.dp))
                    WeekHistoryBar(history = state.weekHistory, goalMl = state.goalMl)
                }
                Spacer(Modifier.height(28.dp))
            }

            // ── Tip card ────────────────────────────────────────────────────
            TipCard(expanded = showTip, onToggle = { showTip = !showTip })

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── Goal bottom sheet ────────────────────────────────────────────────────
    if (showGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoalSheet = false },
            containerColor = Surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
            ) {
                Text("Daily Water Goal", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Doctors recommend 2–2.5L/day for most adults. Adjust for your body & activity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
                Spacer(Modifier.height(24.dp))

                Text(
                    "${goalSlider.toInt() / 1000f}L  (${goalSlider.toInt()}ml)",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = WaterColor,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Slider(
                    value = goalSlider,
                    onValueChange = { goalSlider = it },
                    valueRange = 500f..4000f,
                    steps = 34,
                    colors = SliderDefaults.colors(
                        thumbColor = WaterColor,
                        activeTrackColor = WaterColor,
                        inactiveTrackColor = Divider
                    )
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("0.5L", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text("4L", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
                Spacer(Modifier.height(28.dp))
                Button(
                    onClick = {
                        viewModel.setGoal(goalSlider.toInt())
                        showGoalSheet = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaterColor, contentColor = Background)
                ) {
                    Text("Set Goal", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WATER RING
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WaterRing(
    progress: Float,
    todayMl: Int,
    goalMl: Int,
    animTrigger: Int,
    isGoalReached: Boolean
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "water_ring"
    )

    // Pulse scale on add
    var scale by remember { mutableFloatStateOf(1f) }
    val animScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ring_scale"
    )
    LaunchedEffect(animTrigger) {
        if (animTrigger > 0) {
            scale = 1.06f
            kotlinx.coroutines.delay(200)
            scale = 1f
        }
    }

    val ringColor = if (isGoalReached) GoldAccent else WaterColor
    val ringBg = if (isGoalReached) GoldAccent.copy(alpha = 0.1f) else WaterColor.copy(alpha = 0.08f)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .scale(animScale),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 18.dp.toPx()
                val inset = stroke / 2
                // Background track
                drawArc(
                    color = ringBg,
                    startAngle = -210f,
                    sweepAngle = 240f,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                )
                // Filled arc
                if (animatedProgress > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(ringColor.copy(alpha = 0.6f), ringColor),
                        ),
                        startAngle = -210f,
                        sweepAngle = 240f * animatedProgress,
                        useCenter = false,
                        style = Stroke(stroke, cap = StrokeCap.Round),
                        topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                        size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                    )
                }
            }
            // Center content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isGoalReached) {
                    Text("🎉", fontSize = 32.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Goal reached!", style = MaterialTheme.typography.labelLarge, color = GoldAccent)
                } else {
                    Text(
                        "${todayMl}",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold),
                        color = WaterColor
                    )
                    Text("ml", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${goalMl - todayMl}ml to go",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// VESSEL CHIP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun VesselChip(vessel: WaterVessel, onTap: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "vessel_scale_${vessel.name}"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceVariant)
            .clickable {
                pressed = true
                onTap()
            }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Reset scale after press
        LaunchedEffect(pressed) {
            if (pressed) {
                kotlinx.coroutines.delay(150)
                pressed = false
            }
        }
        Text(vessel.emoji, fontSize = 28.sp)
        Text(
            vessel.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(WaterColor.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("+${vessel.ml}ml", style = MaterialTheme.typography.labelSmall, color = WaterColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REMOVE CHIP
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RemoveChip(vessel: WaterVessel, onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, Divider, RoundedCornerShape(12.dp))
            .clickable { onTap() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("- ${vessel.ml}ml", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WEEK HISTORY BAR CHART
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WeekHistoryBar(history: List<Pair<String, Int>>, goalMl: Int) {
    val maxMl = history.maxOfOrNull { it.second }?.coerceAtLeast(goalMl) ?: goalMl

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        history.forEach { (label, ml) ->
            val frac = (ml.toFloat() / maxMl.toFloat()).coerceIn(0f, 1f)
            val animFrac by animateFloatAsState(
                targetValue = frac,
                animationSpec = tween(600, easing = EaseOutCubic),
                label = "bar_$label"
            )
            val barColor = if (ml >= goalMl) GoldAccent else WaterColor

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                if (ml > 0) {
                    Text(
                        "${ml / 1000f}L",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(3.dp))
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(80.dp * animFrac + 8.dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (ml == 0) Divider else barColor.copy(alpha = 0.7f))
                )
                Spacer(Modifier.height(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TIP CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TipCard(expanded: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("💡", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Hydration tip", style = MaterialTheme.typography.labelLarge, color = GoldAccent)
                if (!expanded) {
                    Text(
                        "Tap to learn about your ideal daily intake",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }
            }
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = Divider, thickness = 0.5.dp)
                Spacer(Modifier.height(4.dp))
                TipRow("🧬", "General", "The WHO recommends 2–2.5L (8–10 cups) per day for adults.")
                TipRow("🏃", "Exercise", "Add 500ml for every 1 hour of intense exercise.")
                TipRow("🌡️", "Hot weather", "Increase by 700ml on hot or humid days.")
                TipRow("☕", "Caffeine", "Coffee is a mild diuretic — compensate with an extra glass.")
                TipRow("💡", "Tip", "Your urine should be pale yellow. Darker means drink more!")
            }
        }
    }
}

@Composable
private fun TipRow(emoji: String, label: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(emoji, fontSize = 16.sp)
        Column {
            Text(label, style = MaterialTheme.typography.labelLarge, color = GoldAccent)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}
