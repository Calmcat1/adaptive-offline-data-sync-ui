package com.example.adaptive_offline_sync_data_ui.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MonochromeLightColorScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEEEEE),
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF424242),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF616161),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF5F5F5),
    onTertiaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFFAFAFA),
    onSurfaceVariant = Color(0xFF424242),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF5F5F5),
    surfaceContainerHigh = Color(0xFFEEEEEE),
    surfaceContainerHighest = Color(0xFFE0E0E0),
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF9E9E9E),
    error = Color(0xFFC62828),
    onError = Color.White
)

@Composable
fun AdaptiveofflinesyncDatauiTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MonochromeLightColorScheme,
        typography = Typography,
        content = content
    )
}