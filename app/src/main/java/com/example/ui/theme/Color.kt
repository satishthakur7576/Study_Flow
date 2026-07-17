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
val SoftLightBg = Color(0xFFFAFAFA) // Premium #FAFAFA requested by UX Architect
val LightCardBg = Color(0xFFFFFFFF) // Crisp brilliant white card surface
val LightTextPrimary = Color(0xFF111827) // Dark slate-900 maximum readability
val LightTextSecondary = Color(0xFF6B7280) // Slate-500 subtexts
val LightOutlineColor = Color(0xFFE5E7EB) // Smart refined border layout (1dp soft gray)

// Dark Theme Coordinates (Premium Luxury Fintrix Matte Black)
val ObsidianDarkBg = Color(0xFF0F1115) // Premium #0F1115 requested by UX Architect
val ObsidianCardBg = Color(0xFF15181E) // Sleek matte dark carbon card container
val DarkTextPrimary = Color(0xFFF9FAFB) // Crisp snow off-white for perfect legibility
val DarkTextSecondary = Color(0xFF9CA3AF) // Exquisite slate-grey subtext/descriptions
val DarkOutlineColor = Color(0xFF1F2937) // Ultra-smart refined dark card outline border

// Styled Priority Levels with Maximum Contrast (Vibrant/Clear)
val LowPriorityColor = Color(0xFF10B981) // Active/Done Green (Success)
val MediumPriorityColor = Color(0xFFF59E0B) // Warn Amber (Warning)
val HighPriorityColor = Color(0xFFEF4444) // Urgent vivid crimson (Danger)

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
            primaryColor = Color(0xFF4F7CFF), // Linear-grade Stripe Blue
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF8AB4FF),
                    Color(0xFF4F7CFF),
                    Color(0xFF1E56F5)
                )
            ),
            lightAccentColor = Color(0xFFEBF1FF),
            darkAccentColor = Color(0xFF1B2C5A)
        )
        "Forest Green" -> DynamicGradientColors(
            primaryColor = Color(0xFF10B981), // Success Emerald Green
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF34D399),
                    Color(0xFF10B981),
                    Color(0xFF065F46)
                )
            ),
            lightAccentColor = Color(0xFFECFDF5),
            darkAccentColor = Color(0xFF064E3B)
        )
        "Lavender" -> DynamicGradientColors(
            primaryColor = Color(0xFF7C3AED), // Notion-grade Premium Purple
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA78BFA),
                    Color(0xFF7C3AED),
                    Color(0xFF5B21B6)
                )
            ),
            lightAccentColor = Color(0xFFF5F3FF),
            darkAccentColor = Color(0xFF2E1065)
        )
        else -> DynamicGradientColors( // "Sunset Red" / Danger or Urgent Hue
            primaryColor = Color(0xFFEF4444), // Danger Crimson Red
            gradientBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF87171),
                    Color(0xFFEF4444),
                    Color(0xFF991B1B)
                )
            ),
            lightAccentColor = Color(0xFFFEF2F2),
            darkAccentColor = Color(0xFF450A0A)
        )
    }
}
