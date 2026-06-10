package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FrostedWhite,
    secondary = CyanAccent,
    tertiary = HotPinkAccent,
    background = ObsidianBlack,
    surface = CoalDark,
    onPrimary = ObsidianBlack,
    onSecondary = ObsidianBlack,
    onTertiary = ObsidianBlack,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = GlassGrey,
    onSurfaceVariant = Color.LightGray,
    outline = GridGrey
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
