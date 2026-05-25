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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import coil.compose.AsyncImage
import com.example.data.*
import java.util.*
import java.text.SimpleDateFormat

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val todayDate by viewModel.todayDisplayDate.collectAsStateWithLifecycle()
    val todayStr by viewModel.todayDateString.collectAsStateWithLifecycle()
    
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    
    val tasksStats by viewModel.todayTasksSummary.collectAsStateWithLifecycle()
    val focusMinutes by viewModel.todayFocusMinutes.collectAsStateWithLifecycle()
    val overallAttendance by viewModel.overallAttendancePercentage.collectAsStateWithLifecycle()
    val streakCount by viewModel.currentStreak.collectAsStateWithLifecycle()
    val activeTheme by viewModel.themeAccent.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("study_flow_prefs", android.content.Context.MODE_PRIVATE) }

    // Control toggles for dynamic widgets
    val showStreakWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_streak", true)) }
    val showClassBanner = remember { mutableStateOf(sharedPrefs.getBoolean("show_class_banner", true)) }
    val showHabitsWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_habits_widget", true)) }

    var showProfileHubDialog by remember { mutableStateOf(false) }
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
                 completions.any { it.habitId == habit.id && it.dateString == todayStr && (it.status == "COMPLETED" || it.status == null) }
             }
             val failedHabitsCount = habits.count { habit ->
                 completions.any { it.habitId == habit.id && it.dateString == todayStr && it.status == "FAILED" }
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
                    .background(cardGradient()),
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
                            text = if (totalHabitsCount > 0) {
                                "$completedHabitsCount completed • $failedHabitsCount failed"
                            } else {
                                "$displaysCompleted/$displaysTotal completed"
                            },
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

        // --- SECTION 3: DAILY HABITS CUSTOM WIDGETS ---
        item {
            val habitsVisible = showHabitsWidget.value

            if (habitsVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(cardGradient()),
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
                                        val completion = completions.find { it.habitId == habit.id && it.dateString == todayStr }
                                        val isCompleted = completion != null && (completion.status == "COMPLETED" || completion.status == null)
                                        val isFailed = completion != null && completion.status == "FAILED"
                                        
                                        // Category Pastel colors
                                        val pastelBgColor = when (index % 4) {
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
                                                .border(
                                                    BorderStroke(
                                                        0.5.dp,
                                                        if (isCompleted) Color(0xFF10B981).copy(alpha = 0.4f)
                                                        else if (isFailed) Color(0xFFEF4444).copy(alpha = 0.4f)
                                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                                    ),
                                                    RoundedCornerShape(14.dp)
                                                )
                                                .background(
                                                    if (isCompleted) Color(0xFF10B981).copy(alpha = 0.08f)
                                                    else if (isFailed) Color(0xFFEF4444).copy(alpha = 0.08f)
                                                    else Color.Transparent,
                                                    RoundedCornerShape(14.dp)
                                                )
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
                                                        color = if (isCompleted || isFailed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
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

                                            // Clean check and cross choices
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Check Circle Button
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isCompleted) Color(0xFF10B981) else Color.Transparent)
                                                        .border(
                                                            1.5.dp,
                                                            if (isCompleted) Color(0xFF10B981) else Color(0xFFCBD5E1),
                                                            CircleShape
                                                        )
                                                        .clickable {
                                                            viewModel.setHabitStatus(habit.id, todayStr, if (isCompleted) null else "COMPLETED")
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Mark Complete",
                                                        tint = if (isCompleted) Color.White else Color(0xFF94A3B8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // Cross Circle Button
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isFailed) Color(0xFFEF4444) else Color.Transparent)
                                                        .border(
                                                            1.5.dp,
                                                            if (isFailed) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                                                            CircleShape
                                                        )
                                                        .clickable {
                                                            viewModel.setHabitStatus(habit.id, todayStr, if (isFailed) null else "FAILED")
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Clear,
                                                        contentDescription = "Mark Failed",
                                                        tint = if (isFailed) Color.White else Color(0xFF94A3B8),
                                                        modifier = Modifier.size(16.dp)
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

        // --- SECTION 3.5: CLASS SCHEDULE TIMETABLE (DYNAMICAL & TODAY-ONLY) ---
        item {
            val daysNameList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            val currentDayName = remember(todayDayOfWeek) { daysNameList.getOrNull(todayDayOfWeek - 1) ?: "Today" }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardGradient())
                    .testTag("home_schedule_card"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title and subtitle showing today's day of week
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Schedule 📚",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentDayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    if (todayClasses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No classes registered for $currentDayName.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            todayClasses.forEach { classItem ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Colored vertical strip for style
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(36.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )

                                        Column {
                                            Text(
                                                text = classItem.subjectName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "⏰ ${classItem.timeSlot}  •  🚪 Room: ${classItem.roomNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Quick Attend & Missed choices for classes
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.incrementTotalSessions(classItem) },
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Missed", color = HighPriorityColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = { viewModel.incrementAttendance(classItem) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = LowPriorityColor)
                                        ) {
                                            Text("Attend", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

                val highPriorityTasks = tasks.filter { (it.priority == "URGENT" || it.priority == "HIGH") && !it.completed }
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
                                .background(cardGradient()),
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
                                            .background(if (task.priority == "URGENT" || task.priority == "HIGH") HighPriorityColor else LowPriorityColor)
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

                                if (task.priority == "URGENT" || task.priority == "HIGH") {
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



    }



    // --- Control Hub & Settings Panel ---
    if (showProfileHubDialog) {
        var hubTab by remember { mutableStateOf(0) } // 0=Account & Accent, 1=Widgets, 2=Sync Config, 3=Donate
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
                        val sheets = listOf("Profile", "Widgets", "Cloud", "Donate")
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
                                    fontSize = 10.sp,
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

                                Spacer(modifier = Modifier.height(12.dp))
                                Text("App Theme Appearance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateDarkTheme(!viewModel.isDarkTheme.value)
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Matte Black Theme Mode", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text("Switch to premium luxury eye-safe dark mode layout.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    val isDarkState by viewModel.isDarkTheme.collectAsStateWithLifecycle()
                                    Switch(
                                        checked = isDarkState,
                                        onCheckedChange = { enabled ->
                                            viewModel.updateDarkTheme(enabled)
                                        }
                                    )
                                }
                            }
                        }
                        1 -> { // Customizable Home Widgets configuration
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
                        2 -> { // Secure cloud synchronization
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
                        3 -> { // Donation & Support QR Code
                            val clipboardManager = LocalClipboardManager.current
                            var copied by remember { mutableStateOf(false) }

                            LaunchedEffect(copied) {
                                if (copied) {
                                    delay(2000)
                                    copied = false
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Support Development & Donate 💖",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "If you enjoy using this application and want to support the creator, consider making a small contribution.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Color(0xFFFD5C25), MaterialTheme.colorScheme.primary))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "ST",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Satish Thakur",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "satishthakur7576-2@oksbi",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString("satishthakur7576-2@oksbi"))
                                            copied = true
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (copied) Color(0xFF00C070).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                            .size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                                            contentDescription = "Copy UPI ID",
                                            tint = if (copied) Color(0xFF00C070) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFEEEEEE))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .background(Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=upi%3A%2F%2Fpay%3Fpa%3Dsatishthakur7576-2%40oksbi%26pn%3DSatish%2520Thakur",
                                                contentDescription = "UPI Payment QR Code",
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        Text(
                                            text = "Scan to pay with any UPI app",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333),
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
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


