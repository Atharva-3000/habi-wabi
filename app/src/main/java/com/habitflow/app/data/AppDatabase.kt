package com.habitflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habitflow.app.data.dao.HabitDao
import com.habitflow.app.data.dao.HabitLogDao
import com.habitflow.app.data.dao.HealthDao
import com.habitflow.app.data.dao.TodoDao
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitLog
import com.habitflow.app.data.model.HealthEntry
import com.habitflow.app.data.model.Todo

@Database(
    entities = [Habit::class, HabitLog::class, Todo::class, HealthEntry::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun todoDao(): TodoDao
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habiwabi.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
