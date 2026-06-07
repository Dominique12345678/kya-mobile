package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = KyaGreen,
    secondary = KyaOrange,
    tertiary = GreenGrey80,
    background = Color(0xFF0F172A), // Modern slate-dark
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color(0xFFF8F9FA),
    onSurface = Color(0xFFF8F9FA)
)

private val LightColorScheme = lightColorScheme(
    primary = KyaGreen,
    secondary = KyaOrange,
    tertiary = GreenGrey40,
    background = KyaBackground,
    surface = KyaCardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = KyaDarkText,
    onSurface = KyaDarkText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We enforce our premium brand identity as requested by the UI guidelines,
    // so we skip Android 12+ dynamic color overriding, ensuring #019a88 is always the primary accent.
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
