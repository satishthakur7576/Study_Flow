package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

// Premium High-Contrast Dashboard Fintrix Palette
val FintrixOrange = Color(0xFFFD5C25) // Radiant signature Orange/Sunset Red
val FintrixOrangeLight = Color(0xFFFF7E40) // Vibrant Coral Orange/Highlight
val FintrixOrangeDark = Color(0xFFE2430C) // Deep Reddish-Orange

// Vibrant accents from the layout
val AccentTeal = Color(0xFF00FFCC) // Neon growing highlight
val AccentGreen = Color(0xFF10B981) // High-contrast green
val AccentBlue = Color(0xFF3B82F6) // Electric blue metrics

// Light Theme Coordinates (High Contrast Clean Off-White)
val SoftLightBg = Color(0xFFF8FAFC) // Sleek Slate-50 background
val LightCardBg = Color(0xFFFFFFFF) // Crisp brilliant white card surface
val LightTextPrimary = Color(0xFF0F172A) // Dark slate-900 maximum readability
val LightTextSecondary = Color(0xFF64748B) // Slate-500 subtexts
val LightOutlineColor = Color(0xFFCBD5E1) // Smart card border layout

// Dark Theme Coordinates (Premium Luxury Fintrix Matte Black)
val ObsidianDarkBg = Color(0xFF0C0D0E) // Ultimate high-end luxury charcoal black background
val ObsidianCardBg = Color(0xFF15161A) // Sleek matte dark carbon card container
val DarkTextPrimary = Color(0xFFF3F4F6) // Crisp snow off-white for perfect legibility
val DarkTextSecondary = Color(0xFF8F929C) // Exquisite slate-grey subtext/descriptions
val DarkOutlineColor = Color(0xFF25262B) // Ultra-smart refined dark card outline border

// Styled Priority Levels with Maximum Contrast (Vibrant/Clear)
val LowPriorityColor = Color(0xFF10B981) // Active/Done Green
val MediumPriorityColor = Color(0xFFF59E0B) // Warn Amber
val HighPriorityColor = Color(0xFFEF4444) // Urgent vivid crimson

// --- PREMIUM HIGH-FIDELITY GRADIENTS FROM SHOWN IMAGES ---
val FintrixOrangeGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF2563EB), // Sleek royal blue
        Color(0xFF3B82F6), // Ocean primary blue
        Color(0xFF60A5FA)  // Soft sky blue
    )
)

val FintrixTealGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF10B981), // High-contrast emerald green
        Color(0xFF34D399)  // Vibrant mint green
    )
)

// Metallic Reflective Card Gradients (Mimicking "Savings account" card)
val FintrixCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF), // Pristine brilliant white
        Color(0xFFFAFBFC)  // Extremely soft, clean light surface
    )
)

val FintrixCardHoverGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF1F5F9)
    )
)

@Composable
fun cardGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun cardHoverGradient(): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surface
        )
    )
}

val DarkBackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F1014), // Luxury graphite slate top
        Color(0xFF060709)  // Absolute infinite deep black bottom
    )
)

// --- ACCENT CLASSIFICATION AND RESOLUTION FOR CUSTOM THEMES ---
data class DynamicGradientColors(
    val primaryColor: Color,
    val gradientBrush: Brush,
    val lightAccentColor: Color,
    val darkAccentColor: Color
)

fun getDynamicColors(accentName: String): DynamicGradientColors {
    return when (accentName) {
        "Ocean Blue" -> DynamicGradientColors(
            primaryColor = Color(0xFF3B82F6),
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF60A5FA),
                    Color(0xFF3B82F6),
                    Color(0xFF1D4ED8)
                )
            ),
            lightAccentColor = Color(0xFF93C5FD),
            darkAccentColor = Color(0xFF1E3A8A)
        )
        "Forest Green" -> DynamicGradientColors(
            primaryColor = Color(0xFF10B981),
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF34D399),
                    Color(0xFF10B981),
                    Color(0xFF047857)
                )
            ),
            lightAccentColor = Color(0xFF6EE7B7),
            darkAccentColor = Color(0xFF064E3B)
        )
        "Lavender" -> DynamicGradientColors(
            primaryColor = Color(0xFF8B5CF6),
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFC084FC),
                    Color(0xFF8B5CF6),
                    Color(0xFF5B21B6)
                )
            ),
            lightAccentColor = Color(0xFFDDD6FE),
            darkAccentColor = Color(0xFF4C1D95)
        )
        else -> DynamicGradientColors( // "Sunset Red" or other
            primaryColor = Color(0xFFFD5C25),
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF8E3C),
                    Color(0xFFFD5C25),
                    Color(0xFFC02A00)
                )
            ),
            lightAccentColor = Color(0xFFFFBFAB),
            darkAccentColor = Color(0xFF4E1605)
        )
    }
}
