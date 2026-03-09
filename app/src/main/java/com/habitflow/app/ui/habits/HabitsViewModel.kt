package com.habitflow.app.ui.habits

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabiWabiApp
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitLog
import com.habitflow.app.data.repository.HabitRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitWithStats(
    val habit: Habit,
    val streak: Int,
    val totalDone: Int,
    // 16 weeks of grid data (112 days), index 0 = oldest
    val gridAlphas: List<Float>
)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: HabitRepository =
        (application as HabiWabiApp).habitRepository

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now()
    private val gridStart = today.minusDays(111)

    val habitsWithStats: StateFlow<List<HabitWithStats>> =
        repo.getAllHabits()
            .flatMapLatest { habits ->
                if (habits.isEmpty()) return@flatMapLatest flowOf(emptyList())
                val streams = habits.map { habit ->
                    repo.getLogsFromDate(habit.id, gridStart).map { logs ->
                        buildStats(habit, logs)
                    }
                }
                combine(streams) { it.toList() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { repo.deleteHabit(habit) }
    }

    private fun buildStats(habit: Habit, logs: List<HabitLog>): HabitWithStats {
        val logSet = logs.filter { it.isDone }.map { it.date }.toHashSet()
        val gridAlphas = (0..111).map { offset ->
            val d = gridStart.plusDays(offset.toLong())
            when {
                d.isAfter(today) -> 0f
                logSet.contains(d.format(dateFormatter)) -> 1f
                else -> 0f
            }
        }
        var streak = 0
        var date = today
        while (logSet.contains(date.format(dateFormatter))) {
            streak++
            date = date.minusDays(1)
        }
        if (streak == 0) {
            date = today.minusDays(1)
            while (logSet.contains(date.format(dateFormatter))) {
                streak++
                date = date.minusDays(1)
            }
        }
        return HabitWithStats(
            habit = habit,
            streak = streak,
            totalDone = logSet.size,
            gridAlphas = gridAlphas
        )
    }
}
