package com.habitflow.app.ui.createhabit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.data.model.HabitFrequency
import com.habitflow.app.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Icon list
// ─────────────────────────────────────────────────────────────────────────────
val habitIcons: List<Pair<ImageVector, String>> = listOf(
    Pair(Icons.Filled.FitnessCenter, "Workout"),
    Pair(Icons.AutoMirrored.Filled.DirectionsRun, "Run"),
    Pair(Icons.AutoMirrored.Filled.MenuBook, "Read"),
    Pair(Icons.Filled.SelfImprovement, "Meditate"),
    Pair(Icons.Filled.WaterDrop, "Water"),
    Pair(Icons.Filled.Bedtime, "Sleep"),
    Pair(Icons.Filled.Restaurant, "Diet"),
    Pair(Icons.Filled.MusicNote, "Music"),
    Pair(Icons.Filled.Code, "Code"),
    Pair(Icons.Filled.Brush, "Art"),
    Pair(Icons.Filled.LocalFireDepartment, "Streak"),
    Pair(Icons.Filled.Favorite, "Health"),
)

private val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")

// ─────────────────────────────────────────────────────────────────────────────
// CREATE HABIT SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateHabitViewModel = viewModel()
) {
    var showReminderSheet by remember { mutableStateOf(false) }
    var advancedExpanded by remember { mutableStateOf(false) }
    val reminderOffset by viewModel.reminderOffsetMinutes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.saveSuccess) {
        if (viewModel.saveSuccess) onNavigateBack()
    }
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val habitColor = HabitColors.getOrElse(viewModel.selectedColorIndex) { HabitColors[9] }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(Background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // ── Top bar ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "habi wabi",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Light),
                            color = TextTertiary.copy(alpha = 0.6f)
                        )
                        Text("New Habit", style = MaterialTheme.typography.titleLarge)
                    }
                }

                // ── Icon picker ──────────────────────────────────────────────
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(habitIcons) { index, (icon, _) ->
                            val isSelected = index == viewModel.selectedIconIndex
                            val size by animateDpAsState(if (isSelected) 62.dp else 40.dp, label = "icon_size_$index")
                            Box(
                                modifier = Modifier
                                    .size(size)
                                    .clip(CircleShape)
                                    .background(habitColor.copy(alpha = if (isSelected) 0.2f else 0.08f))
                                    .then(if (isSelected) Modifier.border(1.5.dp, habitColor, CircleShape) else Modifier)
                                    .clickable { viewModel.selectedIconIndex = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon, null,
                                    tint = if (isSelected) habitColor else TextTertiary,
                                    modifier = Modifier.size(if (isSelected) 28.dp else 20.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    "Choose an icon",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    modifier = Modifier.fillMaxWidth().wrapContentWidth()
                )

                Spacer(Modifier.height(24.dp))

                // ── Habit name ───────────────────────────────────────────────
                SectionLabel("Habit Name", Icons.Filled.Edit)
                OutlinedTextField(
                    value = viewModel.habitName,
                    onValueChange = { viewModel.habitName = it },
                    placeholder = { Text("What habit do you want to build?", color = TextDisabled) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = SurfaceVariant,
                        unfocusedContainerColor = SurfaceVariant,
                        cursorColor = GoldAccent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (viewModel.habitName.isNotEmpty()) {
                            IconButton(onClick = { viewModel.habitName = "" }) {
                                Icon(Icons.Outlined.Close, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))

                // ── Color picker ─────────────────────────────────────────────
                SectionLabel("Color", Icons.Filled.Palette)
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(HabitColors) { index, color ->
                        val isSelected = index == viewModel.selectedColorIndex
                        val borderColor by animateColorAsState(if (isSelected) color else Color.Transparent, label = "color_b_$index")
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.5.dp, borderColor, CircleShape)
                                .clickable { viewModel.selectedColorIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) Icon(Icons.Filled.Check, null, tint = Background, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Habit type ───────────────────────────────────────────────
                SectionLabel("Habit Type", Icons.Filled.ToggleOn)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HabitTypeChip("Checkmark", Icons.Filled.CheckCircle, viewModel.isCheckmarkType, Modifier.weight(1f)) { viewModel.isCheckmarkType = true }
                    HabitTypeChip("Time Tracked", Icons.Filled.Timer, !viewModel.isCheckmarkType, Modifier.weight(1f)) { viewModel.isCheckmarkType = false }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (viewModel.isCheckmarkType) "Mark done with a single tap each day."
                    else "Log focused time sessions and track total hours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(Modifier.height(24.dp))

                // ── Advanced accordion ───────────────────────────────────────
                AdvancedSection(viewModel = viewModel, expanded = advancedExpanded, onToggle = { advancedExpanded = !advancedExpanded }, habitColor = habitColor)

                Spacer(Modifier.height(100.dp))
            }

            // ── Save button (pinned) ─────────────────────────────────────────
            Button(
                onClick = { showReminderSheet = true },
                enabled = !viewModel.isSaving && viewModel.habitName.isNotBlank(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .navigationBarsPadding()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background)
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Background, strokeWidth = 2.dp)
                } else {
                    Text("Continue", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // ── Reminder bottom sheet ────────────────────────────────────────────────
    if (showReminderSheet) {
        ReminderSheet(
            reminderEnabled = viewModel.reminderEnabled,
            reminderHour = viewModel.reminderHour,
            reminderMinute = viewModel.reminderMinute,
            reminderOffset = reminderOffset,
            onDismiss = { showReminderSheet = false },
            onSkip = {
                showReminderSheet = false
                viewModel.reminderEnabled = false
                viewModel.saveHabit()
            },
            onSaveWithReminder = { h, m ->
                showReminderSheet = false
                viewModel.reminderEnabled = true
                viewModel.reminderHour = h
                viewModel.reminderMinute = m
                viewModel.saveHabit()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADVANCED SECTION (frequency, target, specific days)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AdvancedSection(
    viewModel: CreateHabitViewModel,
    expanded: Boolean,
    onToggle: () -> Unit,
    habitColor: Color
) {
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow_rot")

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        // Toggle header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceVariant)
                .clickable(onClick = onToggle)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Tune, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Advanced Options", style = MaterialTheme.typography.labelLarge)
                Text("Frequency, target, specific days", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
            Icon(
                Icons.Filled.KeyboardArrowDown, null,
                tint = TextTertiary,
                modifier = Modifier.size(20.dp).rotate(arrowRotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(SurfaceVariant)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HorizontalDivider(color = Divider, thickness = 0.5.dp)

                // ── Frequency ────────────────────────────────────────────────
                Text("How often?", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(HabitFrequency.DAILY, HabitFrequency.WEEKLY, HabitFrequency.CUSTOM).forEach { freq ->
                        val label = when (freq) {
                            HabitFrequency.DAILY   -> "Daily"
                            HabitFrequency.WEEKLY  -> "Weekly"
                            HabitFrequency.CUSTOM  -> "Specific Days"
                            else                   -> freq.name
                        }
                        val isSelected = viewModel.selectedFrequency == freq
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) habitColor.copy(alpha = 0.2f) else Background)
                                .border(1.dp, if (isSelected) habitColor else Divider, RoundedCornerShape(10.dp))
                                .clickable { viewModel.selectedFrequency = freq }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelMedium, color = if (isSelected) habitColor else TextTertiary)
                        }
                    }
                }

                // ── Weekday picker (only for SPECIFIC_DAYS) ──────────────────
                AnimatedVisibility(visible = viewModel.selectedFrequency == HabitFrequency.CUSTOM) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Select days", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            weekdays.forEachIndexed { i, label ->
                                val isOn = viewModel.selectedWeekdays.contains(i)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isOn) habitColor else Background)
                                        .border(1.dp, if (isOn) habitColor else Divider, CircleShape)
                                        .clickable { viewModel.toggleWeekday(i) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = if (isOn) Background else TextTertiary, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // ── Completion target ────────────────────────────────────────
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Daily Goal", style = MaterialTheme.typography.labelLarge)
                        Text("Times to complete per day", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    }
                    Switch(
                        checked = viewModel.completionTargetEnabled,
                        onCheckedChange = { viewModel.completionTargetEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Background,
                            checkedTrackColor = GoldAccent,
                            uncheckedThumbColor = TextTertiary,
                            uncheckedTrackColor = Divider
                        )
                    )
                }

                AnimatedVisibility(visible = viewModel.completionTargetEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Background)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = { if (viewModel.completionTarget > 1) viewModel.completionTarget-- },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) { Icon(Icons.Filled.Remove, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                        Text(
                            "${viewModel.completionTarget}×",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = habitColor,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        IconButton(
                            onClick = { if (viewModel.completionTarget < 20) viewModel.completionTarget++ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) { Icon(Icons.Filled.Add, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// REMINDER BOTTOM SHEET  — with time picker
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSheet(
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    reminderOffset: Int,
    onDismiss: () -> Unit,
    onSkip: () -> Unit,
    onSaveWithReminder: (hour: Int, minute: Int) -> Unit
) {
    var wantReminder by remember { mutableStateOf(reminderEnabled) }
    var hour by remember { mutableIntStateOf(reminderHour) }
    var minute by remember { mutableIntStateOf(reminderMinute) }
    val timeState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)

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
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Notifications, null, tint = GoldAccent, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Add a Reminder?", style = MaterialTheme.typography.titleMedium)
                    Text("Get a gentle nudge at the right time", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
                }
            }

            // Reminder toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable reminder", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(
                    checked = wantReminder,
                    onCheckedChange = { wantReminder = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Background,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = Divider
                    )
                )
            }

            // Time picker
            AnimatedVisibility(visible = wantReminder) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Pick a time", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                    TimePicker(
                        state = timeState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = SurfaceVariant,
                            selectorColor = GoldAccent,
                            clockDialSelectedContentColor = Background,
                            clockDialUnselectedContentColor = TextPrimary,
                            periodSelectorBorderColor = Divider,
                            periodSelectorSelectedContainerColor = GoldAccent.copy(alpha = 0.2f),
                            periodSelectorSelectedContentColor = GoldAccent,
                            periodSelectorUnselectedContentColor = TextTertiary,
                            timeSelectorSelectedContainerColor = GoldAccent.copy(alpha = 0.2f),
                            timeSelectorUnselectedContainerColor = SurfaceVariant,
                            timeSelectorSelectedContentColor = GoldAccent,
                            timeSelectorUnselectedContentColor = TextPrimary
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldAccent.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        val offsetText = if (reminderOffset == 0) "on time" else "$reminderOffset mins before"
                        Text(
                            "Reminder will be sent $offsetText",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Action buttons
            Button(
                onClick = {
                    if (wantReminder) onSaveWithReminder(timeState.hour, timeState.minute)
                    else onSkip()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background)
            ) {
                Icon(if (wantReminder) Icons.Filled.Notifications else Icons.Filled.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (wantReminder) "Save with Reminder" else "Save Habit", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }

            TextButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text("Skip for now", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED HELPERS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
internal fun SectionLabel(label: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text("*", color = GoldAccent, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun HabitTypeChip(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val borderColor by animateColorAsState(if (selected) GoldAccent else Divider, label = "chip_b_$label")
    val bgColor by animateColorAsState(if (selected) GoldAccent.copy(alpha = 0.1f) else Color.Transparent, label = "chip_bg_$label")
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = if (selected) GoldAccent else TextTertiary, modifier = Modifier.size(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) GoldAccent else TextSecondary,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
