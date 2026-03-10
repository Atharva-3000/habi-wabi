package com.habitflow.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TodoFrequency { DAILY, WEEKLY, CUSTOM, ONCE }

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val categoryLabel: String = "Personal",
    val date: String,                          // ISO date
    val frequency: TodoFrequency = TodoFrequency.ONCE,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderTime: String? = null           // "HH:mm" or null
)
