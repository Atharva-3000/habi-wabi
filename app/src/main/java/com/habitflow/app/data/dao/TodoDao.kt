package com.habitflow.app.data.dao

import androidx.room.*
import com.habitflow.app.data.model.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY date ASC, createdAt ASC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE date = :date ORDER BY createdAt ASC")
    fun getTodosForDate(date: String): Flow<List<Todo>>

    @Query("SELECT * FROM todos WHERE date >= :startDate ORDER BY date ASC, createdAt ASC")
    fun getTodosFromDate(startDate: String): Flow<List<Todo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)
}
