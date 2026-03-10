package com.habitflow.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitflow.app.ui.theme.Background
import com.habitflow.app.ui.theme.Surface
import com.habitflow.app.ui.theme.TextPrimary
import com.habitflow.app.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.*

private val WeightColor = Color(0xFF7B68EE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val logs by viewModel.allWeightLogs.collectAsState()

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Weight History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    navigationIconContentColor = TextPrimary,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No weight history yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
            ) {
                // The logs are ordered by timestamp DESC (newest at index 0)
                itemsIndexed(logs) { index, log ->
                    // To find the difference, we compare to the PREVIOUS log in time,
                    // which is the next element in the list (index + 1)
                    val diff = if (index < logs.lastIndex) {
                        log.weightKg - logs[index + 1].weightKg
                    } else {
                        0f
                    }

                    val diffLabel = if (diff > 0) "+%.1f".format(diff) else "%.1f".format(diff)
                    val diffColor = if (diff <= 0f) Color(0xFF5AE88A) else Color(0xFFE85A5A)
                    val dateLabel = formatTimestamp(log.timestamp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "%.1f kg".format(log.weightKg),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = WeightColor
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = dateLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextTertiary
                            )
                        }
                        
                        Text(
                            text = if (index == logs.lastIndex) "+0.0" else diffLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (index == logs.lastIndex) TextTertiary else diffColor
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}
