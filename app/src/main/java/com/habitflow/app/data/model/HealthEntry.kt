package com.habitflow.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class HealthEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,                        // ISO date, one entry per day
    val waterMl: Int = 0,                    // total ml consumed today
    val dailyGoalMl: Int = 2000,             // user-configurable daily goal
    val weightKg: Float? = null
)
