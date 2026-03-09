package com.habitflow.app.data.repository

import com.habitflow.app.data.dao.HabitDao
import com.habitflow.app.data.dao.HabitLogDao
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // ── Habits ──────────────────────────────────────────────────────────────────
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    // ── Logs ────────────────────────────────────────────────────────────────────
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> =
        habitLogDao.getLogsForHabit(habitId)

    fun getLogsFromDate(habitId: Long, startDate: LocalDate): Flow<List<HabitLog>> =
        habitLogDao.getLogsFromDate(habitId, startDate.format(dateFormatter))

    suspend fun getLogForHabitAndDate(habitId: Long, date: LocalDate): HabitLog? =
        habitLogDao.getLogForHabitAndDate(habitId, date.format(dateFormatter))

    /**
     * Toggles the completion state for [habitId] on [date].
     * If already done → removes the log. If not done → inserts a completed log.
     */
    suspend fun toggleHabitDone(habitId: Long, date: LocalDate = LocalDate.now()) {
        val dateStr = date.format(dateFormatter)
        val existing = habitLogDao.getLogForHabitAndDate(habitId, dateStr)
        if (existing != null && existing.isDone) {
            habitLogDao.deleteLogForDate(habitId, dateStr)
        } else {
            habitLogDao.insertLog(
                HabitLog(
                    habitId = habitId,
                    date = dateStr,
                    isDone = true,
                    timeTrackedMinutes = 0
                )
            )
        }
    }

    /**
     * Compute current streak (consecutive days ending today or yesterday).
     */
    suspend fun getStreak(habitId: Long): Int {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val log = habitLogDao.getLogForHabitAndDate(habitId, date.format(dateFormatter))
            if (log != null && log.isDone) {
                streak++
                date = date.minusDays(1)
            } else {
                // Allow a one-day gap (didn't check today yet)
                if (streak == 0) {
                    date = date.minusDays(1)
                    val yesterday = habitLogDao.getLogForHabitAndDate(habitId, date.format(dateFormatter))
                    if (yesterday != null && yesterday.isDone) {
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
