package com.habitflow.app.ui.todo

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.data.model.Todo
import com.habitflow.app.ui.theme.*
import java.time.LocalDate

private val categoryColors = mapOf(
    "Personal"  to Color(0xFF7B68EE),
    "Work"      to Color(0xFF5A9FE8),
    "Health"    to Color(0xFF5AE88A),
    "Finance"   to Color(0xFFC9A96E),
    "Learning"  to Color(0xFFE85AB0),
    "Creative"  to Color(0xFFE8935A),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(viewModel: TodoViewModel = viewModel()) {
    val groups by viewModel.groups.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val totalActive by viewModel.totalActive.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceVariant,
                    contentColor = TextPrimary,
                    actionColor = GoldAccent,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = GoldAccent,
                contentColor = Background,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background)
                .statusBarsPadding()
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    "habi wabi",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Light),
                    color = TextTertiary.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tasks", style = MaterialTheme.typography.displayLarge, modifier = Modifier.weight(1f))
                    AnimatedVisibility(visible = totalActive > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GoldAccent)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "$totalActive",
                                style = MaterialTheme.typography.labelLarge,
                                color = Background,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // ── Filter chips ─────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoFilter.values().forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldAccent,
                            selectedLabelColor = Background,
                            containerColor = SurfaceVariant,
                            labelColor = TextTertiary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = filter == f,
                            selectedBorderColor = GoldAccent,
                            borderColor = Divider
                        )
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Content ──────────────────────────────────────────────────────
            if (groups.isEmpty()) {
                TodoEmptyState(filter = filter, onAdd = { showAddSheet = true })
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    groups.forEach { group ->
                        item(key = "header_${group.dateKey}") {
                            TodoGroupHeader(label = group.label, count = group.todos.count { !it.isDone })
                        }
                        items(group.todos, key = { it.id }) { todo ->
                            TodoItem(
                                todo = todo,
                                onToggle = { viewModel.toggleTodo(todo) },
                                onDelete = {
                                    viewModel.deleteTodo(todo)
                                }
                            )
                        }
                        item(key = "spacer_${group.dateKey}") { Spacer(Modifier.height(12.dp)) }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ── Add Task bottom sheet ─────────────────────────────────────────────────
    if (showAddSheet) {
        AddTodoSheet(
            onDismiss = { showAddSheet = false },
            onAdd = { title, date, category ->
                viewModel.addTodo(title, date, category)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun TodoGroupHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            color = if (label == "Today") GoldAccent else TextSecondary
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Divider)
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text("$count", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = Divider, thickness = 0.5.dp)
    }
}

@Composable
private fun TodoItem(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    val catColor = categoryColors[todo.categoryLabel] ?: GoldAccent

    // Animate check
    var checkScale by remember { mutableFloatStateOf(1f) }
    val animCheck by animateFloatAsState(
        targetValue = checkScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "check_anim"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Animated circular checkbox
        Box(
            modifier = Modifier
                .size(24.dp)
                .scale(animCheck)
                .clip(CircleShape)
                .background(if (todo.isDone) GoldAccent else Color.Transparent)
                .border(2.dp, if (todo.isDone) GoldAccent else Divider, CircleShape)
                .clickable {
                    checkScale = 1.3f
                    onToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            LaunchedEffect(checkScale) {
                if (checkScale > 1f) {
                    kotlinx.coroutines.delay(120)
                    checkScale = 1f
                }
            }
            if (todo.isDone) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Background,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Content
        Column(Modifier.weight(1f)) {
            Text(
                todo.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (todo.isDone) TextTertiary else TextPrimary,
                textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2
            )
            Spacer(Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.7f))
                )
                Text(
                    todo.categoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Delete",
                tint = TextTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun TodoEmptyState(filter: TodoFilter, onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Text("○", fontSize = 48.sp, color = TextTertiary.copy(alpha = 0.2f))
            Text(
                when (filter) {
                    TodoFilter.DONE   -> "Nothing completed yet"
                    TodoFilter.ACTIVE -> "All done! 🎉"
                    TodoFilter.ALL    -> "No tasks yet"
                },
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "One task at a time. One day at a time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
            if (filter == TodoFilter.ALL) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add a Task", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTodoSheet(
    onDismiss: () -> Unit,
    onAdd: (String, LocalDate, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedCategory by remember { mutableStateOf("Personal") }

    val categories = categoryColors.keys.toList()

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New Task", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("What needs to be done?", color = TextDisabled) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = Divider,
                    focusedContainerColor = SurfaceVariant,
                    unfocusedContainerColor = SurfaceVariant,
                    cursorColor = GoldAccent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Date quick-select
            Column {
                Text("When", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Today" to LocalDate.now(), "Tomorrow" to LocalDate.now().plusDays(1)).forEach { (label, date) ->
                        val selected = selectedDate == date
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) GoldAccent else SurfaceVariant)
                                .border(1.dp, if (selected) GoldAccent else Divider, RoundedCornerShape(10.dp))
                                .clickable { selectedDate = date }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Background else TextSecondary
                            )
                        }
                    }
                }
            }

            // Category
            Column {
                Text("Category", style = MaterialTheme.typography.labelLarge, color = TextTertiary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val color = categoryColors[cat] ?: GoldAccent
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.2f) else SurfaceVariant)
                                .border(1.dp, if (isSelected) color else Divider, RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                                Text(
                                    cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) color else TextTertiary
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { if (title.isNotBlank()) onAdd(title, selectedDate, selectedCategory) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background),
                enabled = title.isNotBlank()
            ) {
                Text("Add Task", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
