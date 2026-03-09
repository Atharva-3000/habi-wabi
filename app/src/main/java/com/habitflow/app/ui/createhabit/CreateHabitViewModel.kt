package com.habitflow.app.ui.createhabit

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabiWabiApp
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitFrequency
import com.habitflow.app.data.model.HabitType
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.notifications.NotificationHelper
import kotlinx.coroutines.launch

class CreateHabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: HabitRepository = (application as HabiWabiApp).habitRepository

    // ── Form state ────────────────────────────────────────────────────────────
    var habitName by mutableStateOf("")
    var selectedIconIndex by mutableIntStateOf(11)
    var selectedColorIndex by mutableIntStateOf(9)
    var isCheckmarkType by mutableStateOf(true)

    // Advanced
    var selectedFrequency by mutableStateOf(HabitFrequency.DAILY)
    var selectedWeekdays = mutableSetOf<Int>()   // 0=Mon…6=Sun
    var completionTargetEnabled by mutableStateOf(false)
    var completionTarget by mutableIntStateOf(1)

    // Reminder
    var reminderEnabled by mutableStateOf(false)
    var reminderHour by mutableIntStateOf(8)
    var reminderMinute by mutableIntStateOf(0)

    fun toggleWeekday(dayIndex: Int) {
        if (selectedWeekdays.contains(dayIndex)) selectedWeekdays.remove(dayIndex)
        else selectedWeekdays.add(dayIndex)
    }

    // ── Icon / color hex lookup lists ─────────────────────────────────────────
    private val iconNames = listOf(
        "fitness_center", "directions_run", "menu_book", "self_improvement",
        "water_drop", "bedtime", "restaurant", "music_note",
        "code", "brush", "local_fire_department", "favorite"
    )
    private val colorHexList = listOf(
        "#E85A5A", "#E8935A", "#E8CE5A", "#A8E85A",
        "#5AE88A", "#5AE8CE", "#5AB8E8", "#5A75E8",
        "#8A5AE8", "#C9A96E", "#E85AB0", "#A0A0A0"
    )

    var isSaving by mutableStateOf(false)
        private set
    var saveSuccess by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun saveHabit() {
        if (habitName.isBlank()) {
            errorMessage = "Please enter a habit name"
            return
        }
        viewModelScope.launch {
            isSaving = true
            try {
                val newHabit = Habit(
                    title = habitName.trim(),
                    iconName = iconNames.getOrElse(selectedIconIndex) { "favorite" },
                    colorHex = colorHexList.getOrElse(selectedColorIndex) { "#C9A96E" },
                    habitType = if (isCheckmarkType) HabitType.CHECKMARK else HabitType.TIME_TRACKED,
                    frequency = selectedFrequency,
                    completionTarget = if (completionTargetEnabled) completionTarget else 1,
                    reminderEnabled = reminderEnabled,
                    reminderHour = reminderHour,
                    reminderMinute = reminderMinute
                )
                val id = repo.insertHabit(newHabit)
                if (reminderEnabled) {
                    NotificationHelper.scheduleReminder(getApplication(), newHabit.copy(id = id))
                }
                saveSuccess = true
            } catch (e: Exception) {
                errorMessage = "Failed to save habit: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    fun clearError() { errorMessage = null }
}
