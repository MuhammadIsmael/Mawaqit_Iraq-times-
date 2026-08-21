package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MawaqitColorScheme = darkColorScheme(
    primary = MawaqitGoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = MawaqitGoldDark,
    onPrimaryContainer = Color.White,
    secondary = MawaqitGreenBadge,
    onSecondary = Color.White,
    background = MawaqitBackground,
    onBackground = MawaqitTextPrimary,
    surface = MawaqitCardBg,
    onSurface = MawaqitTextPrimary,
    outline = MawaqitCardBorder
)

@Composable
fun MawaqitIraqTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MawaqitColorScheme,
        typography = Typography,
        content = content
    )
}
