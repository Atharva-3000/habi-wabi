package com.habitflow.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.data.model.Habit
import com.habitflow.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ── Icon mapping ─────────────────────────────────────────────────────────────
fun iconForName(name: String): ImageVector = when (name) {
    "fitness_center"        -> Icons.Filled.FitnessCenter
    "directions_run"        -> Icons.AutoMirrored.Filled.DirectionsRun
    "menu_book"             -> Icons.AutoMirrored.Filled.MenuBook
    "self_improvement"      -> Icons.Filled.SelfImprovement
    "water_drop"            -> Icons.Filled.WaterDrop
    "bedtime"               -> Icons.Filled.Bedtime
    "restaurant"            -> Icons.Filled.Restaurant
    "music_note"            -> Icons.Filled.MusicNote
    "code"                  -> Icons.Filled.Code
    "brush"                 -> Icons.Filled.Brush
    "local_fire_department" -> Icons.Filled.LocalFireDepartment
    else                    -> Icons.Filled.Favorite
}

private val mockQuotes = listOf(
    "We are what we repeatedly do. Excellence, then, is not an act but a habit." to "Aristotle",
    "Success is the sum of small efforts, repeated day in and day out." to "Robert Collier",
    "Motivation is what gets you started. Habit is what keeps you going." to "Jim Ryun",
    "Chains of habit are too light to be felt until they are too heavy to be broken." to "Warren Buffett",
    "The secret of your future is hidden in your daily routine." to "Mike Murdock",
    "Small steps in the right direction can turn out to be the biggest step of your life." to "Unknown",
)

