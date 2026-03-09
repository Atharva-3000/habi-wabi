package com.habitflow.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.habitflow.app.data.PreferencesManager
import com.habitflow.app.ui.habits.HabitsScreen
import com.habitflow.app.ui.home.HomeScreen
import com.habitflow.app.ui.onboarding.OnboardingScreen
import com.habitflow.app.ui.settings.SettingsScreen
import com.habitflow.app.ui.splash.SplashScreen
import com.habitflow.app.ui.todo.TodoScreen
import com.habitflow.app.ui.createhabit.CreateHabitScreen
import com.habitflow.app.ui.water.WaterTrackingScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val prefsManager = PreferencesManager(context)
    val onboardingComplete by prefsManager.onboardingComplete.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ── Splash (always first) ──────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onComplete = {
                    val dest = if (onboardingComplete) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(dest) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Onboarding ─────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Main tabs ──────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToCreateHabit = { navController.navigate(Screen.CreateHabit.route) },
                onNavigateToWater = { navController.navigate(Screen.Water.route) }
            )
        }
        composable(Screen.Habits.route) {
            HabitsScreen(
                onNavigateToCreateHabit = { navController.navigate(Screen.CreateHabit.route) }
            )
        }
        composable(Screen.Todo.route) {
            TodoScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }

        // ── Full-screen flows ──────────────────────────────────────────────
        composable(Screen.CreateHabit.route) {
            CreateHabitScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Water.route) {
            WaterTrackingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
