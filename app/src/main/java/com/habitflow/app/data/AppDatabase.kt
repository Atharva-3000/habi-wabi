package com.habitflow.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.habitflow.app.data.dao.HabitDao
import com.habitflow.app.data.dao.HabitLogDao
import com.habitflow.app.data.dao.HealthDao
import com.habitflow.app.data.dao.TodoDao
import com.habitflow.app.data.dao.WeightLogDao
import com.habitflow.app.data.model.Habit
import com.habitflow.app.data.model.HabitLog
import com.habitflow.app.data.model.HealthEntry
import com.habitflow.app.data.model.Todo
import com.habitflow.app.data.model.WeightLog
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Habit::class, HabitLog::class, Todo::class, HealthEntry::class, WeightLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun todoDao(): TodoDao
    abstract fun healthDao(): HealthDao
    abstract fun weightLogDao(): WeightLogDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `weight_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `weightKg` REAL NOT NULL)"
                )
                // Remove the generated mock data (keep today's data intact)
                val todayLog = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                db.execSQL("DELETE FROM health_entries WHERE date != '$todayLog'")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habiwabi.db"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