// ─────────────────────────────────────────────────────────────────────────────
// HOME SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateHabit: () -> Unit,
    onNavigateToWater: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val today = LocalDate.now()
    val quote = mockQuotes[today.dayOfYear % mockQuotes.size]
    val habitsWithStatus by viewModel.habitsWithStatus.collectAsState()
    val health by viewModel.healthSnapshot.collectAsState()
    val doneCount = habitsWithStatus.count { it.isDoneToday }
    val totalCount = habitsWithStatus.size
    val unreadCount by viewModel.unreadNotifCount.collectAsState()

    // Weight quick-log sheet
    var showWeightSheet by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Spacer(Modifier.height(16.dp))
        HomeHeader(
            today = today,
            unreadCount = unreadCount,
            onBellClick = { viewModel.clearNotifications() }
        )
        Spacer(Modifier.height(20.dp))

        if (totalCount > 0) {
            DailyProgressBanner(doneCount = doneCount, totalCount = totalCount)
            Spacer(Modifier.height(20.dp))
        }

        DailyQuoteCard(quote = quote.first, attribution = quote.second)
        Spacer(Modifier.height(28.dp))

        TodayHabitsSection(
            habits = habitsWithStatus,
            onAddHabit = onNavigateToCreateHabit,
            onToggle = { id ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.toggleHabit(id)
            }
        )

        Spacer(Modifier.height(28.dp))
        HealthStatsSection(
            health = health,
            onWaterTap = onNavigateToWater,
            onWeightTap = { showWeightSheet = true }
        )
        Spacer(Modifier.height(32.dp))
    }

    // Weight quick-log sheet
    if (showWeightSheet) {
        WeightLogSheet(
            currentKg = health.weightKg,
            onDismiss = { showWeightSheet = false },
            onLog = { kg ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.logWeight(kg)
                showWeightSheet = false
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HOME HEADER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HomeHeader(today: LocalDate, unreadCount: Int, onBellClick: () -> Unit) {
    val hour = LocalTime.now().hour
    val (greeting, emoji) = when {
        hour < 5  -> "Good night" to "🌙"
        hour < 12 -> "Good morning" to "☀️"
        hour < 17 -> "Good afternoon" to "🌤️"
        hour < 21 -> "Good evening" to "🌆"
        else      -> "Good night" to "🌙"
    }
    val dateStr = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "habi wabi",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Light),
                color = TextTertiary.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(6.dp))
            Text(dateStr, style = MaterialTheme.typography.labelLarge, color = TextTertiary)
            Spacer(Modifier.height(4.dp))
            Text(
                buildAnnotatedString {
                    append("$greeting $emoji ")
                },
                style = MaterialTheme.typography.displayLarge
            )
        }
        // Notification bell with red dot badge
        Box {
            IconButton(onClick = onBellClick) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = TextTertiary, modifier = Modifier.size(22.dp))
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE85A5A))
                        .align(Alignment.TopEnd)
                        .offset(x = (-8).dp, y = 8.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DAILY PROGRESS BANNER
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DailyProgressBanner(doneCount: Int, totalCount: Int) {
    val fraction = doneCount.toFloat() / totalCount.toFloat()
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "progress_bar"
    )
    val allDone = doneCount == totalCount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (allDone) GoldAccent.copy(alpha = 0.1f) else Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    if (allDone) "All done today! 🎉" else "$doneCount of $totalCount habits done",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (allDone) GoldAccent else TextPrimary
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Divider)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animFraction)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldAccent.copy(alpha = 0.7f), GoldAccent)
                            )
                        )
                )
            }
        }
        Text(
            "${(fraction * 100).toInt()}%",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (allDone) GoldAccent else TextSecondary
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DAILY QUOTE CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DailyQuoteCard(quote: String, attribution: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
                .background(GoldAccent)
        )
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 20.dp)) {
            Text("\u201C", fontFamily = PlayfairFamily, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = GoldAccent, lineHeight = 36.sp, modifier = Modifier.offset(y = 6.dp))
            Spacer(Modifier.height(2.dp))
            Text(quote, fontFamily = PlayfairFamily, fontSize = 15.sp, fontStyle = FontStyle.Italic, lineHeight = 24.sp, color = TextSecondary)
            Spacer(Modifier.height(10.dp))
            Text("— $attribution", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TODAY'S HABITS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TodayHabitsSection(
    habits: List<HabitWithStatus>,
    onAddHabit: () -> Unit,
    onToggle: (Long) -> Unit
) {
    val pendingCount = habits.count { !it.isDoneToday }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Today", style = MaterialTheme.typography.titleLarge)
        if (pendingCount > 0) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GoldAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("$pendingCount left", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddHabit()
            },
            modifier = Modifier.size(32.dp).clip(CircleShape).background(SurfaceVariant)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Habit", tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }

    Spacer(Modifier.height(14.dp))

    if (habits.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceVariant)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("✦", color = GoldAccent, fontSize = 28.sp)
                Text("No habits yet — start small.", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
            }
        }
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            habits.forEach { hw ->
                HabitCard(habitWithStatus = hw, onToggle = { onToggle(hw.habit.id) })
            }
        }
    }
}

