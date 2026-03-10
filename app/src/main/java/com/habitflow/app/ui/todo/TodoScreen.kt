package com.habitflow.app.ui.todo

import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.data.model.Todo
import com.habitflow.app.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    appViewModel: TodoViewModel = viewModel(),
    onNavigateHome: () -> Unit = {}
) {
    val filter by appViewModel.filter.collectAsState()
    val groups by appViewModel.groups.collectAsState()
    val totalActive by appViewModel.totalActive.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    editingTodo = null
                    showAddSheet = true
                },
                containerColor = GoldAccent,
                contentColor = Color.White, // pure white plus icon
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TodoHeader(
                activeCount = totalActive,
                currentFilter = filter,
                onFilterChanged = appViewModel::setFilter
            )

            if (groups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✦", color = GoldAccent, fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Your mind is clear.",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            "Tap + to add a new task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    groups.forEach { group ->
                        item(key = group.dateKey) {
                            Column(Modifier.padding(horizontal = 24.dp)) {
                                Text(
                                    group.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    group.todos.forEach { todo ->
                                        TodoItem(
                                            todo = todo,
                                            onToggle = {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                appViewModel.toggleTodo(it)
                                            },
                                            onEdit = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                editingTodo = it
                                                showAddSheet = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddTodoSheet(
            existingTodo = editingTodo,
            onDismiss = {
                showAddSheet = false
                editingTodo = null
            },
            onSave = { title, date, category, reminderTime ->
                if (editingTodo != null) {
                    appViewModel.updateTodo(
                        editingTodo!!.copy(
                            title = title,
                            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            categoryLabel = category,
                            reminderTime = reminderTime
                        )
                    )
                } else {
                    appViewModel.addTodo(title, date, category, reminderTime)
                }
                showAddSheet = false
                editingTodo = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodoHeader(
    activeCount: Int,
    currentFilter: TodoFilter,
    onFilterChanged: (TodoFilter) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(top = 48.dp, bottom = 16.dp, start = 24.dp, end = 24.dp)
    ) {
        Text("Tasks", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(4.dp))
        Text("$activeCount active tasks", style = MaterialTheme.typography.bodyLarge, color = TextTertiary)

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TodoFilter.entries.forEach { f ->
                val selected = f == currentFilter
                FilterChip(
                    selected = selected,
                    onClick = { onFilterChanged(f) },
                    label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = SurfaceVariant,
                        labelColor = TextSecondary,
                        selectedContainerColor = GoldAccent,
                        selectedLabelColor = Color.White // Fix contrast
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoItem(todo: Todo, onToggle: (Todo) -> Unit, onEdit: (Todo) -> Unit) {
    val isDone = todo.isDone
    val bgColor by animateColorAsState(targetValue = if (isDone) SurfaceVariant.copy(alpha = 0.5f) else Surface, label = "bg")
    val alphaAnim by animateDpAsState(targetValue = if (isDone) 0.5.dp else 1.dp, label = "alpha")
    val cardAlpha = alphaAnim.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .combinedClickable(
                onClick = { onToggle(todo) },
                onLongClick = { onEdit(todo) }
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val checkColor = if (isDone) GoldAccent else Divider
        val internalColor = if (isDone) GoldAccent else Color.Transparent

        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(internalColor)
                .border(2.dp, checkColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }

        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = todo.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                ),
                color = if (isDone) TextTertiary else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(todo.categoryLabel, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                if (todo.reminderTime != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.NotificationsActive, contentDescription = "Reminder", tint = GoldAccent, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(todo.reminderTime, style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADD/EDIT TASK SHEET
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTodoSheet(
    existingTodo: Todo? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, date: LocalDate, category: String, reminderTime: String?) -> Unit
) {
    val today = LocalDate.now()
    var title by remember { mutableStateOf(existingTodo?.title ?: "") }
    
    // Custom properties
    var customCategories by remember { mutableStateOf(listOf("Personal", "Work", "Health", "Errands")) }
    var selectedCategory by remember { mutableStateOf(existingTodo?.categoryLabel ?: "Personal") }
    
    // Date parsing for edit mode
    var selectedDate by remember { 
        mutableStateOf(
            existingTodo?.let { LocalDate.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE) } ?: today
        ) 
    }
    
    // Time reminders
    var reminderTime by remember { mutableStateOf<String?>(existingTodo?.reminderTime) }
    var showTimePickerSheet by remember { mutableStateOf(false) }
    var tempHour by remember { mutableIntStateOf(12) }
    var tempMinute by remember { mutableIntStateOf(0) }
    
    val reminderTimeParts = reminderTime?.split(":")?.mapNotNull { it.toIntOrNull() }
    if (reminderTimeParts?.size == 2) {
        tempHour = reminderTimeParts[0]
        tempMinute = reminderTimeParts[1]
    }

    val context = LocalContext.current

    // Dialog flags
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .imePadding()
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(if (existingTodo != null) "Edit Task" else "New Task", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What needs to be done?", color = TextDisabled) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = Divider,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    cursorColor = GoldAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Date picker row
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("When", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isToday = selectedDate == today
                    val isTomorrow = selectedDate == today.plusDays(1)
                    val isCustom = !isToday && !isTomorrow

                    DateChip("Today", isToday) { selectedDate = today }
                    DateChip("Tomorrow", isTomorrow) { selectedDate = today.plusDays(1) }
                    
                    val customLabel = if (isCustom) {
                        selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))
                    } else "Custom"
                    
                    DateChip(
                        label = customLabel, 
                        selected = isCustom, 
                        icon = Icons.Outlined.Event
                    ) { showDatePicker = true }
                }
            }
            
            // Reminder row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { 
                        if (reminderTime == null) showTimePickerSheet = true 
                        else reminderTime = null
                    }
                    .background(if (reminderTime != null) GoldAccent.copy(alpha = 0.1f) else SurfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    if (reminderTime != null) Icons.Filled.NotificationsActive else Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = if (reminderTime != null) GoldAccent else TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    if (reminderTime != null) "Remind me at $reminderTime" else "Add reminder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reminderTime != null) GoldAccent else TextPrimary
                )
            }

            // Categories row (Fixed wrapping with LazyRow)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(customCategories) { cat ->
                        CategoryChip(
                            label = cat,
                            selected = cat == selectedCategory,
                            onClick = { selectedCategory = cat }
                        )
                    }
                    item {
                        CategoryChip(
                            label = "+ New",
                            selected = false,
                            onClick = { showNewCategoryDialog = true },
                            isAddBtn = true
                        )
                    }
                }
            }

            Button(
                onClick = { onSave(title, selectedDate, selectedCategory, reminderTime) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.White),
                enabled = title.isNotBlank()
            ) {
                Text("Save Task", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    // Material 3 Custom DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = GoldAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextTertiary) }
            },
            colors = DatePickerDefaults.colors(containerColor = Surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = GoldAccent,
                    selectedDayContentColor = Color.White,
                    todayDateBorderColor = GoldAccent,
                    todayContentColor = GoldAccent,
                    titleContentColor = GoldAccent
                )
            )
        }
    }

    // Material 3 TimePicker Dialog
    if (showTimePickerSheet) {
        val timeState = rememberTimePickerState(initialHour = tempHour, initialMinute = tempMinute, is24Hour = true)
        
        AlertDialog(
            onDismissRequest = { showTimePickerSheet = false },
            confirmButton = {
                TextButton(onClick = {
                    reminderTime = String.format("%02d:%02d", timeState.hour, timeState.minute)
                    showTimePickerSheet = false
                }) { Text("OK", color = GoldAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerSheet = false }) { Text("Cancel", color = TextTertiary) }
            },
            containerColor = Surface,
            title = { Text("Select Time", style = MaterialTheme.typography.titleMedium) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(
                        state = timeState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = SurfaceVariant,
                            selectorColor = GoldAccent,
                            clockDialSelectedContentColor = Background,
                            clockDialUnselectedContentColor = TextPrimary
                        )
                    )
                }
            }
        )
    }

    // New Category Dialog
    if (showNewCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            containerColor = Surface,
            title = { Text("New Category", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = newCatName,
                    onValueChange = { newCatName = it },
                    placeholder = { Text("E.g., Groceries") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldAccent)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCatName.isNotBlank() && !customCategories.contains(newCatName)) {
                            customCategories = customCategories + newCatName
                            selectedCategory = newCatName
                        }
                        showNewCategoryDialog = false
                    }
                ) { Text("Add", color = GoldAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancel", color = TextTertiary) }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CHIPS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DateChip(
    label: String, 
    selected: Boolean, 
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    val bg = if (selected) GoldAccent else SurfaceVariant
    val tc = if (selected) Color.White else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tc, modifier = Modifier.size(14.dp))
            }
            Text(label, color = tc, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit, isAddBtn: Boolean = false) {
    val bg = if (selected) GoldAccent.copy(alpha = 0.15f) else if (isAddBtn) Color.Transparent else SurfaceVariant
    val borderCol = if (selected) GoldAccent else if (isAddBtn) Divider else Color.Transparent
    val tc = if (selected) GoldAccent else TextSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = tc, style = MaterialTheme.typography.bodyMedium)
    }
}
