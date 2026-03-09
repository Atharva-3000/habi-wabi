package com.habitflow.app.data.dao

import androidx.room.*
import com.habitflow.app.data.model.HealthEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    @Query("SELECT * FROM health_entries ORDER BY date DESC LIMIT 30")
    fun getRecentEntries(): Flow<List<HealthEntry>>

    @Query("SELECT * FROM health_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getWeeklyEntries(startDate: String): Flow<List<HealthEntry>>

    @Query("SELECT * FROM health_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): HealthEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: HealthEntry)
}
