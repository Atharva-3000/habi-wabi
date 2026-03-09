package com.habitflow.app.ui.habits

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.data.model.Habit
import com.habitflow.app.ui.home.iconForName
import com.habitflow.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    onNavigateToCreateHabit: () -> Unit,
    viewModel: HabitsViewModel = viewModel()
) {
    val habitsWithStats by viewModel.habitsWithStats.collectAsState()
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateHabit,
                containerColor = GoldAccent,
                contentColor = Background,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New Habit")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "habi wabi",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 4.sp, fontWeight = FontWeight.Light
                        ),
                        color = TextTertiary.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("My Habits", style = MaterialTheme.typography.displayLarge)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldAccent.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${habitsWithStats.size} habits",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent
                    )
                }
            }

            // ── Content ──────────────────────────────────────────────────────
            if (habitsWithStats.isEmpty()) {
                EmptyHabitsState(onAddHabit = onNavigateToCreateHabit)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(habitsWithStats, key = { it.habit.id }) { hw ->
                        HabitListCard(
                            habitWithStats = hw,
                            onDeleteRequest = { habitToDelete = hw.habit }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) } // FAB clearance
                }
            }
        }
    }

    // ── Delete confirmation dialog ───────────────────────────────────────────
    habitToDelete?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            containerColor = Surface,
            title = {
                Text("Delete \"${habit.title}\"?", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text(
                    "All logs for this habit will also be deleted. This can't be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habit)
                    habitToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFE85A5A))
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = TextTertiary)
                }
            }
        )
    }
}

@Composable
private fun HabitListCard(
    habitWithStats: HabitWithStats,
    onDeleteRequest: () -> Unit
) {
    val habit = habitWithStats.habit
    val habitColor = remember(habit.colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
            .getOrDefault(GoldAccent)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Surface)
            .padding(16.dp)
    ) {
        // ── Top row: icon + title + delete button ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(habitColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconForName(habit.iconName),
                    contentDescription = null,
                    tint = habitColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${habit.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · ${
                        if (habit.habitType.name == "CHECKMARK") "Checkmark" else "Time tracked"
                    }",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            // Streak badge
            if (habitWithStats.streak > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(habitColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "🔥 ${habitWithStats.streak}",
                        style = MaterialTheme.typography.labelSmall,
                        color = habitColor
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            IconButton(
                onClick = onDeleteRequest,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "Options",
                    tint = TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── 16-week contribution heatmap ──
        val weeks = habitWithStats.gridAlphas.chunked(7)
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            weeks.forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { alpha ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (alpha == 0f) Divider
                                    else habitColor.copy(alpha = alpha)
                                )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Stats row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatChip(label = "Total", value = "${habitWithStats.totalDone} days", color = habitColor)
            StatChip(label = "Streak", value = "${habitWithStats.streak} days", color = habitColor)
            StatChip(label = "Type", value = if (habit.habitType.name == "CHECKMARK") "☑" else "⏱", color = habitColor)
        }
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = color)
    }
}

@Composable
private fun EmptyHabitsState(onAddHabit: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Text("∅", fontSize = 48.sp, color = TextTertiary.copy(alpha = 0.3f))
            Text(
                "No habits yet",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                "Start with one tiny habit. That's the wabi-sabi way.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onAddHabit,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create a Habit", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
