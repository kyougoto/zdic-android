package com.kyougoto.zdic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    secondary = Gold,
    tertiary = Leaf,
    background = Paper,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Paper2,
    onSurfaceVariant = InkPrimary,
    outline = Color(0xFF9A8F80),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFE39286),
    onPrimary = Color(0xFF3E0E08),
    secondary = Color(0xFFE0C285),
    tertiary = Color(0xFFA7C4AE),
    background = Color(0xFF191713),
    onBackground = Color(0xFFF0ECE2),
    surface = Color(0xFF211E19),
    onSurface = Color(0xFFF0ECE2),
    outline = Color(0xFF7C7467),
)

@Composable
fun ZdicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) Dark else Light,
        typography = Typography,
        content = content,
    )
}
