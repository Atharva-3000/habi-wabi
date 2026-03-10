package com.habitflow.app.data.repository

import com.habitflow.app.data.dao.HealthDao
import com.habitflow.app.data.dao.WeightLogDao
import com.habitflow.app.data.model.HealthEntry
import com.habitflow.app.data.model.WeightLog
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HealthRepository(
    private val healthDao: HealthDao,
    private val weightLogDao: WeightLogDao
) {

    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    fun getTodayEntry(): Flow<List<HealthEntry>> =
        healthDao.getWeeklyEntries(LocalDate.now().format(fmt))

    fun getWeeklyEntries(): Flow<List<HealthEntry>> =
        healthDao.getWeeklyEntries(LocalDate.now().minusDays(6).format(fmt))

    fun getMonthlyEntries(): Flow<List<HealthEntry>> =
        healthDao.getWeeklyEntries(LocalDate.now().minusDays(29).format(fmt))

    suspend fun addWater(ml: Int, goalMl: Int) {
        val today = LocalDate.now().format(fmt)
        val existing = healthDao.getEntryForDate(today)
        val current = existing ?: HealthEntry(date = today, waterMl = 0, dailyGoalMl = goalMl)
        healthDao.insertOrUpdate(current.copy(waterMl = current.waterMl + ml))
    }

    suspend fun subtractWater(ml: Int) {
        val today = LocalDate.now().format(fmt)
        val existing = healthDao.getEntryForDate(today) ?: return
        val newMl = (existing.waterMl - ml).coerceAtLeast(0)
        healthDao.insertOrUpdate(existing.copy(waterMl = newMl))
    }

    suspend fun setDailyGoal(goalMl: Int) {
        val today = LocalDate.now().format(fmt)
        val existing = healthDao.getEntryForDate(today)
            ?: HealthEntry(date = today, waterMl = 0, dailyGoalMl = goalMl)
        healthDao.insertOrUpdate(existing.copy(dailyGoalMl = goalMl))
    }

    fun getWeightLogs(): Flow<List<WeightLog>> = weightLogDao.getAllLogs()

    suspend fun logWeight(kg: Float) {
        val today = LocalDate.now().format(fmt)
        val existing = healthDao.getEntryForDate(today)
            ?: HealthEntry(date = today)
        healthDao.insertOrUpdate(existing.copy(weightKg = kg))
        
        weightLogDao.insertLog(WeightLog(weightKg = kg))
    }

}
