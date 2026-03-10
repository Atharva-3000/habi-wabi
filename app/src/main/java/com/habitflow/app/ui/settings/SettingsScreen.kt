package com.habitflow.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.data.model.Habit
import com.habitflow.app.ui.home.iconForName
import com.habitflow.app.ui.theme.*

private val WeightColor = Color(0xFF7B68EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToWeightHistory: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {

    val allHabits           by viewModel.allHabits.collectAsState()
    val todayWeight         by viewModel.todayWeight.collectAsState()
    val weightHistory       by viewModel.weightHistory.collectAsState()
    val globalNotifs        by viewModel.globalNotificationsEnabled.collectAsState()
    val waterGoal           by viewModel.waterDailyGoalMl.collectAsState()
    val quietStart          by viewModel.quietHoursStart.collectAsState()
    val quietEnd            by viewModel.quietHoursEnd.collectAsState()
    val reminderOffset      by viewModel.reminderOffsetMinutes.collectAsState()
    val snackbarHostState   = remember { SnackbarHostState() }

    // Habit whose reminder time we are currently editing (null = no sheet open)
    var editReminderHabit by remember { mutableStateOf<Habit?>(null) }
    // Water goal edit state
    var editWaterGoal by remember { mutableStateOf(false) }
    var waterGoalInput by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.weightSaved) {
        if (viewModel.weightSaved) {
            snackbarHostState.showSnackbar("Weight saved ✓")
            viewModel.clearWeightSaved()
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

            // ─── Header ──────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    "habi wabi",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 4.sp, fontWeight = FontWeight.Light
                    ),
                    color = TextTertiary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text("Settings", style = MaterialTheme.typography.displayLarge)
                Text(
                    "Preferences, health & reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            }

            // ─── Weight Logging ───────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Filled.MonitorWeight,
                iconTint = WeightColor,
                title = "Weight"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (todayWeight != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(WeightColor.copy(alpha = 0.08f))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Today: %.1f kg".format(todayWeight),
                                style = MaterialTheme.typography.titleMedium,
                                color = WeightColor,
                                modifier = Modifier.weight(1f)
                            )
                            Text("✓", color = Color(0xFF5AE88A), fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = viewModel.weightInput,
                            onValueChange = { viewModel.weightInput = it },
                            placeholder = { Text("e.g. 72.5", color = TextDisabled) },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = textFieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { viewModel.logWeight() },
                            modifier = Modifier.height(56.dp).align(Alignment.Bottom),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background),
                            enabled = viewModel.weightInput.toFloatOrNull() != null
                        ) { Text("Log", fontWeight = FontWeight.SemiBold) }
                    }
                    Text(
                        "Logged once per day — used for the 30-day trend chart below.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("30-Day Trend", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    WeightLineChart(points = weightHistory, modifier = Modifier.fillMaxWidth())

                    Spacer(Modifier.height(16.dp))
                    
                    // Button to navigate to full weight history
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToWeightHistory)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Check weight history",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            SettingsDivider()

            // ─── Habit Reminders ──────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Outlined.Notifications,
                iconTint = GoldAccent,
                title = "Habit Reminders"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    // Global master toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.NotificationsActive, null, tint = GoldAccent, modifier = Modifier.size(18.dp)) }
                        Column(Modifier.weight(1f)) {
                            Text("All Notifications", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (globalNotifs) "Reminders are on" else "All reminders paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (globalNotifs) Color(0xFF5AE88A) else TextTertiary
                            )
                        }
                        Switch(
                            checked = globalNotifs,
                            onCheckedChange = { viewModel.setGlobalNotifications(it) },
                            colors = switchColors()
                        )
                    }

                    // Per-habit list
                    if (allHabits.isEmpty()) {
                        Text(
                            "Create a habit first — then enable reminders here or when adding a habit.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary
                        )
                    } else {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Divider,
                            thickness = 0.5.dp
                        )
                        Text(
                            "Per-Habit Reminders",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextTertiary
                        )
                        allHabits.forEach { habit ->
                            HabitReminderRow(
                                habit = habit,
                                globalEnabled = globalNotifs,
                                onToggle = { viewModel.toggleHabitReminder(habit) },
                                onEditTime = { editReminderHabit = habit }
                            )
                        }
                    }

                    // Quiet hours row
                    SettingsDivider()
                    Box(modifier = Modifier.padding(top = 8.dp)) {
                        QuietHoursRow(
                            start = quietStart,
                            end = quietEnd,
                            onEdit = { s, e -> viewModel.setQuietHours(s, e) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Reminder offset setting
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Surface)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Timer, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Reminder Offset", style = MaterialTheme.typography.labelLarge)
                                Text("Nudge me early so I can prepare", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp), // Add slight padding for focus/ripple clearance
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(0, 5, 10, 15, 30).forEach { mins ->
                                val isActive = reminderOffset == mins
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isActive) GoldAccent.copy(alpha = 0.15f) else SurfaceVariant)
                                        .border(1.dp, if (isActive) GoldAccent else Divider, RoundedCornerShape(10.dp))
                                        .clickable { viewModel.setReminderOffset(mins) }
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (mins == 0) "On time" else "${mins}m early",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) GoldAccent else TextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            SettingsDivider()

            // ─── App Preferences ──────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Filled.Tune,
                iconTint = Color(0xFF5AB8E8),
                title = "App Preferences"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Water daily goal
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF5AB8E8).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Filled.WaterDrop, null, tint = Color(0xFF5AB8E8), modifier = Modifier.size(18.dp)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Daily Water Goal", style = MaterialTheme.typography.titleMedium)
                            Text("${waterGoal} ml / day", style = MaterialTheme.typography.labelSmall, color = Color(0xFF5AB8E8))
                        }
                        AnimatedVisibility(visible = !editWaterGoal) {
                            TextButton(onClick = {
                                waterGoalInput = waterGoal.toString()
                                editWaterGoal = true
                            }) { Text("Edit", color = GoldAccent) }
                        }
                    }

                    // Water goal inline editor & chips (only visible when editing)
                    AnimatedVisibility(visible = editWaterGoal) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Quick water goal chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(1500, 2000, 2500, 3000).forEach { ml ->
                                    val isSelected = waterGoalInput == ml.toString()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Color(0xFF5AB8E8).copy(alpha = 0.2f) else Background)
                                            .border(1.dp, if (isSelected) Color(0xFF5AB8E8) else Divider, RoundedCornerShape(10.dp))
                                            .clickable { waterGoalInput = ml.toString() }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "${ml}ml",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) Color(0xFF5AB8E8) else TextTertiary
                                        )
                                    }
                                }
                            }

                            // Manual entry + save button
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = waterGoalInput,
                                    onValueChange = { waterGoalInput = it },
                                    label = { Text("Custom (ml)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    colors = textFieldColors(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = {
                                        waterGoalInput.toIntOrNull()?.let { viewModel.setWaterGoal(it) }
                                        editWaterGoal = false
                                    },
                                    modifier = Modifier.height(56.dp).align(Alignment.Bottom),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background),
                                    enabled = waterGoalInput.toIntOrNull() != null
                                ) { Text("Save", fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }
            }

            SettingsDivider()
            Spacer(Modifier.height(16.dp)) // Push 'About' down

            // ─── About ────────────────────────────────────────────────────────
            SettingsSection(
                icon = Icons.Outlined.Info,
                iconTint = TextTertiary,
                title = "About"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AboutRow("App",        "Habi Wabi")
                    AboutRow("Version",    "1.2.0 · Alpha Release (March 10, 2026)")
                    AboutRow("Philosophy", "Wabi-sabi · imperfect, impermanent, incomplete")
                    AboutRow("Data",       "100% on-device · Room SQLite · Auto Backup")
                    AboutRow("Privacy",    "No accounts. No tracking. Ever.")
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldAccent.copy(alpha = 0.06f))
                            .padding(14.dp)
                    ) {
                        Text(
                            "\"In the beginner's mind there are many possibilities, " +
                            "but in the expert's mind there are few.\" — Shunryu Suzuki",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            color = TextTertiary
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // ── Per-habit reminder time picker sheet ───────────────────────────────────
    editReminderHabit?.let { habit ->
        ReminderTimeSheet(
            habit = habit,
            onDismiss = { editReminderHabit = null },
            onSave = { h, m ->
                viewModel.updateHabitReminderTime(habit, h, m)
                editReminderHabit = null
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PER-HABIT REMINDER ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HabitReminderRow(
    habit: Habit,
    globalEnabled: Boolean,
    onToggle: () -> Unit,
    onEditTime: () -> Unit
) {
    val habitColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(GoldAccent)

    val rowAlpha by animateFloatAsState(
        targetValue = if (globalEnabled) 1f else 0.45f,
        label = "row_alpha_${habit.id}"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(habitColor.copy(alpha = 0.12f * rowAlpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconForName(habit.iconName), null, tint = habitColor.copy(alpha = rowAlpha), modifier = Modifier.size(18.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(habit.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary.copy(alpha = rowAlpha))
                AnimatedContent(targetState = habit.reminderEnabled, label = "reminder_time") { enabled ->
                    if (enabled) {
                        Text(
                            formatTime(habit.reminderHour, habit.reminderMinute),
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent.copy(alpha = rowAlpha)
                        )
                    } else {
                        Text("No reminder", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                }
            }

            // Edit time button — only visible when enabled
            AnimatedVisibility(visible = habit.reminderEnabled && globalEnabled) {
                TextButton(
                    onClick = onEditTime,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(14.dp), tint = GoldAccent)
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                }
            }

            // Toggle
            Switch(
                checked = habit.reminderEnabled,
                onCheckedChange = { onToggle() },
                enabled = globalEnabled,
                colors = switchColors()
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUIET HOURS ROW
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuietHoursRow(start: String, end: String, onEdit: (String, String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.DarkMode, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Quiet Hours", style = MaterialTheme.typography.labelLarge)
                Text(
                    "No reminders between $end – $start",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        // Quick presets
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("22:00", "07:00", "10pm–7am"),
                Triple("23:00", "07:00", "11pm–7am"),
                Triple("21:00", "08:00", "9pm–8am"),
                Triple("00:00", "00:00", "Off")
            ).forEach { (s, e, label) ->
                val isActive = start == s && end == e
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) GoldAccent.copy(alpha = 0.15f) else SurfaceVariant)
                        .border(1.dp, if (isActive) GoldAccent else Divider, RoundedCornerShape(10.dp))
                        .clickable { onEdit(s, e) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelSmall, color = if (isActive) GoldAccent else TextTertiary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REMINDER TIME PICKER SHEET
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int) -> Unit
) {
    val timeState = rememberTimePickerState(
        initialHour = habit.reminderHour,
        initialMinute = habit.reminderMinute,
        is24Hour = true
    )
    val habitColor = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(GoldAccent)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(iconForName(habit.iconName), null, tint = habitColor, modifier = Modifier.size(20.dp)) }
                Column(Modifier.weight(1f)) {
                    Text("Edit Reminder", style = MaterialTheme.typography.titleMedium)
                    Text(habit.title, style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                }
            }

            // Time picker
            TimePicker(
                state = timeState,
                colors = TimePickerDefaults.colors(
                    clockDialColor          = SurfaceVariant,
                    selectorColor           = GoldAccent,
                    clockDialSelectedContentColor   = Background,
                    clockDialUnselectedContentColor = TextPrimary,
                    periodSelectorBorderColor       = Divider,
                    periodSelectorSelectedContainerColor  = GoldAccent.copy(alpha = 0.2f),
                    periodSelectorSelectedContentColor    = GoldAccent,
                    periodSelectorUnselectedContentColor  = TextTertiary,
                    timeSelectorSelectedContainerColor    = GoldAccent.copy(alpha = 0.2f),
                    timeSelectorUnselectedContainerColor  = SurfaceVariant,
                    timeSelectorSelectedContentColor      = GoldAccent,
                    timeSelectorUnselectedContentColor    = TextPrimary
                )
            )

            // Current time display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldAccent.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Daily at ${formatTime(timeState.hour, timeState.minute)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent
                )
            }

            // Save button
            Button(
                onClick = { onSave(timeState.hour, timeState.minute) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background)
            ) {
                Icon(Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Reminder", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED COMPOSABLE HELPERS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SettingsSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp)) }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
        content()
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Divider, thickness = 0.5.dp)
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextTertiary, modifier = Modifier.width(90.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor  = Background,
    checkedTrackColor  = GoldAccent,
    uncheckedThumbColor = TextTertiary,
    uncheckedTrackColor = Divider
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor    = GoldAccent,
    unfocusedBorderColor  = Divider,
    focusedContainerColor = SurfaceVariant,
    unfocusedContainerColor = SurfaceVariant,
    cursorColor           = GoldAccent,
    focusedTextColor      = TextPrimary,
    unfocusedTextColor    = TextPrimary,
    focusedLabelColor     = GoldAccent,
    unfocusedLabelColor   = TextTertiary
)

private fun formatTime(hour: Int, minute: Int): String {
    return "%02d:%02d".format(hour, minute)
}
