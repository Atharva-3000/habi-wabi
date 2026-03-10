package com.habitflow.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habitflow.app.data.model.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WeightLog)
    
    @Query("DELETE FROM weight_logs")
    suspend fun deleteAllLogs()
}
