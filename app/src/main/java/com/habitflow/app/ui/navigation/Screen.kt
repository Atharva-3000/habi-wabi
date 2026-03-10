package com.habitflow.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Onboarding   : Screen("onboarding")
    object Home         : Screen("home")
    object Habits       : Screen("habits")
    object Todo         : Screen("todo")
    object Settings     : Screen("settings")
    object CreateHabit  : Screen("create_habit")
    object Water        : Screen("water")
    object WeightHistory: Screen("weight_history")
}
