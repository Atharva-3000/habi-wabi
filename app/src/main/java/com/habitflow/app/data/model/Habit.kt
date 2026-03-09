package com.habitflow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitType { CHECKMARK, TIME_TRACKED }
enum class HabitFrequency { DAILY, WEEKLY, CUSTOM }

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val iconName: String = "favorite",         // Material icon name string
    val colorHex: String = "#C9A96E",          // Stored as hex string
    val habitType: HabitType = HabitType.CHECKMARK,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDays: String = "",               // Comma-separated DayOfWeek ints for CUSTOM/WEEKLY
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val completionTarget: Int = 1,             // e.g. 1 = once a day, 8 = 8 glasses water
    val createdAt: Long = System.currentTimeMillis()
)
