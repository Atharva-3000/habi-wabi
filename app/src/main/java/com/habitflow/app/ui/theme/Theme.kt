package com.habitflow.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HabitFlowDarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = Background,
    primaryContainer = GoldAccentDim,
    onPrimaryContainer = GoldAccent,
    secondary = TextSecondary,
    onSecondary = Background,
    background = Background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Divider,
    outlineVariant = Color(0xFF1C1C1C),
    error = Color(0xFFCF6679),
    onError = Background,
)

@Composable
fun HabitFlowTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HabitFlowDarkColorScheme,
        typography = AppTypography,
        content = content
    )
}
