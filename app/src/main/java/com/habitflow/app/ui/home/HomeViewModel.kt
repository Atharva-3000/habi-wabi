package com.habitflow.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabiWabiApp
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitLog
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.data.repository.HealthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitWithStatus(
    val habit: Habit,
    val isDoneToday: Boolean,
    val streak: Int,
    val last7Days: List<Boolean>   // oldest→newest, index 6 = today
)

data class HealthSnapshot(
    val waterMl: Int = 0,
    val waterGoalMl: Int = 2000,
    val weightKg: Float? = null,
    val weightDelta: Float? = null  // delta vs yesterday
) {
    val waterFraction: Float get() = (waterMl.toFloat() / waterGoalMl.toFloat()).coerceIn(0f, 1f)
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: HabitRepository = (application as HabiWabiApp).habitRepository
    private val healthRepo: HealthRepository = (application as HabiWabiApp).healthRepository

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now()
    private val todayStr = today.format(dateFormatter)

    /** Habits + completion status for today */
    val habitsWithStatus: StateFlow<List<HabitWithStatus>> =
        repo.getAllHabits()
            .flatMapLatest { habits ->
                if (habits.isEmpty()) return@flatMapLatest flowOf(emptyList())
                val streams = habits.map { habit ->
                    val start = today.minusDays(6)
                    repo.getLogsFromDate(habit.id, start).map { logs ->
                        val logsByDate = logs.associateBy { it.date }
                        val last7 = (0..6).map { offset ->
                            val d = start.plusDays(offset.toLong()).format(dateFormatter)
                            logsByDate[d]?.isDone == true
                        }
                        HabitWithStatus(
                            habit = habit,
                            isDoneToday = logsByDate[todayStr]?.isDone == true,
                            streak = computeStreak(logs),
                            last7Days = last7
                        )
                    }
                }
                combine(streams) { it.toList() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Live water + weight from Room */
    val healthSnapshot: StateFlow<HealthSnapshot> =
        healthRepo.getWeeklyEntries()
            .map { entries ->
                val yesterday = today.minusDays(1).format(dateFormatter)
                val todayEntry   = entries.find { it.date == todayStr }
                val yesterdayEntry = entries.find { it.date == yesterday }
                HealthSnapshot(
                    waterMl      = todayEntry?.waterMl ?: 0,
                    waterGoalMl  = todayEntry?.dailyGoalMl ?: 2000,
                    weightKg     = todayEntry?.weightKg ?: yesterdayEntry?.weightKg,
                    weightDelta  = if (todayEntry?.weightKg != null && yesterdayEntry?.weightKg != null)
                        todayEntry.weightKg - yesterdayEntry.weightKg else null
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HealthSnapshot())

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch { repo.toggleHabitDone(habitId) }
    }

    fun logWeight(kg: Float) {
        viewModelScope.launch { healthRepo.logWeight(kg) }
    }

    private fun computeStreak(logs: List<HabitLog>): Int {
        var streak = 0
        var date = today
        val logSet = logs.filter { it.isDone }.map { it.date }.toHashSet()
        while (true) {
            val dateStr = date.format(dateFormatter)
            if (logSet.contains(dateStr)) {
                streak++
                date = date.minusDays(1)
            } else {
                if (streak == 0) {
                    date = date.minusDays(1)
                    if (logSet.contains(date.format(dateFormatter))) {
                        streak++
                        date = date.minusDays(1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }
}
