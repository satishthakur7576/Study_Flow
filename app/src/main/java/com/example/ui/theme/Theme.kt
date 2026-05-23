package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CustomDarkColorScheme = darkColorScheme(
    primary = FintrixOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2C1914), // Muted dark warm copper
    onPrimaryContainer = FintrixOrangeLight, 
    secondary = AccentTeal,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = Color(0xFF064E3B), // Sleek forest-emerald container
    onSecondaryContainer = Color(0xFF34D399), 
    background = ObsidianDarkBg,
    onBackground = DarkTextPrimary,
    surface = ObsidianCardBg,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF1E2026), // Lighter card/inset slate variant 
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutlineColor
)

private val CustomLightColorScheme = lightColorScheme(
    primary = FintrixOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEBE3), // Smooth soft orange tint
    onPrimaryContainer = FintrixOrangeDark, 
    secondary = Color(0xFF0F766E), // Deep clean teal
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF1F5F9), // Slate active selection
    onSecondaryContainer = LightTextPrimary, 
    background = SoftLightBg,
    onBackground = LightTextPrimary,
    surface = LightCardBg,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF1F5F9), 
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutlineColor
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentName: String = "Sunset Red",
    content: @Composable () -> Unit,
) {
    val dynamicColors = getDynamicColors(accentName)

    val baseDark = CustomDarkColorScheme.copy(
        primary = dynamicColors.primaryColor,
        primaryContainer = dynamicColors.darkAccentColor,
        onPrimaryContainer = dynamicColors.lightAccentColor
    )

    val baseLight = CustomLightColorScheme.copy(
        primary = dynamicColors.primaryColor,
        primaryContainer = dynamicColors.lightAccentColor,
        onPrimaryContainer = dynamicColors.darkAccentColor
    )

    val colorScheme = if (darkTheme) baseDark else baseLight

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