@Composable
private fun HabitCard(habitWithStatus: HabitWithStatus, onToggle: () -> Unit) {
    val habit = habitWithStatus.habit
    val isDone = habitWithStatus.isDoneToday
    val haptic = LocalHapticFeedback.current
    val habitColor = remember(habit.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }.getOrDefault(GoldAccent)
    }
    val bgColor by animateColorAsState(
        targetValue = if (isDone) habitColor.copy(alpha = 0.12f) else SurfaceVariant,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "card_bg_${habit.id}"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable(onClick = onToggle)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(habitColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForName(habit.iconName), contentDescription = null, tint = habitColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.weight(1f))
            val checkBg by animateColorAsState(targetValue = if (isDone) habitColor else Surface, label = "check_${habit.id}")
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(checkBg)
                    .border(
                        1.dp,
                        if (isDone) Color.Transparent else habitColor.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Text("✓", color = habitColor.copy(alpha = 0.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
        Text(habit.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        Text(
            if (habitWithStatus.streak > 0) "🔥 ${habitWithStatus.streak} days" else "Start today",
            style = MaterialTheme.typography.labelSmall,
            color = if (isDone) habitColor else TextTertiary
        )
        Spacer(Modifier.height(4.dp))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val gap = 3.dp
            val weeks = habitWithStatus.gridAlphas.chunked(7)
            val weekCount = weeks.size
            if (weekCount > 0) {
                val exactBoxSize = (maxWidth - (gap * (weekCount - 1))) / weekCount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeks.forEach { week ->
                        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
                            week.forEach { alpha ->
                                Box(
                                    modifier = Modifier
                                        .size(exactBoxSize)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (alpha == 0f) Divider else habitColor.copy(alpha = alpha))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HEALTH STATS — LIVE FROM ROOM
// ─────────────────────────────────────────────────────────────────────────────
private val WaterAccent = Color(0xFF6EC6E8)
private val WeightAccent = Color(0xFF7B68EE)

@Composable
private fun HealthStatsSection(
    health: HealthSnapshot,
    onWaterTap: () -> Unit,
    onWeightTap: () -> Unit = {}
) {
    val waterFraction by animateFloatAsState(
        targetValue = health.waterFraction,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "water_progress"
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Health", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Water card ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onWaterTap)
                    .background(Surface)
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = WaterAccent, modifier = Modifier.size(18.dp))
                    Text("Water", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(90.dp)) {
                        val stroke = 8.dp.toPx()
                        val inset = stroke / 2
                        drawArc(
                            color = WaterAccent.copy(alpha = 0.12f),
                            startAngle = -90f, sweepAngle = 360f, useCenter = false,
                            style = Stroke(stroke, cap = StrokeCap.Round),
                            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                        )
                        if (waterFraction > 0f) {
                            drawArc(
                                color = WaterAccent,
                                startAngle = -90f, sweepAngle = 360f * waterFraction, useCenter = false,
                                style = Stroke(stroke, cap = StrokeCap.Round),
                                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                                size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke)
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (health.waterMl >= 1000) "${health.waterMl / 1000f}L" else "${health.waterMl}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = WaterAccent
                        )
                        if (health.waterMl < 1000) Text("ml", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }

                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Goal: ${health.waterGoalMl / 1000f}L",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary
                )

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onWaterTap)
                        .background(WaterAccent.copy(alpha = 0.08f))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tap to log →", style = MaterialTheme.typography.labelMedium, color = WaterAccent, fontWeight = FontWeight.Bold)
                }
            }

            // ── Weight card ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onWeightTap)
                    .background(Surface)
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.MonitorWeight, contentDescription = null, tint = WeightAccent, modifier = Modifier.size(18.dp))
                    Text("Weight", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                }

                Spacer(Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier.size(90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (health.weightKg != null) {
                            Text(
                                "%.1f".format(health.weightKg),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                                color = WeightAccent
                            )
                            Text("kg", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        } else {
                            Text("—", style = MaterialTheme.typography.titleLarge.copy(fontSize = 32.sp), color = TextTertiary.copy(alpha = 0.3f))
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (health.weightKg != null && health.weightDelta != null) {
                    val sign = if (health.weightDelta >= 0f) "+" else ""
                    val deltaColor = if (health.weightDelta <= 0f) Color(0xFF5AE88A) else Color(0xFFE85A5A)
                    Text("$sign${"%.1f".format(health.weightDelta)} vs yesterday", style = MaterialTheme.typography.labelMedium, color = deltaColor)
                } else {
                    Text("Log today's weight", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onWeightTap)
                        .background(WeightAccent.copy(alpha = 0.08f))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Log weight →", style = MaterialTheme.typography.labelMedium, color = WeightAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// WEIGHT QUICK-LOG SHEET
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogSheet(
    currentKg: Float?,
    onDismiss: () -> Unit,
    onLog: (Float) -> Unit
) {
    var input by remember { mutableStateOf(currentKg?.let { "%.1f".format(it) } ?: "") }
    val kg = input.toFloatOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Log Weight", style = MaterialTheme.typography.titleLarge)
            if (currentKg != null) {
                Text(
                    "Last: %.1f kg".format(currentKg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("e.g. 72.5", color = TextDisabled) },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = Divider,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    cursorColor = GoldAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = GoldAccent,
                    unfocusedLabelColor = TextTertiary
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = { if (kg != null) onLog(kg) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background),
                enabled = kg != null
            ) { Text("Save", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
        }
    }
}
