package com.habitflow.app.ui.todo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habitflow.app.HabiWabiApp
import com.habitflow.app.data.model.Todo
import com.habitflow.app.data.repository.TodoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class TodoFilter { ALL, ACTIVE, DONE }

data class TodoGroup(
    val label: String,
    val dateKey: String,
    val todos: List<Todo>
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: TodoRepository = (application as HabiWabiApp).todoRepository
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    private val _filter = MutableStateFlow(TodoFilter.ALL)
    val filter: StateFlow<TodoFilter> = _filter

    val groups: StateFlow<List<TodoGroup>> = repo.getAllTodos()
        .combine(_filter) { todos, filter ->
            val filtered = when (filter) {
                TodoFilter.ALL    -> todos
                TodoFilter.ACTIVE -> todos.filter { !it.isDone }
                TodoFilter.DONE   -> todos.filter { it.isDone }
            }
            buildGroups(filtered)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalActive: StateFlow<Int> = repo.getAllTodos()
        .map { todos -> todos.count { !it.isDone } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun setFilter(f: TodoFilter) { _filter.value = f }

    fun addTodo(title: String, date: LocalDate, category: String = "Personal", reminderTime: String? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repo.insertTodo(
                Todo(
                    title = title.trim(),
                    date = date.format(fmt),
                    categoryLabel = category,
                    reminderTime = reminderTime
                )
            )
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch { repo.updateTodo(todo) }
    }

    fun toggleTodo(todo: Todo) {
        viewModelScope.launch { repo.updateTodo(todo.copy(isDone = !todo.isDone)) }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch { repo.deleteTodo(todo) }
    }

    private fun buildGroups(todos: List<Todo>): List<TodoGroup> {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val todayStr = today.format(fmt)
        val tomorrowStr = tomorrow.format(fmt)

        return todos
            .groupBy { it.date }
            .entries
            .sortedBy { it.key }
            .map { (dateStr, items) ->
                val label = when (dateStr) {
                    todayStr     -> "Today"
                    tomorrowStr  -> "Tomorrow"
                    else -> {
                        val date = LocalDate.parse(dateStr, fmt)
                        if (date.isBefore(today)) "Past"
                        else {
                            "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}, " +
                            "${date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)} ${date.dayOfMonth}"
                        }
                    }
                }
                TodoGroup(label = label, dateKey = dateStr, todos = items)
            }
    }
}
