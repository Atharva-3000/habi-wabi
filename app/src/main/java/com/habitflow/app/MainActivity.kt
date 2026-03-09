package com.habitflow.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habitflow.app.data.PreferencesManager
import com.habitflow.app.ui.navigation.BottomNavBar
import com.habitflow.app.ui.navigation.NavGraph
import com.habitflow.app.ui.navigation.Screen
import com.habitflow.app.ui.theme.Background
import com.habitflow.app.ui.theme.HabitFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Force night mode regardless of device system setting (no AppCompat dependency needed)
        val config = resources.configuration
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
        resources.updateConfiguration(config, resources.displayMetrics)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitFlowTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val prefsManager = remember { PreferencesManager(context) }
                val onboardingComplete by prefsManager.onboardingComplete.collectAsState(initial = false)

                // Screens that show the bottom nav
                val bottomNavRoutes = listOf(
                    Screen.Home.route,
                    Screen.Habits.route,
                    Screen.Todo.route,
                    Screen.Settings.route
                )
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomNav = currentRoute in bottomNavRoutes

                Scaffold(
                    containerColor = Background,
                    bottomBar = {
                        if (showBottomNav) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Background)
                            .padding(bottom = if (showBottomNav) innerPadding.calculateBottomPadding() else androidx.compose.foundation.layout.PaddingValues().calculateBottomPadding())
                    ) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
