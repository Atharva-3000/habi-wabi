package com.habitflow.app.ui.water

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabiWabiApp
import com.habitflow.app.data.model.HealthEntry
import com.habitflow.app.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class WaterVessel(val label: String, val ml: Int, val emoji: String) {
    SIP("Sip", 50, "💧"),
    CUP("Espresso", 80, "☕"),
    SMALL_GLASS("Small Glass", 150, "🍵"),
    GLASS("Glass", 250, "🥛"),
    MUG("Mug", 350, "🫖"),
    BOTTLE("Bottle", 500, "🧃"),
    LARGE_BOTTLE("Large Bottle", 750, "🍶")
}

data class WaterUiState(
    val todayMl: Int = 0,
    val goalMl: Int = 2000,
    val weekHistory: List<Pair<String, Int>> = emptyList(), // date label → ml
    val lastAddedMl: Int = 0,     // for animation trigger
    val animTrigger: Int = 0       // incremented each add to re-trigger animation
) {
    val progressFraction: Float get() = (todayMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
    val remainingMl: Int get() = (goalMl - todayMl).coerceAtLeast(0)
    val isGoalReached: Boolean get() = todayMl >= goalMl
}

class WaterViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: HealthRepository = (application as HabiWabiApp).healthRepository
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val _uiState = MutableStateFlow(WaterUiState())
    val uiState: StateFlow<WaterUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getWeeklyEntries()
                .collect { entries ->
                    val todayStr = LocalDate.now().format(fmt)
                    val todayEntry = entries.find { it.date == todayStr }
                    val weekData = buildWeekHistory(entries)
                    _uiState.update { state ->
                        state.copy(
                            todayMl = todayEntry?.waterMl ?: 0,
                            goalMl = todayEntry?.dailyGoalMl ?: state.goalMl,
                            weekHistory = weekData
                        )
                    }
                }
        }
    }

    fun addVessel(vessel: WaterVessel) {
        viewModelScope.launch {
            repo.addWater(vessel.ml, _uiState.value.goalMl)
            _uiState.update { it.copy(lastAddedMl = vessel.ml, animTrigger = it.animTrigger + 1) }
        }
    }

    fun undoLast() {
        val ml = _uiState.value.lastAddedMl
        if (ml > 0) {
            viewModelScope.launch { repo.subtractWater(ml) }
        }
    }

    fun setGoal(ml: Int) {
        viewModelScope.launch { repo.setDailyGoal(ml) }
        _uiState.update { it.copy(goalMl = ml) }
    }

    private fun buildWeekHistory(entries: List<HealthEntry>): List<Pair<String, Int>> {
        val today = LocalDate.now()
        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dateStr = date.format(fmt)
            val dayLabel = when (daysAgo) {
                0 -> "T"
                1 -> "Y"
                else -> date.dayOfWeek.name.take(1)
            }
            val ml = entries.find { it.date == dateStr }?.waterMl ?: 0
            Pair(dayLabel, ml)
        }
    }
}
