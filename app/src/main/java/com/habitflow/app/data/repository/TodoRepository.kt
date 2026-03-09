package com.habitflow.app.data.repository

import com.habitflow.app.data.dao.TodoDao
import com.habitflow.app.data.model.Todo
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()
    suspend fun insertTodo(todo: Todo) = todoDao.insertTodo(todo)
    suspend fun updateTodo(todo: Todo) = todoDao.updateTodo(todo)
    suspend fun deleteTodo(todo: Todo) = todoDao.deleteTodo(todo)
}
