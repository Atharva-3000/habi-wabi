package com.habitflow.app

import android.app.Application
import com.habitflow.app.data.AppDatabase
import com.habitflow.app.data.repository.HabitRepository
import com.habitflow.app.data.repository.HealthRepository
import com.habitflow.app.data.repository.TodoRepository
import com.habitflow.app.notifications.NotificationHelper

class HabiWabiApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val habitRepository by lazy {
        HabitRepository(database.habitDao(), database.habitLogDao())
    }
    val todoRepository by lazy { TodoRepository(database.todoDao()) }
    val healthRepository by lazy { HealthRepository(database.healthDao(), database.weightLogDao()) }


    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
