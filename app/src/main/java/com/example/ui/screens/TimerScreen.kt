package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyViewModel
import com.example.ui.theme.*

@Composable
fun TimerScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val secondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val isRunning by viewModel.timerIsRunning.collectAsStateWithLifecycle()
    val isBreakMode by viewModel.isBreakMode.collectAsStateWithLifecycle()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val totalSessions by viewModel.totalSessions.collectAsStateWithLifecycle()
    val focusDurationMinutes by viewModel.focusDurationMinutes.collectAsStateWithLifecycle()
    val breakDurationMinutes by viewModel.breakDurationMinutes.collectAsStateWithLifecycle()

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val timerString = "%02d:%02d".format(minutes, seconds)

    val maxSeconds = if (isBreakMode) breakDurationMinutes * 60 else focusDurationMinutes * 60
    val progressFraction = if (maxSeconds > 0) secondsLeft.toFloat() / maxSeconds.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        label = "clock_progress"
    )

    val activePrimaryColor = if (isBreakMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("timer_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PHASE STATE DECAL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FintrixCardGradient),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, activePrimaryColor.copy(alpha = 0.60f)),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isBreakMode) "☕ Break Phase Active" else "🎯 Focus Phase Centered",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = activePrimaryColor
                )
                Text(
                    text = if (isBreakMode) "Take a walk, stretch, or grab some water!" else "Minimize interruptions. Stay committed until the bell.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // --- CORE CIRCULAR POMODORO CLOCK ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(FintrixCardGradient),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(230.dp)
                ) {
                    // Static Tracker Circle Background
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(220.dp),
                        strokeWidth = 10.dp,
                        color = activePrimaryColor.copy(alpha = 0.12f)
                    )

                    // Animated Active Timer Circle
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.size(220.dp),
                        strokeWidth = 10.dp,
                        color = activePrimaryColor,
                        trackColor = Color.Transparent
                    )

                    // Timer Values
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = timerString,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 48.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("timer_countdown_label")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Session $sessionCount of $totalSessions",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // --- CLOCK BUTTON CONSOLE ---
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    FilledTonalIconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("reset_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Play/Pause FAB Button with custom luxury gradient
                    val buttonGradient = if (isBreakMode) FintrixTealGradient else FintrixOrangeGradient
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(buttonGradient)
                            .clickable {
                                if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                            }
                            .testTag("toggle_timer_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Skip Session Button
                    FilledTonalIconButton(
                        onClick = { viewModel.skipSession() },
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("skip_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Skip Session",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // --- CUSTOM DURATIONS CONFIGURATION CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FintrixCardGradient),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Custom Timer Settings ⚙️",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isRunning) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Pause or Reset the timer first to customize durations.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // --- FOCUS DURATION ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Focus Duration",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Choose study length in minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { viewModel.setFocusDuration(focusDurationMinutes - 5) },
                                modifier = Modifier.size(36.dp),
                                enabled = !isRunning
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Focus Duration",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$focusDurationMinutes m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.Center
                            )

                            FilledTonalIconButton(
                                onClick = { viewModel.setFocusDuration(focusDurationMinutes + 5) },
                                modifier = Modifier.size(36.dp),
                                enabled = !isRunning
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Focus Duration",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val focusPresets = listOf(15, 25, 45, 60)
                        focusPresets.forEach { preset ->
                            val isSelected = focusDurationMinutes == preset
                            SuggestionChip(
                                onClick = { viewModel.setFocusDuration(preset) },
                                label = { Text("$preset min") },
                                enabled = !isRunning,
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // --- BREAK DURATION ---
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Break Duration",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Choose break length in minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { viewModel.setBreakDuration(breakDurationMinutes - 1) },
                                modifier = Modifier.size(36.dp),
                                enabled = !isRunning
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease Break Duration",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$breakDurationMinutes m",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(48.dp),
                                textAlign = TextAlign.Center
                            )

                            FilledTonalIconButton(
                                onClick = { viewModel.setBreakDuration(breakDurationMinutes + 1) },
                                modifier = Modifier.size(36.dp),
                                enabled = !isRunning
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase Break Duration",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Preset Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val breakPresets = listOf(3, 5, 10, 15)
                        breakPresets.forEach { preset ->
                            val isSelected = breakDurationMinutes == preset
                            SuggestionChip(
                                onClick = { viewModel.setBreakDuration(preset) },
                                label = { Text("$preset min") },
                                enabled = !isRunning,
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                    labelColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // --- METHODOLOGY EXPLAINER CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(FintrixCardGradient),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Understanding Pomodoro 💡",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "The Pomodoro Technique is simple but highly effective: study for a focus session, then take a short break. Accomplish 4 cycles, then take a longer 15-30 minute break.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Automated logs: focus session completion automatically deposits your customized focus minutes to your today stats!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
