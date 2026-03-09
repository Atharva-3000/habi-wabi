package com.habitflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitflow.app.ui.theme.*

/**
 * Page 1 preview: shows a habit checkmark card with streak.
 */
@Composable
fun OnboardingHabitPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Habit card mock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceVariant)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Morning Workout", style = MaterialTheme.typography.titleMedium)
                    Text("🔥 Streak: 7 days", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", color = Background, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            // Mini 7-day contribution strip
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(1f, 1f, 1f, 1f, 1f, 1f, 0f).forEachIndexed { i, done ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (done == 1f) GoldAccent else Divider)
                    )
                }
            }
        }
    }
}

/**
 * Page 2 preview: GitHub-style heatmap.
 */
@Composable
fun OnboardingHeatmapPreview() {
    val intensities = listOf(
        listOf(0.0f, 0.2f, 0.4f, 0.0f, 1.0f, 0.6f, 0.8f),
        listOf(0.6f, 0.0f, 1.0f, 0.4f, 0.0f, 1.0f, 0.2f),
        listOf(1.0f, 0.8f, 0.0f, 1.0f, 0.6f, 0.0f, 1.0f),
        listOf(0.4f, 1.0f, 0.8f, 0.0f, 1.0f, 0.4f, 0.6f),
        listOf(0.0f, 0.6f, 1.0f, 0.8f, 0.0f, 1.0f, 0.2f),
    )
    val baseColor = GoldAccent

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Progress", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            intensities.forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { intensity ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (intensity == 0f) Divider
                                    else baseColor.copy(alpha = intensity)
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Page 3 preview: task timeline.
 */
@Composable
fun OnboardingTaskPreview() {
    val tasks = listOf(
        Triple("Morning Run", "Personal", true),
        Triple("Read 20 pages", "Personal", true),
        Triple("Review PRs", "Work", false),
        Triple("Call dentist", "Personal", false),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            "Today",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        tasks.forEach { (title, category, done) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (done) GoldAccent else Color.Transparent)
                        .border(
                            width = if (done) 0.dp else 1.5.dp,
                            color = if (done) GoldAccent else TextTertiary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (done) Text("✓", color = Background, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (done) TextTertiary else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldAccent.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(category, style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                }
            }
            HorizontalDivider(color = Divider, thickness = 0.5.dp)
        }
    }
}
