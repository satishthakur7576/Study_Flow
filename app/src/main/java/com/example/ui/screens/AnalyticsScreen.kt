package com.example.ui.screens

import kotlinx.coroutines.delay
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.StudyViewModel
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.data.*
import java.util.*
import java.text.SimpleDateFormat

@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val weeklyStats by viewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val activeTheme by viewModel.themeAccent.collectAsStateWithLifecycle()
    val lifetimeStats by viewModel.lifetimeAnalytics.collectAsStateWithLifecycle()
    val dailyContributions by viewModel.dailyContributions.collectAsStateWithLifecycle()
    val isDarkThemeActive by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    var currentAnalyticsTab by remember { mutableStateOf(0) } // 0 = Focus, 1 = Tasks, 2 = Habits
    var showContributionExplanationDialog by remember { mutableStateOf(false) }

    val todayDayOfWeek = remember {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SUNDAY) 7 else day - 1
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_scroll_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: WEEKLY ANALYTICS CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardGradient()),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Weekly Analytics 📊",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Inspect activity and study volumes logged for this week.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // --- WEEKLY ANALYTICS SUB-VIEW ---
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

        // --- SECTION 2: LIFETIME PROGRESS CARD ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardGradient()),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Lifetime Progress 🏆",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your academic lifetime accomplishments and badges.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // --- LIFETIME ANALYTICS SUB-VIEW ---
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Trophy Room (${lifetimeStats.activeBadgesCount} / 5 Merits)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val badges = listOf(
                                Triple("Focus Initiate", "🌱", lifetimeStats.totalFocusMinutes > 0),
                                Triple("Focus Monk", "🧘", lifetimeStats.totalFocusMinutes >= 300),
                                Triple("High Achiever", "🏅", lifetimeStats.totalTasksCompleted >= 5),
                                Triple("Unstoppable", "⚡", lifetimeStats.totalHabitCompletions >= 10),
                                Triple("Committed Scholar", "🎓", lifetimeStats.overallAttendancePercentage >= 80f && lifetimeStats.totalClassesCount > 0)
                            )

                            badges.forEach { (name, emoji, unlocked) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (unlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = emoji, fontSize = 14.sp)
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (unlocked) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ContributionCalendarGrid(
                        contributions = dailyContributions,
                        todayDayOfWeek = todayDayOfWeek,
                        isDark = isDarkThemeActive,
                        onShowLearnMore = { showContributionExplanationDialog = true }
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Focus Space", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val totalH = lifetimeStats.totalFocusMinutes / 60
                                    val totalM = lifetimeStats.totalFocusMinutes % 60
                                    val focusDisplay = if (totalH > 0) "${totalH}h ${totalM}m" else "${totalM}m"
                                    Text(
                                        text = focusDisplay,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Cumulative hours", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Task Power", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val taskRatio = if (lifetimeStats.totalTasksCreated == 0) "0%" else {
                                        val pct = (lifetimeStats.totalTasksCompleted.toFloat() / lifetimeStats.totalTasksCreated.toFloat() * 100).toInt()
                                        "$pct%"
                                    }
                                    Text(
                                        text = taskRatio,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${lifetimeStats.totalTasksCompleted}/${lifetimeStats.totalTasksCreated} completed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Habit Reps", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${lifetimeStats.totalHabitCompletions} reps",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val habitNote = if (lifetimeStats.topHabitName != "None") {
                                        "Top: ${lifetimeStats.topHabitIcon} ${lifetimeStats.topHabitName}"
                                    } else {
                                        "Form consistency"
                                    }
                                    Text(
                                        text = habitNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFFFD5C25),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text("Schedule", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val attPct = "%.0f%%".format(lifetimeStats.overallAttendancePercentage)
                                    Text(
                                        text = attPct,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val classAttNote = if (lifetimeStats.totalClassesCount > 0) {
                                        "${lifetimeStats.totalClassesAttended}/${lifetimeStats.totalClassesCount} attended"
                                    } else {
                                        "No timetable logs"
                                    }
                                    Text(
                                        text = classAttNote,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        val motMessage = if (lifetimeStats.activeBadgesCount == 5) {
                            "Absolute legend! You've unlocked all 5 lifetime study badges! 👑"
                        } else if (lifetimeStats.activeBadgesCount >= 3) {
                            "Phenomenal consistency. Keep compiling focus and habit sessions!"
                        } else {
                            "Grow your progress! Unlock more merit badges by tracking classes, tasks and focus. 💪"
                        }
                        Text(
                            text = motMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            DonateSection()
        }
    }

    if (showContributionExplanationDialog) {
        AlertDialog(
            onDismissRequest = { showContributionExplanationDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "How we count contributions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Your daily contributions represent your active study and self-improvement efforts. They are compiled in real-time as follows:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🧘", fontSize = 20.sp)
                        Column {
                            Text(
                                "Focus Spaces",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Earn 1 point for every 15 minutes spent on focused study time. For example, a 60-minute Pomodoro session yields 4 points.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("⚡", fontSize = 20.sp)
                        Column {
                            Text(
                                "Habit Repetitions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Earn 1 point for each daily or customized habit completed and checked off for the day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🏅", fontSize = 20.sp)
                        Column {
                            Text(
                                "Task Completion",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Earn 1 point for each academic or personal task completed and finalized.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showContributionExplanationDialog = false }
                ) {
                    Text("Got it, thank you!", fontWeight = FontWeight.Bold)
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

@Composable
fun ContributionCalendarGrid(
    contributions: Map<String, Int>,
    todayDayOfWeek: Int,
    isDark: Boolean,
    onShowLearnMore: () -> Unit
) {
    // Generate 53 columns (weeks), each with 7 days (Sunday to Saturday)
    val columns = remember {
        val list = mutableListOf<List<CalendarDay>>()
        val cal = Calendar.getInstance()
        
        // Find Sunday of current week
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        // Move back 52 weeks to show exactly 53 weeks (full year + current week)
        cal.add(Calendar.WEEK_OF_YEAR, -52)
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val monthSdf = SimpleDateFormat("MMM", Locale.US)
        
        for (w in 0 until 53) {
            val daysInWeek = mutableListOf<CalendarDay>()
            for (d in 0 until 7) {
                val dateStr = sdf.format(cal.time)
                val monthName = monthSdf.format(cal.time)
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                daysInWeek.add(CalendarDay(dateStr, monthName, dayOfMonth))
                cal.add(Calendar.DAY_OF_YEAR, 1) // Increment by 1 day
            }
            list.add(daysInWeek)
        }
        list
    }

    // Determine the color of each square based on standard Git-style shades
    fun getCellColor(count: Int, isDarkTheme: Boolean): Color {
        return if (count <= 0) {
            if (isDarkTheme) Color(0xFF161B22) else Color(0x0F000000)
        } else if (count in 1..2) {
            if (isDarkTheme) Color(0xFF0E4429) else Color(0xFFC6E48B)
        } else if (count in 3..4) {
            if (isDarkTheme) Color(0xFF006D32) else Color(0xFF7BC96F)
        } else if (count in 5..6) {
            if (isDarkTheme) Color(0xFF26A641) else Color(0xFF239A3B)
        } else {
            if (isDarkTheme) Color(0xFF39D353) else Color(0xFF196127)
        }
    }

    // Horizontal ScrollState for contribution calendar grid
    val scrollState = rememberScrollState()
    
    // Auto scroll to latest date (far right) on load
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Scrollable Grid Layout (Month labels at top, Day of week labels on left)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Day titles column: padding top to clear the month headers space
            Column(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .height(95.dp), // 7 * 11.dp (squares) + 6 * 3.dp (gaps) = 95.dp exactly
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Sunday row -> blank
                Spacer(modifier = Modifier.height(11.dp))
                // Monday row -> "Mon"
                Text(
                    text = "Mon",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.height(11.dp)
                )
                // Tuesday row -> blank
                Spacer(modifier = Modifier.height(11.dp))
                // Wednesday row -> "Wed"
                Text(
                    text = "Wed",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.height(11.dp)
                )
                // Thursday row -> blank
                Spacer(modifier = Modifier.height(11.dp))
                // Friday row -> "Fri"
                Text(
                    text = "Fri",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.height(11.dp)
                )
                // Saturday row -> blank
                Spacer(modifier = Modifier.height(11.dp))
            }

            // Horizontally Scrollable Calendar Column containing month names + days matrix
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                // Row 1: Month labels at the exact column offset
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    columns.forEachIndexed { i, week ->
                        val firstDay = week.first()
                        val prevWeekFirstDay = if (i > 0) columns[i - 1].first() else null
                        
                        // Render months only at the threshold where transition happens
                        val showMonth = i == 0 || (prevWeekFirstDay != null && firstDay.monthName != prevWeekFirstDay.monthName)
                        
                        Box(
                            modifier = Modifier.width(14.dp) // cell width 11.dp + gap 3.dp = 14.dp
                        ) {
                            if (showMonth) {
                                Text(
                                    text = firstDay.monthName,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                                    modifier = Modifier.offset(y = (-4).dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Row 2: Grid matrix of cells
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    columns.forEach { week ->
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            week.forEach { day ->
                                val count = contributions[day.dateStr] ?: 0
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(getCellColor(count, isDark))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Bottom labels & legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Learn how we count contributions" clickable label
            Row(
                modifier = Modifier
                    .clickable(onClick = onShowLearnMore)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Learn how we count contributions",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }

            // Legend: Less [Sq0] [Sq1] [Sq2] [Sq3] [Sq4] More
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Less",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                listOf(0, 2, 4, 6, 8).forEach { mockCount ->
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(getCellColor(mockCount, isDark))
                    )
                }

                Text(
                    text = "More",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class CalendarDay(
    val dateStr: String,
    val monthName: String,
    val dayOfMonth: Int
)

@Composable
fun DonateSection() {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardGradient()),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Support Development & Donate 💖",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "If you enjoy using this application and want to support the creator, consider a contribution via UPI.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFFD5C25), MaterialTheme.colorScheme.primary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ST",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Satish Thakur",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (copied) Color(0xFF00C070).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Copy UPI ID",
                        tint = if (copied) Color(0xFF00C070) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=upi%3A%2F%2Fpay%3Fpa%3Dsatishthakur7576-2%40oksbi%26pn%3DSatish%2520Thakur",
                            contentDescription = "UPI Payment QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "Scan to pay with any UPI app",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
