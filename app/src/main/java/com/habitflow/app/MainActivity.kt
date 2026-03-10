package com.habitflow.app

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.habitflow.app.data.PreferencesManager
import com.habitflow.app.notifications.NotificationHelper
import com.habitflow.app.ui.navigation.BottomNavBar
import com.habitflow.app.ui.navigation.NavGraph
import com.habitflow.app.ui.navigation.Screen
import com.habitflow.app.ui.theme.Background
import com.habitflow.app.ui.theme.HabitFlowTheme
import kotlinx.coroutines.launch

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
                val welcomeNotifSent by prefsManager.welcomeNotifSent.collectAsState(initial = true)
                val scope = rememberCoroutineScope()

                // Notification Permission Request
                val requestPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted && !welcomeNotifSent) {
                        NotificationHelper.sendWelcomeTestNotification(context)
                        scope.launch { prefsManager.setWelcomeNotifSent(true) }
                    }
                }

                LaunchedEffect(welcomeNotifSent) {
                    NotificationHelper.createNotificationChannel(context)
                    NotificationHelper.scheduleHydrationReminders(context)
                    if (!welcomeNotifSent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                NotificationHelper.sendWelcomeTestNotification(context)
                                prefsManager.setWelcomeNotifSent(true)
                            }
                        } else {
                            // For Android < 13 where permission isn't needed
                            NotificationHelper.sendWelcomeTestNotification(context)
                            prefsManager.setWelcomeNotifSent(true)
                        }
                    }
                }

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
