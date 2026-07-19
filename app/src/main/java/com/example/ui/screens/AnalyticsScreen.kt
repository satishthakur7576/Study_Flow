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
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle(emptyList())
    val weeklyFocusGoalHours by viewModel.weeklyFocusGoalHours.collectAsStateWithLifecycle(10)
    val focusRecords by viewModel.focusRecords.collectAsStateWithLifecycle(emptyList())

    val todayDateString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val completedHabitsCount = remember(completions, todayDateString) {
        completions.count { it.dateString == todayDateString && (it.status == "COMPLETED" || it.status == null) }
    }
    val completedTasksCount = remember(tasks, todayDateString) {
        tasks.count { it.completed && it.dueDate == todayDateString }
    }

    val dynamicColors = remember(activeTheme) { getDynamicColors(activeTheme) }

    var currentAnalyticsTab by remember { mutableStateOf(0) } // 0 = Focus, 1 = Tasks, 2 = Habits
    var showContributionExplanationDialog by remember { mutableStateOf(false) }

    val todayDayOfWeek = remember {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        if (day == Calendar.SUNDAY) 7 else day - 1
    }

    val activeDaysSet = remember(completions, focusRecords, tasks) {
        val compDates = completions.filter { it.status == "COMPLETED" || it.status == null }.map { it.dateString }
        val focusDates = focusRecords.filter { it.durationMinutes > 0 }.map { it.dateString }
        val taskDates = tasks.filter { it.completed }.map { it.dueDate }
        (compDates + focusDates + taskDates).toSet()
    }
    val activeDaysCount = activeDaysSet.size

    val streakStats = remember(activeDaysSet) {
        if (activeDaysSet.isEmpty()) Pair(0, 0)
        else {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val sortedDates = activeDaysSet.mapNotNull {
                try { sdf.parse(it) } catch (e: Exception) { null }
            }.sorted()
            
            if (sortedDates.isEmpty()) Pair(0, 0)
            else {
                var longestStreak = 0
                var tempStreak = 0
                
                var currentStreak = 0
                val curCal = Calendar.getInstance()
                var checkDateStr = sdf.format(curCal.time)
                if (activeDaysSet.contains(checkDateStr)) {
                    currentStreak++
                    curCal.add(Calendar.DAY_OF_YEAR, -1)
                    checkDateStr = sdf.format(curCal.time)
                    while (activeDaysSet.contains(checkDateStr)) {
                        currentStreak++
                        curCal.add(Calendar.DAY_OF_YEAR, -1)
                        checkDateStr = sdf.format(curCal.time)
                    }
                } else {
                    curCal.add(Calendar.DAY_OF_YEAR, -1)
                    checkDateStr = sdf.format(curCal.time)
                    while (activeDaysSet.contains(checkDateStr)) {
                        currentStreak++
                        curCal.add(Calendar.DAY_OF_YEAR, -1)
                        checkDateStr = sdf.format(curCal.time)
                    }
                }
                
                var prevDate: java.util.Date? = null
                sortedDates.forEach { date ->
                    if (prevDate == null) {
                        tempStreak = 1
                    } else {
                        val diffMs = date.time - prevDate!!.time
                        val diffDays = diffMs / (1000 * 60 * 60 * 24)
                        if (diffDays <= 1) {
                            tempStreak++
                        } else {
                            if (tempStreak > longestStreak) {
                                longestStreak = tempStreak
                            }
                            tempStreak = 1
                        }
                    }
                    prevDate = date
                }
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
                
                Pair(currentStreak, maxOf(longestStreak, currentStreak))
            }
        }
    }
    val currentStreak = streakStats.first
    val longestStreak = streakStats.second

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 680.dp)
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
                                        if (isActive) Modifier.background(dynamicColors.gradientBrush) else Modifier
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
                        0 -> dynamicColors.primaryColor
                        1 -> MaterialTheme.colorScheme.secondary
                        else -> dynamicColors.primaryColor
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

        // --- SECTION 2: DAYS LEFT IN CURRENT YEAR ---
        item {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val isLeapYear = (currentYear % 4 == 0 && currentYear % 100 != 0) || (currentYear % 400 == 0)
            val totalDaysInYear = if (isLeapYear) 366 else 365
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val daysLeft = totalDaysInYear - dayOfYear
            val percentPassed = (dayOfYear * 100) / totalDaysInYear

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardGradient()),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Days Left in $currentYear 📅",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Every square represents one day of this year.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        // Small badge with percentage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(dynamicColors.primaryColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$percentPassed% Gone",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = dynamicColors.primaryColor,
                                softWrap = false,
                                maxLines = 1
                            )
                        }
                    }

                    // Numeric stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$daysLeft",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = dynamicColors.primaryColor
                            )
                            Text(
                                text = "Days Left",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$dayOfYear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Days Passed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalDaysInYear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total Days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                    // The grid of squares
                    val columns = 28
                    val rows = (totalDaysInYear + columns - 1) / columns
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (r in 0 until rows) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (c in 0 until columns) {
                                    val dayIndex = r * columns + c + 1
                                    if (dayIndex <= totalDaysInYear) {
                                        val isPassed = dayIndex < dayOfYear
                                        val isToday = dayIndex == dayOfYear
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(6.5.dp)
                                                .clip(RoundedCornerShape(1.5.dp))
                                                .background(
                                                    if (isPassed) {
                                                        dynamicColors.primaryColor
                                                    } else if (isToday) {
                                                        Color(0xFFFD5C25) // Vibrant highlight for today
                                                    } else {
                                                        if (isDarkThemeActive) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                                                    }
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isPassed) {
                                                        Color.Transparent
                                                    } else if (isToday) {
                                                        Color(0xFFFD5C25)
                                                    } else {
                                                        if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0)
                                                    },
                                                    shape = RoundedCornerShape(1.5.dp)
                                                )
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.size(6.5.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
















        item {
            DonateSection()
        }
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
    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp, bottom = 18.dp)
    ) {
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val gridRows = 4
            val chartHeight = height * 0.82f // Leave 18% at the top for labels / padding
            val safeMax = if (maxValue == 0f) 1f else maxValue

            // 1. Draw horizontal gridlines and vertical axis labels on the right edge
            for (i in 0..gridRows) {
                val y = (height - chartHeight) + i * (chartHeight / gridRows)
                val gridVal = safeMax * (gridRows - i) / gridRows
                val labelText = if (unit == "%") "${gridVal.toInt()}%" else "%.1f%s".format(gridVal, unit)

                drawLine(
                    color = onSurfaceColor.copy(alpha = 0.08f),
                    start = Offset(x = 0f, y = y),
                    end = Offset(x = width - 42.dp.toPx(), y = y),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    width - 34.dp.toPx(),
                    y + 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = onSurfaceColor.copy(alpha = 0.45f).toArgb()
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                    }
                )
            }

            // 2. Compute margins and bar positions
            val totalBars = days.size
            if (totalBars == 0) return@Canvas

            val barSpacing = 14.dp.toPx()
            val availableBarWidth = (width - 46.dp.toPx()) - (barSpacing * (totalBars + 1))
            val barWidth = availableBarWidth / totalBars

            val maxVal = values.maxOrNull() ?: 0f
            val maxIndex = if (maxVal > 0f) values.indexOf(maxVal) else -1

            for (i in 0 until totalBars) {
                val day = days[i]
                val currentRawVal = values.getOrNull(i) ?: 0f
                val currentAnimatedVal = currentRawVal * transitionProgress.value

                // Draw background track for each column
                val barLeft = barSpacing + i * (barWidth + barSpacing)
                drawRoundRect(
                    color = onSurfaceColor.copy(alpha = 0.03f),
                    topLeft = Offset(x = barLeft, y = 0f),
                    size = Size(width = barWidth, height = height),
                    cornerRadius = CornerRadius(x = 6.dp.toPx(), y = 6.dp.toPx())
                )

                // Limit maximum drawing heights
                val normalizedValue = if (currentAnimatedVal > safeMax) safeMax else currentAnimatedVal
                val percentHeight = normalizedValue / safeMax
                val barActualHeight = chartHeight * percentHeight
                val barTop = height - barActualHeight

                val isHighlighted = maxIndex != -1 && i == maxIndex && currentRawVal > 0f
                val brushColors = if (isHighlighted) {
                    listOf(barColor.copy(alpha = 0.95f), barColor.copy(alpha = 0.7f))
                } else {
                    listOf(barColor.copy(alpha = 0.6f), barColor.copy(alpha = 0.3f))
                }

                // Draw standard Material card bars with nice rounded top-corners
                drawRoundRect(
                    brush = Brush.verticalGradient(colors = brushColors),
                    topLeft = Offset(x = barLeft, y = barTop),
                    size = Size(width = barWidth, height = barActualHeight),
                    cornerRadius = CornerRadius(x = 6.dp.toPx(), y = 6.dp.toPx())
                )

                if (isHighlighted && currentAnimatedVal > 0f) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = 2.5.dp.toPx(),
                        center = Offset(x = barLeft + barWidth / 2f, y = barTop + 4.dp.toPx())
                    )
                }

                // 3. Draw numerical badges over active bars
                if (currentAnimatedVal > 0f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        if (currentRawVal % 1f == 0f) currentRawVal.toInt().toString() else "%.1f".format(currentRawVal),
                        barLeft + barWidth / 2f,
                        if (barTop - 12.dp.toPx() < 12.dp.toPx()) 12.dp.toPx() else barTop - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = if (isHighlighted) barColor.toArgb() else onSurfaceColor.copy(alpha = 0.82f).toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = isHighlighted
                        }
                    )
                }

                // 4. Draw Days underneath on the bottom margin label
                drawContext.canvas.nativeCanvas.drawText(
                    day,
                    barLeft + barWidth / 2f,
                    height + 14.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = if (isHighlighted) barColor.toArgb() else onSurfaceColor.copy(alpha = 0.65f).toArgb()
                        textSize = 11.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isHighlighted
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
        
        // Find Sunday of current week in a robust, locale-independent way
        cal.firstDayOfWeek = Calendar.SUNDAY
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = currentDayOfWeek - Calendar.SUNDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
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

@Composable
fun AcademicDistributionCard(
    tasks: List<TaskEntity>,
    habits: List<HabitEntity>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Tasks by Subject, 1 = Habits by Category

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
                    text = "Thematic Distribution 🎨",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Understand your focus allocation across subjects and habits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Segmented selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Subjects", "Habit Categories")
                tabs.forEachIndexed { i, tab ->
                    val isActive = selectedTab == i
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (isActive) Modifier.background(Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF3B82F6)))) else Modifier
                            )
                            .clickable { selectedTab = i }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (selectedTab == 0) {
                // Tasks by Subject
                val subjectsMap = remember(tasks) {
                    tasks.groupBy { if (it.subject.trim().isEmpty()) "General" else it.subject.trim() }
                        .mapValues { entry ->
                            val total = entry.value.size
                            val completed = entry.value.count { it.completed }
                            val rate = if (total == 0) 0f else completed.toFloat() / total.toFloat()
                            Triple(completed, total, rate)
                        }.toList()
                        .sortedByDescending { it.second.second }
                }

                if (subjectsMap.isEmpty()) {
                    EmptyStatePlaceholder(text = "No tasks available to show subject analytics.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        subjectsMap.take(5).forEach { (subject, stats) ->
                            val (completed, total, rate) = stats
                            DistributionRow(
                                title = subject,
                                countText = "$completed/$total completed",
                                percentage = rate,
                                barColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            } else {
                // Habits by Category
                val categoriesMap = remember(habits) {
                    habits.groupBy { if (it.category.trim().isEmpty()) "Other" else it.category.trim() }
                        .mapValues { entry ->
                            entry.value.size
                        }.toList()
                        .sortedByDescending { it.second }
                }

                val totalHabits = remember(habits) { habits.size }

                if (categoriesMap.isEmpty()) {
                    EmptyStatePlaceholder(text = "No habits available to show category analytics.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categoriesMap.take(5).forEach { (category, count) ->
                            val rate = if (totalHabits == 0) 0f else count.toFloat() / totalHabits.toFloat()
                            DistributionRow(
                                title = category,
                                countText = "$count habits",
                                percentage = rate,
                                barColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionRow(
    title: String,
    countText: String,
    percentage: Float,
    barColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = countText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val animatedWidthFactor by animateFloatAsState(
            targetValue = percentage,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            label = "widthFactor"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = animatedWidthFactor.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(barColor.copy(alpha = 0.7f), barColor)
                        )
                    )
            )
        }
    }
}

@Composable
fun EmptyStatePlaceholder(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun WeeklyGoalProgressCard(
    weeklyFocusHours: Float,
    weeklyGoalHours: Int,
    onUpdateGoal: (Int) -> Unit,
    primaryColor: Color,
    gradientBrush: Brush
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Focus Target 🎯",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Set and crush your weekly study target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Active status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE GOAL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            // Interactive Progress Tracker Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Canvas Progress Arc
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val progressFraction = if (weeklyGoalHours <= 0) 1f else (weeklyFocusHours / weeklyGoalHours).coerceIn(0f, 1.5f)
                    
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction.coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "goalProgress"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background circle
                        drawArc(
                            color = primaryColor.copy(alpha = 0.08f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 8.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                        // Foreground dynamic progress arc
                        drawArc(
                            brush = gradientBrush,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 8.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (progressFraction >= 1f) "Crushed!" else "Focus",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (progressFraction >= 1f) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Stats and Interactive controls
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Hours Logged This Week:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "%.1fh".format(weeklyFocusHours),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/ ${weeklyGoalHours}h target",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    // Interactive Goal Adjuster Button Bar
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { if (weeklyGoalHours > 1) onUpdateGoal(weeklyGoalHours - 1) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease target hours",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "Goal",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        IconButton(
                            onClick = { onUpdateGoal(weeklyGoalHours + 1) },
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase target hours",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Dynamic Motivational Quote based on percentage
            val progressPercentage = if (weeklyGoalHours <= 0) 100 else (weeklyFocusHours / weeklyGoalHours * 100).toInt()
            val motivationText = when {
                progressPercentage == 0 -> "Let's kick things off! Hit the timer tab to log your first session. ⏰"
                progressPercentage < 25 -> "Good start! Focus is the doorway to absolute mastery. 🚪"
                progressPercentage < 50 -> "Steady progress! Keep feeding the fire of knowledge. 📚"
                progressPercentage < 75 -> "You're in the study groove! Fantastic momentum! ⚡"
                progressPercentage < 100 -> "So close! Finish the week strong like a champion! 🎯"
                else -> "Goal crushed! Focus Monk status achieved this week! 👑"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.05f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = motivationText,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DailyAcademicMilestones(
    todayFocusHours: Float,
    completedHabitsCount: Int,
    completedTasksCount: Int,
    primaryColor: Color
) {
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
                    text = "Daily Academic Milestones 🚀",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Unlock your achievements by building study consistency today.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Milestone Rows
            val todayFocusMinutes = todayFocusHours * 60
            val focusCompleted = todayFocusMinutes >= 30f
            val habitsCompleted = completedHabitsCount >= 1
            val tasksCompleted = completedTasksCount >= 1

            MilestoneItemRow(
                title = "Focus Spark (30m Study)",
                description = "Log at least 30 minutes of focus time.",
                progressText = "${todayFocusMinutes.toInt()}m / 30m",
                progressFraction = (todayFocusMinutes / 30f).coerceIn(0f, 1f),
                isCompleted = focusCompleted,
                primaryColor = primaryColor,
                emoji = "🧘"
            )

            MilestoneItemRow(
                title = "Habit Builder (1+ Habits)",
                description = "Complete at least one positive habit today.",
                progressText = "$completedHabitsCount completed",
                progressFraction = if (habitsCompleted) 1f else 0f,
                isCompleted = habitsCompleted,
                primaryColor = Color(0xFF10B981),
                emoji = "⚡"
            )

            MilestoneItemRow(
                title = "Academic Closer (1+ Tasks)",
                description = "Mark a task due today as completed.",
                progressText = "$completedTasksCount completed",
                progressFraction = if (tasksCompleted) 1f else 0f,
                isCompleted = tasksCompleted,
                primaryColor = Color(0xFFFD5C25),
                emoji = "🏅"
            )
        }
    }
}

@Composable
fun MilestoneItemRow(
    title: String,
    description: String,
    progressText: String,
    progressFraction: Float,
    isCompleted: Boolean,
    primaryColor: Color,
    emoji: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isCompleted) primaryColor.copy(alpha = 0.06f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = if (isCompleted) primaryColor.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon / Emoji Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) primaryColor.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }

        // Title and description
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) primaryColor else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = progressText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    label = "milestoneProgress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.7f), primaryColor)
                            )
                        )
                )
            }
        }

        // Checkmark badge
        if (isCompleted) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(primaryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun RecentFocusLogsCard(
    focusRecords: List<FocusRecordEntity>,
    primaryColor: Color
) {
    var expanded by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus Activity Logs 📜",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Chronological archive of focus sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "Collapse" else "View All",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            if (focusRecords.isEmpty()) {
                EmptyStatePlaceholder(text = "No focus records recorded yet. Let's study!")
            } else {
                val displayList = if (expanded) focusRecords.sortedByDescending { it.dateString } else focusRecords.sortedByDescending { it.dateString }.take(3)
                
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    displayList.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${record.durationMinutes} Minutes Session",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatLogDate(record.dateString),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+ ${(record.durationMinutes / 15).coerceAtLeast(1)} Pts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatLogDate(dateString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US)
        val date = parser.parse(dateString)
        date?.let { formatter.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

data class MilestoneBadgeData(
    val name: String,
    val description: String,
    val emoji: String,
    val unlocked: Boolean,
    val progressFraction: Float,
    val progressText: String
)

@Composable
fun PremiumMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    progress: Float
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(cardGradient()),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Small horizontal progress line inside metric card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                    label = "metricProgress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = animatedProgress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(iconColor)
                )
            }
        }
    }
}

