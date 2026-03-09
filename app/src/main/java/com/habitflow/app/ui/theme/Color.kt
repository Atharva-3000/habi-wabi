package com.habitflow.app.ui.theme

import androidx.compose.ui.graphics.Color

// --- Backgrounds ---
val Background = Color(0xFF0E0E0E)
val Surface = Color(0xFF1A1A1A)
val SurfaceVariant = Color(0xFF212121)
val Divider = Color(0xFF2C2C2C)

// --- Accent ---
val GoldAccent = Color(0xFFC9A96E)
val GoldAccentDim = Color(0x40C9A96E)

// --- Text ---
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB3FFFFFF) // 70%
val TextTertiary = Color(0x66FFFFFF)  // 40%
val TextDisabled = Color(0x33FFFFFF)  // 20%

// --- Habit Palette (user-selectable for heat maps) ---
val HabitColors = listOf(
    Color(0xFFE05C5C), // Crimson
    Color(0xFFE8935A), // Amber
    Color(0xFFE8C65A), // Gold
    Color(0xFF6DBF6F), // Sage Green
    Color(0xFF5A9FE8), // Sky Blue
    Color(0xFF7B68EE), // Lavender
    Color(0xFFBF5AE8), // Violet
    Color(0xFFE85AA6), // Rose
    Color(0xFF5AE8CE), // Teal
    Color(0xFFC9A96E), // Warm Gold (default)
)
