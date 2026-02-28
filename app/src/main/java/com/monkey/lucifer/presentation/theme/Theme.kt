package com.monkey.lucifer.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val DarkColorScheme = Colors(
    primary = Color(0xFFFF6B6B),
    primaryVariant = Color(0xFFFF5252),
    secondary = Color(0xFF4ECDC4),
    secondaryVariant = Color(0xFF45B7AA),
    background = Color(0xFF000000),
    surface = Color(0xFF1A1A1A),
    error = Color(0xFFFF6B6B),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFF000000),
    onError = Color(0xFF000000)
)

@Composable
fun LuciferTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = DarkColorScheme,
        content = content
    )
}