@Composable
fun YearlyProductivityTrendChart(
    monthlyValues: List<Float>,
    primaryColor: Color,
    gradientBrush: Brush
) {
    val monthLabels = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
    val maxValue = remember(monthlyValues) {
        val max = monthlyValues.maxOrNull() ?: 10f
        if (max < 5f) 5f else max
    }

    val transitionProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlyValues) {
        transitionProgress.snapTo(0f)
        transitionProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(top = 8.dp, bottom = 12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                val gridRows = 3
                val chartHeight = height * 0.75f // Leave 25% at the top for spacing/labels
                val safeMax = if (maxValue == 0f) 1f else maxValue

                val leftMargin = 12.dp.toPx()
                val rightMargin = 42.dp.toPx()
                val chartWidth = width - leftMargin - rightMargin
                val totalPoints = monthlyValues.size // 12 months

                // 1. Draw horizontal gridlines and vertical axis labels on the right edge
                for (i in 0..gridRows) {
                    val y = (height - chartHeight) + i * (chartHeight / gridRows)
                    val gridVal = safeMax * (gridRows - i) / gridRows
                    val labelText = "%.1fh".format(gridVal)

                    drawLine(
                        color = onSurfaceColor.copy(alpha = 0.08f),
                        start = Offset(x = leftMargin, y = y),
                        end = Offset(x = width - rightMargin, y = y),
                        strokeWidth = 1.dp.toPx()
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        width - rightMargin + 8.dp.toPx(),
                        y + 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = onSurfaceColor.copy(alpha = 0.45f).toArgb()
                            textSize = 9.sp.toPx()
                            textAlign = android.graphics.Paint.Align.LEFT
                        }
                    )
                }

                if (totalPoints == 0) return@Canvas

                val xSpacing = chartWidth / (totalPoints - 1)

                // Calculate animated coordinates
                val points = List(totalPoints) { i ->
                    val x = leftMargin + i * xSpacing
                    val rawVal = monthlyValues.getOrNull(i) ?: 0f
                    val animatedVal = rawVal * transitionProgress.value
                    val percentHeight = (animatedVal / safeMax).coerceIn(0f, 1f)
                    val y = height - (chartHeight * percentHeight)
                    Offset(x, y)
                }

                // 2. Draw Area Path (under the line)
                val areaPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points[0].x, height)
                    points.forEach { point ->
                        lineTo(point.x, point.y)
                    }
                    lineTo(points.last().x, height)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.25f), primaryColor.copy(alpha = 0.0f)),
                        startY = height - chartHeight,
                        endY = height
                    )
                )

                // 3. Draw Trend Line Path
                val linePath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until totalPoints) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = primaryColor,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )

                // 4. Draw Glow Dots on active months
                points.forEachIndexed { index, point ->
                    val value = monthlyValues.getOrNull(index) ?: 0f
                    if (value > 0f) {
                        // Outer glow
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.25f),
                            radius = 6.dp.toPx(),
                            center = point
                        )
                        // Inner solid
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                        // Core white
                        drawCircle(
                            color = Color.White,
                            radius = 1.5.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }

        // X-Axis Labels (aligned perfectly to points)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 42.dp, start = 12.dp), // align with left and right margins of the chart
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            monthLabels.forEachIndexed { index, label ->
                val hasValue = monthlyValues.getOrNull(index) ?: 0f > 0f
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (hasValue) FontWeight.Bold else FontWeight.Normal,
                        color = if (hasValue) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun InsightRow(
    title: String,
    value: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

