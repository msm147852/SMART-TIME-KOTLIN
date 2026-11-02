package com.smarttime.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SmartTimeColors = darkColorScheme(
    primary = Gold,
    onPrimary = Navy,
    secondary = GoldLight,
    background = Navy,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavyRaised,
    onSurfaceVariant = TextSecondary,
    outline = DividerGold
)

@Composable
fun SmartTimeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SmartTimeColors,
        content = content
    )
}
