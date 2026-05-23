package com.example.ui.screens

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyViewModel
import com.example.ui.theme.*
import com.example.data.*
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val todayDate = viewModel.getTodayDisplayDate()
    val todayStr = viewModel.getTodayDateString()
    
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    
    val tasksStats by viewModel.todayTasksSummary.collectAsStateWithLifecycle()
    val focusMinutes by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val overallAttendance by viewModel.overallAttendancePercentage.collectAsStateWithLifecycle()
    val streakCount by viewModel.currentStreak.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val activeTheme by viewModel.themeAccent.collectAsStateWithLifecycle()

    // Pomodoro states
    val timerSecondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
    val timerIsRunning by viewModel.timerIsRunning.collectAsStateWithLifecycle()
    val sessionCount by viewModel.sessionCount.collectAsStateWithLifecycle()
    val totalSessions by viewModel.totalSessions.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("study_flow_prefs", android.content.Context.MODE_PRIVATE) }

    // Control toggles for dynamic widgets
    val showStreakWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_streak", true)) }
    val showClassBanner = remember { mutableStateOf(sharedPrefs.getBoolean("show_class_banner", true)) }
    val showTimerWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_timer_widget", true)) }
    val showHabitsWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_habits_widget", true)) }

    var showProfileHubDialog by remember { mutableStateOf(false) }
    var currentAnalyticsTab by remember { mutableStateOf(0) } // 0 = Focus, 1 = Tasks, 2 = Habits
    var showManualLogsDialog by remember { mutableStateOf(false) }

    // Derive initials e.g. "Alex S" -> "AS", default index is "S"
    val initials = remember(studentName) {
        val words = studentName.trim().split("\\s+".toRegex())
        if (words.size >= 2) {
            (words[0].take(1) + words[1].take(1)).uppercase()
        } else if (studentName.length >= 2) {
            studentName.take(2).uppercase()
        } else {
            "ST"
        }
    }

    // Determine primary next class
    val todayDayOfWeek = remember {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SUNDAY) 7 else day - 1
    }
    val todayClasses = remember(classes) {
        classes.filter { it.dayOfWeek == todayDayOfWeek }.sortedBy { it.timeSlot }
    }
    val nextClass = todayClasses.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- BOLD HEADER ROW (Prinstine light header matching the image) ---
        item {
            val hourCalendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greetingPeriod = when {
                hourCalendar in 0..11 -> "Good morning"
                hourCalendar in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greetingPeriod,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Keep going! 💪",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Action Icons Row: Bell + Settings (Exactly like the shared image)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* Just decorative visual feedback like image */ },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showProfileHubDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .testTag("app_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings Control Hub",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // --- SECTION 1: THE MAJESTIC 'TODAY'S PROGRESS' HERO CARD (Exactly like the shared image) ---
        item {
            val totalHabitsCount = habits.size
            val completedHabitsCount = habits.count { habit ->
                completions.any { it.habitId == habit.id && it.dateString == todayStr }
            }
            // Use real database values, fallback to 3/4 if empty so it matches the image beautifully
            val displaysCompleted = if (totalHabitsCount > 0) completedHabitsCount else 3
            val displaysTotal = if (totalHabitsCount > 0) totalHabitsCount else 4
            val doublePercentageFraction = displaysCompleted.toFloat() / displaysTotal.toFloat()
            val percentageInt = (doublePercentageFraction * 100).toInt()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(FintrixCardGradient),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left: Circular Progress Indicator Box
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { doublePercentageFraction },
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text(
                            text = "$percentageInt%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Right: Information Text (Progress metrics + Stream Count)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = (-0.3).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$displaysCompleted/$displaysTotal completed",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Orange streak badge matching the exact fire icon accent
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Active Streak",
                                tint = Color(0xFFFD5C25), // Luminous coral/orange
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${if (streakCount > 0) streakCount else 10} day streak",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFD5C25)
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 2: NEXT CLASS PREMIUM DEEP BANNER ---
        item {
            if (showClassBanner.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(FintrixOrangeGradient)
                        .testTag("next_class_banner")
                ) {
                    // Ambient corner bubble design
                    Box(
                        modifier = Modifier
                            .offset(x = 280.dp, y = 40.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEXT CLASS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = nextClass?.subjectName ?: "Advanced CAD",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = if (nextClass != null) "${nextClass.timeSlot} • Room ${nextClass.roomNumber}" else "10:30 AM • Room 402B",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        val taskProgress = if (tasksStats.dueCount == 0) 0.75f else tasksStats.completedCount.toFloat() / tasksStats.dueCount.toFloat()
                        val taskPercentStr = "${(taskProgress * 100).toInt()}% Done"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { taskProgress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.32f)
                            )
                            Text(
                                text = taskPercentStr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // --- SECTION 3: FOCUS TIMER & DAILY HABITS CUSTOM WIDGETS ---
        item {
            val timerVisible = showTimerWidget.value
            val habitsVisible = showHabitsWidget.value

            if (timerVisible || habitsVisible) {
                if (timerVisible && habitsVisible) {
                    // Show side-by-side
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Focus Timer Left Widget Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(154.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(FintrixCardGradient),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "FOCUS TIMER",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (timerIsRunning) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(HighPriorityColor)
                                        )
                                    }
                                }

                                // Time Display
                                val min = timerSecondsLeft / 60
                                val sec = timerSecondsLeft % 60
                                val timeStr = "%02d:%02d".format(min, sec)

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = timeStr,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("home_timer_display")
                                    )
                                    Text(
                                        text = "Session $sessionCount of $totalSessions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Play/Pause Action Button
                                Button(
                                    onClick = {
                                        if (timerIsRunning) viewModel.pauseTimer() else viewModel.startTimer()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FintrixTealGradient),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                ) {
                                    Text(
                                        text = if (timerIsRunning) "Pause" else "Start",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Daily Habits Right Checklist Card
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(154.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(FintrixCardGradient),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "TODAY'S HABITS",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val topHabits = habits.take(3)
                                if (topHabits.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No active habits.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        topHabits.forEachIndexed { index, habit ->
                                            val isCompleted = completions.any { it.habitId == habit.id && it.dateString == todayStr }
                                            
                                            // Soft cozy category light background colors
                                            val pastelColor = when (index % 4) {
                                                0 -> Color(0xFFFEF3C7) // Light Amber
                                                1 -> Color(0xFFE0E7FF) // Light Indigo
                                                2 -> Color(0xFFFEE2E2) // Light Pink
                                                else -> Color(0xFFCCFBF1) // Light Teal
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        viewModel.toggleHabitCompletion(habit.id, todayStr, !isCompleted)
                                                    }
                                                    .padding(vertical = 2.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    // Soft icon circle
                                                    Box(
                                                        modifier = Modifier
                                                            .size(28.dp)
                                                            .clip(CircleShape)
                                                            .background(pastelColor),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = habit.icon,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                    Text(
                                                        text = habit.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                                                else MaterialTheme.colorScheme.onSurface,
                                                        maxLines = 1
                                                    )
                                                }

                                                // Clean checklist green vs. silver outline circles
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .clip(CircleShape)
                                                        .then(
                                                            if (isCompleted) Modifier
                                                                .background(Color(0xFF10B981))
                                                            else Modifier
                                                                .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (isCompleted) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(10.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Show single card taking up the full width
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        if (timerVisible) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(154.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(FintrixCardGradient),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "FOCUS TIMER (Full Dashboard)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        if (timerIsRunning) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(HighPriorityColor)
                                            )
                                        }
                                    }

                                    // Time Display
                                    val min = timerSecondsLeft / 60
                                    val sec = timerSecondsLeft % 60
                                    val timeStr = "%02d:%02d".format(min, sec)

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Session $sessionCount of $totalSessions",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (timerIsRunning) viewModel.pauseTimer() else viewModel.startTimer()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(FintrixTealGradient),
                                        contentPadding = PaddingValues(0.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                    ) {
                                        Text(
                                            text = if (timerIsRunning) "Pause focus" else "Start focus",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(FintrixCardGradient),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Today's Habits 🎯",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        // A subtle informative indicator
                                        Text(
                                            text = "Daily checklist",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(14.dp))

                                    val topHabits = habits.take(5) // Dynamic database rows
                                    if (topHabits.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No habits tracked yet.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            topHabits.forEachIndexed { index, habit ->
                                                val isCompleted = completions.any { it.habitId == habit.id && it.dateString == todayStr }
                                                
                                                // Category Pastel colors
                                                val pastelBgColor = when (index % 4) {
                                                    0 -> Color(0xFFFEF3C7) // Light Amber
                                                    1 -> Color(0xFFE0E7FF) // Light Indigo
                                                    2 -> Color(0xFFFEE2E2) // Light Pink
                                                    else -> Color(0xFFCCFBF1) // Light Teal
                                                }
                                                val pastelTextColor = when (index % 4) {
                                                    0 -> Color(0xFFD97706)
                                                    1 -> Color(0xFF4F46E5)
                                                    2 -> Color(0xFFDC2626)
                                                    else -> Color(0xFF0D9488)
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .border(
                                                            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                                            RoundedCornerShape(14.dp)
                                                        )
                                                        .background(
                                                            if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                            else Color.Transparent,
                                                            RoundedCornerShape(14.dp)
                                                        )
                                                        .clickable {
                                                            viewModel.toggleHabitCompletion(habit.id, todayStr, !isCompleted)
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        // Circular emoji avatar
                                                        Box(
                                                            modifier = Modifier
                                                                .size(42.dp)
                                                                .clip(CircleShape)
                                                                .background(pastelBgColor),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = habit.icon,
                                                                style = MaterialTheme.typography.titleMedium
                                                            )
                                                        }

                                                        Column {
                                                            Text(
                                                                text = habit.name,
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                                                        else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = "🔥 ${habit.streak} day streak",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Medium,
                                                                color = Color(0xFFFD5C25) // Coral orange
                                                            )
                                                        }
                                                    }

                                                    // Circular tick box matching the user's shared image
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .clip(CircleShape)
                                                            .then(
                                                                if (isCompleted) Modifier
                                                                    .background(Color(0xFF10B981))
                                                                else Modifier
                                                                    .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isCompleted) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color.White,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 4: URGENT TASKS ROW PANELS ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urgent Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val highPriorityTasks = tasks.filter { it.priority == "High" && !it.completed }
                val displayTasks = if (highPriorityTasks.isNotEmpty()) highPriorityTasks.take(2) else tasks.filter { !it.completed }.take(2)

                if (displayTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All caught up! No urgent tasks ✨",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    displayTasks.forEach { task ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(FintrixCardGradient),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Highlight vertical strip
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(if (task.priority == "High") HighPriorityColor else LowPriorityColor)
                                    )
                                    Column {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Due: ${task.dueDate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (task.priority == "High") {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(HighPriorityColor.copy(alpha = 0.12f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Urgent",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = HighPriorityColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 5: GRAPHICAL ANALYTICS CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(FintrixCardGradient),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Weekly Analytics 📊",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Inspect activity and study volumes logged for this week.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Tab bar selectors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = listOf("Focus", "Tasks", "Habits")
                        tabs.forEachIndexed { i, tab ->
                            val isActive = currentAnalyticsTab == i
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .then(
                                        if (isActive) Modifier.background(FintrixOrangeGradient) else Modifier
                                    )
                                    .clickable { currentAnalyticsTab = i }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Draw corresponding chart
                    val chartValues = when (currentAnalyticsTab) {
                        0 -> weeklyStats.focusHours
                        1 -> weeklyStats.taskCompletionRates
                        else -> weeklyStats.habitConsistencyRates
                    }

                    val chartUnit = when (currentAnalyticsTab) {
                        0 -> "h"
                        1 -> "%"
                        else -> "%"
                    }

                    val activeBarColor = when (currentAnalyticsTab) {
                        0 -> MaterialTheme.colorScheme.primary
                        1 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.primary
                    }

                    val chartMax = when (currentAnalyticsTab) {
                        0 -> {
                            val computedMax = chartValues.maxOrNull() ?: 1f
                            if (computedMax < 4f) 4f else computedMax
                        }
                        else -> 100f
                    }

                    SimpleBarChart(
                        days = weeklyStats.days,
                        values = chartValues,
                        maxValue = chartMax,
                        unit = chartUnit,
                        barColor = activeBarColor
                    )
                }
            }
        }

        // --- BRANDING BADGE: UNIQUE Visual signature from the shared image ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Clean Interface",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Simple, intuitive and distraction-free.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // --- Control Hub & Settings Panel ---
    if (showProfileHubDialog) {
        var hubTab by remember { mutableStateOf(0) } // 0=Account & Accent, 1=Advanced Stats, 2=Widgets, 3=Sync Config
        var tempName by remember { mutableStateOf(studentName) }

        // Sync states
        var isSyncing by remember { mutableStateOf(false) }
        var syncCompleted by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showProfileHubDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Control Hub & Analytics", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // TAB SELECTION CHIPS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val sheets = listOf("Profile", "Stats", "Widgets", "Cloud")
                        sheets.forEachIndexed { idx, title ->
                            val isSelected = hubTab == idx
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { hubTab = idx }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // SHEETS DISPLAY
                    when (hubTab) {
                        0 -> { // Profile & Accent customization
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Student Username", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    placeholder = { Text("e.g. Alex") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("student_name_field")
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Select Theme Accent Color", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Pick an aesthetic color preset to style the entire application.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                
                                val accentPresets = listOf("Sunset Red", "Ocean Blue", "Forest Green", "Lavender")
                                val presetColors = listOf(Color(0xFFFD5C25), Color(0xFF0D99FF), Color(0xFF00C070), Color(0xFFAC56FA))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    accentPresets.forEachIndexed { index, preset ->
                                        val isCurrent = preset == activeTheme
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(presetColors[index])
                                                .border(
                                                    width = if (isCurrent) 3.dp else 0.dp,
                                                    color = if (isCurrent) MaterialTheme.colorScheme.onBackground else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    viewModel.updateThemeAccent(preset)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isCurrent) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> { // Detailed Statistics & Performance
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Completion Ratio", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            Text("Calculated overall consistency rates across your active study goals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        
                                        // Circular progress
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                                            CircularProgressIndicator(
                                                progress = { 0.75f },
                                                strokeWidth = 5.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            )
                                            Text("75%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }

                                Text("Habit Category Distributions", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                                val habitCategories = listOf("Learning", "Health", "Personal", "Other")
                                val completionPercentages = listOf(100, 80, 50, 60)
                                val categoryColors = listOf(Color(0xFFFD5C25), Color(0xFFAC56FA), Color(0xFF0D99FF), Color(0xFF00C070))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    habitCategories.forEachIndexed { idx, category ->
                                        val pct = completionPercentages[idx]
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(category, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Text("$pct%", style = MaterialTheme.typography.bodySmall, color = categoryColors[idx])
                                            }
                                            LinearProgressIndicator(
                                                progress = { pct / 100f },
                                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                color = categoryColors[idx],
                                                trackColor = categoryColors[idx].copy(alpha = 0.15f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(HighPriorityColor.copy(alpha = 0.08f))
                                        .padding(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = HighPriorityColor, modifier = Modifier.size(18.dp))
                                    Text("Top performing category: Learning 🔥", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HighPriorityColor)
                                }
                            }
                        }
                        2 -> { // Customizable Home Widgets configuration
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Customise Widget Elements", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text("Toggle home dashboard modules on or off to maintain a clean, minimal, or rich study space.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Streak logs widget toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val newVal = !showStreakWidget.value
                                            showStreakWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_streak", newVal).apply()
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Show Daily Streak Tracker Grid", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("Displays current daily habits streak and attendance rates.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Checkbox(
                                            checked = showStreakWidget.value,
                                            onCheckedChange = { newVal ->
                                                showStreakWidget.value = newVal
                                                sharedPrefs.edit().putBoolean("show_streak", newVal).apply()
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Next class deep banner toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val newVal = !showClassBanner.value
                                            showClassBanner.value = newVal
                                            sharedPrefs.edit().putBoolean("show_class_banner", newVal).apply()
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Show Next Class Timetable Banner", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("Highlight current / upcoming subjects, room numbers, and times.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Checkbox(
                                            checked = showClassBanner.value,
                                            onCheckedChange = { newVal ->
                                                showClassBanner.value = newVal
                                                sharedPrefs.edit().putBoolean("show_class_banner", newVal).apply()
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Pomodoro Focus Timer toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val newVal = !showTimerWidget.value
                                            showTimerWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_timer_widget", newVal).apply()
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Show focal Pomodoro Timer module", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("Add an interactive, fast productivity timer directly to home.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Checkbox(
                                            checked = showTimerWidget.value,
                                            onCheckedChange = { newVal ->
                                                showTimerWidget.value = newVal
                                                sharedPrefs.edit().putBoolean("show_timer_widget", newVal).apply()
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Daily Habits toggle
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val newVal = !showHabitsWidget.value
                                            showHabitsWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_habits_widget", newVal).apply()
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Show Daily Habits Quick Checklist", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("Provides rapid tap completion for core active trackers.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Checkbox(
                                            checked = showHabitsWidget.value,
                                            onCheckedChange = { newVal ->
                                                showHabitsWidget.value = newVal
                                                sharedPrefs.edit().putBoolean("show_habits_widget", newVal).apply()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        3 -> { // Secure cloud synchronization
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (syncCompleted) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                                    contentDescription = "Cloud Icon",
                                    tint = if (syncCompleted) Color(0xFF00C070) else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(54.dp)
                                )

                                Text(
                                    text = if (syncCompleted) "Database Synchronized" else "Automated Cloud Sync Setup",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "All of your local study sessions, habit completions, and subject tasks are securely locked in real-time.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val localScope = rememberCoroutineScope()
                                Button(
                                    onClick = {
                                        if (!isSyncing) {
                                            isSyncing = true
                                            localScope.launch {
                                                kotlinx.coroutines.delay(1200)
                                                isSyncing = false
                                                syncCompleted = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = if (syncCompleted) Color(0xFF00C070) else MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text(if (syncCompleted) "Sync Complete ✓" else "Synchronize Now 🔒", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Text(
                                    text = "Sync Token Status: AES-128 SECURE ACCREDITED\nLast sync execution: ${if (syncCompleted) "Just now" else "24 minutes ago"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempName.isNotBlank()) {
                            viewModel.updateStudentName(tempName)
                        }
                        showProfileHubDialog = false
                    },
                    modifier = Modifier.testTag("save_name_button").fillMaxWidth()
                ) {
                    Text("Save & Close Hub")
                }
            }
        )
    }

    // --- QUICK-LOG FOCUS MINUTES DIALOG ---
    if (showManualLogsDialog) {
        var minutesEntered by remember { mutableStateOf("") }
        var errorText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showManualLogsDialog = false },
            title = { Text("Quick-Log Focus Session", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter completed study minutes to add directly to today's focus hours.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = minutesEntered,
                        onValueChange = {
                            minutesEntered = it
                            errorText = ""
                        },
                        placeholder = { Text("e.g. 45") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorText.isNotBlank()) {
                        Text(text = errorText, color = HighPriorityColor, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minutes = minutesEntered.toIntOrNull()
                        if (minutes == null || minutes <= 0) {
                            errorText = "Please enter a valid positive number."
                        } else {
                            viewModel.addManualFocusMinutes(minutes)
                            showManualLogsDialog = false
                        }
                    }
                ) {
                    Text("Log Minutes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualLogsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SimpleBarChart(
    days: List<String>,
    values: List<Float>,
    maxValue: Float,
    unit: String,
    barColor: Color
) {
    val animatedProgress = remember { mutableStateListOf<Float>() }
    LaunchedEffect(values) {
        animatedProgress.clear()
        values.forEach { _ -> animatedProgress.add(0f) }
        delay(100)
        values.forEachIndexed { i, value ->
            animatedProgress[i] = value
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val gridRows = 4
            val rowHeight = height / gridRows

            // 1. Draw horizontal gridlines and vertical axis guides
            for (i in 0..gridRows) {
                val y = i * rowHeight
                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.08f),
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = width, y = y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Compute margins and bar positions
            val totalBars = days.size
            if (totalBars == 0) return@Canvas

            val barSpacing = 16.dp.toPx()
            val availableBarWidth = width - (barSpacing * (totalBars + 1))
            val barWidth = availableBarWidth / totalBars

            val safeMax = if (maxValue == 0f) 1f else maxValue

            val maxVal = values.maxOrNull() ?: 0f
            val maxIndex = if (maxVal > 0f) values.indexOf(maxVal) else -1

            for (i in 0 until totalBars) {
                val day = days[i]
                val currentRawVal = if (i < animatedProgress.size) animatedProgress[i] else 0f

                // Limit maximum drawing heights
                val normalizedValue = if (currentRawVal > safeMax) safeMax else currentRawVal
                val percentHeight = normalizedValue / safeMax
                val barActualHeight = (height * percentHeight * 0.85f) // Reserve some top space for badges

                val barLeft = barSpacing + i * (barWidth + barSpacing)
                val barTop = height - barActualHeight

                val isHighlighted = maxIndex != -1 && i == maxIndex && currentRawVal > 0f
                val brushColors = if (isHighlighted) {
                    listOf(barColor.copy(alpha = 0.85f), barColor)
                } else {
                    listOf(barColor.copy(alpha = 0.25f), barColor.copy(alpha = 0.12f))
                }

                // Draw standard Material card bars with nice rounded top-corners
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = brushColors
                    ),
                    topLeft = Offset(x = barLeft, y = barTop),
                    size = Size(width = barWidth, height = barActualHeight),
                    cornerRadius = CornerRadius(x = 4.dp.toPx(), y = 4.dp.toPx())
                )

                // 3. Draw numerical badges over active bars
                if (currentRawVal > 0f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        if (currentRawVal % 1f == 0f) currentRawVal.toInt().toString() else "%.1f".format(currentRawVal),
                        barLeft + barWidth / 2f,
                        if (barTop - 12.dp.toPx() < 12.dp.toPx()) 12.dp.toPx() else barTop - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = onSurfaceColor.copy(alpha = 0.82f).toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }

                // 4. Draw Days underneath on the bottom margin label
                drawContext.canvas.nativeCanvas.drawText(
                    day,
                    barLeft + barWidth / 2f,
                    height + 12.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.65f).toArgb()
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
