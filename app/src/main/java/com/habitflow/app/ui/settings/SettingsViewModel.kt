package com.habitflow.app.ui.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabiWabiApp
import com.habitflow.app.data.PreferencesManager
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.data.repository.HealthRepository
import com.habitflow.app.notifications.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class WeightPoint(val dateLabel: String, val kg: Float, val dayIndex: Int)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val habitRepo: HabitRepository   = (application as HabiWabiApp).habitRepository
    private val healthRepo: HealthRepository  = (application as HabiWabiApp).healthRepository
    private val prefs     : PreferencesManager = PreferencesManager(application)
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    // ── Habit flows ────────────────────────────────────────────────────────────
    val habitsWithReminders: StateFlow<List<Habit>> = habitRepo.getAllHabits()
        .map { it.filter { h -> h.reminderEnabled } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allHabits: StateFlow<List<Habit>> = habitRepo.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Weight ─────────────────────────────────────────────────────────────────
    var weightInput by mutableStateOf("")
    var weightSaved by mutableStateOf(false)

    val todayWeight: StateFlow<Float?> = healthRepo.getWeeklyEntries()
        .map { entries -> entries.find { it.date == LocalDate.now().format(fmt) }?.weightKg }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val weightHistory: StateFlow<List<WeightPoint>> = healthRepo.getMonthlyEntries()
        .map { entries ->
            entries
                .filter { it.weightKg != null }
                .sortedBy { it.date }
                .mapIndexed { i, entry ->
                    WeightPoint(
                        dateLabel = formatDateLabel(entry.date),
                        kg = entry.weightKg!!,
                        dayIndex = i
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allWeightLogs: StateFlow<List<com.habitflow.app.data.model.WeightLog>> = healthRepo.getWeightLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Preferences ────────────────────────────────────────────────────────────
    val globalNotificationsEnabled: StateFlow<Boolean> = prefs.globalNotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val waterDailyGoalMl: StateFlow<Int> = prefs.waterDailyGoalMl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2500)

    val quietHoursStart: StateFlow<String> = prefs.quietHoursStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "22:00")

    val quietHoursEnd: StateFlow<String> = prefs.quietHoursEnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "07:00")

    val reminderOffsetMinutes: StateFlow<Int> = prefs.reminderOffsetMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 15)

    // ── Weight actions ─────────────────────────────────────────────────────────
    fun logWeight() {
        val kg = weightInput.toFloatOrNull() ?: return
        viewModelScope.launch {
            healthRepo.logWeight(kg)
            weightSaved = true
            weightInput = ""
        }
    }

    fun clearWeightSaved() { weightSaved = false }

    // ── Reminder actions ───────────────────────────────────────────────────────
    fun toggleHabitReminder(habit: Habit) {
        viewModelScope.launch {
            val updated = habit.copy(reminderEnabled = !habit.reminderEnabled)
            habitRepo.updateHabit(updated)
            if (updated.reminderEnabled) NotificationHelper.scheduleReminder(getApplication(), updated)
            else NotificationHelper.cancelReminder(getApplication(), updated)
        }
    }

    fun updateHabitReminderTime(habit: Habit, hour: Int, minute: Int) {
        viewModelScope.launch {
            val updated = habit.copy(reminderEnabled = true, reminderHour = hour, reminderMinute = minute)
            habitRepo.updateHabit(updated)
            NotificationHelper.cancelReminder(getApplication(), habit)
            NotificationHelper.scheduleReminder(getApplication(), updated)
        }
    }

    // ── Global preference actions ──────────────────────────────────────────────
    fun setGlobalNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setGlobalNotifications(enabled)
            // Cancel or reschedule all habit alarms accordingly
            val habits = habitRepo.getAllHabits().first()
            habits.filter { it.reminderEnabled }.forEach { habit ->
                if (enabled) NotificationHelper.scheduleReminder(getApplication(), habit)
                else NotificationHelper.cancelReminder(getApplication(), habit)
            }
        }
    }

    fun setWaterGoal(ml: Int) {
        viewModelScope.launch { prefs.setWaterDailyGoalMl(ml) }
    }

    fun setQuietHours(start: String, end: String) {
        viewModelScope.launch { prefs.setQuietHours(start, end) }
    }

    fun setReminderOffset(minutes: Int) {
        viewModelScope.launch { 
            prefs.setReminderOffsetMinutes(minutes)
            // Reschedule all active reminders with new offset
            val habits = habitRepo.getAllHabits().first()
            habits.filter { it.reminderEnabled }.forEach { habit ->
                NotificationHelper.scheduleReminder(getApplication(), habit)
            }
        }
    }

    // ── Internal helpers ───────────────────────────────────────────────────────
    private fun formatDateLabel(isoDate: String): String = try {
        val date = LocalDate.parse(isoDate, fmt)
        "${date.dayOfMonth}/${date.monthValue}"
    } catch (e: Exception) { isoDate }
}
