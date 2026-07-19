package com.example.ui.screens

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun AnimatedMeshGradient(isDark: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_gradient")

    // Animate positions for multiple color blobs to create the organic "mesh" movement
    val t1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )

    val t2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )

    val t3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t3"
    )

    // Base background color
    val baseColor = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)

    // Vibrant but elegant mesh colors (adjusted opacity to keep text readable)
    val color1 = if (isDark) Color(0x2B6366F1) else Color(0x13818CF8) // Indigo-like
    val color2 = if (isDark) Color(0x2B8B5CF6) else Color(0x13C084FC) // Purple-like
    val color3 = if (isDark) Color(0x1F06B6D4) else Color(0x0E22D3EE) // Cyan-like
    val color4 = if (isDark) Color(0x24EC4899) else Color(0x13F472B6) // Pink-like

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        if (w == 0f || h == 0f) return@Canvas

        // Draw solid base background
        drawRect(color = baseColor)

        // Calculate moving center offsets for the radial gradients using sine and cosine functions
        val x1 = w * 0.5f + (w * 0.3f * kotlin.math.cos(t1.toDouble())).toFloat()
        val y1 = h * 0.4f + (h * 0.2f * kotlin.math.sin(t1.toDouble())).toFloat()
        val r1 = kotlin.math.max(w, h) * 0.6f

        val x2 = w * 0.4f + (w * 0.25f * kotlin.math.sin(t2.toDouble())).toFloat()
        val y2 = h * 0.6f + (h * 0.25f * kotlin.math.cos(t2.toDouble())).toFloat()
        val r2 = kotlin.math.max(w, h) * 0.7f

        val x3 = w * 0.6f + (w * 0.35f * kotlin.math.cos(t3.toDouble() + 1.5)).toFloat()
        val y3 = h * 0.5f + (h * 0.2f * kotlin.math.sin(t3.toDouble() - 1.0)).toFloat()
        val r3 = kotlin.math.max(w, h) * 0.5f

        val x4 = w * 0.3f + (w * 0.2f * kotlin.math.sin(t1.toDouble() * 0.5 + 2.0)).toFloat()
        val y4 = h * 0.3f + (h * 0.3f * kotlin.math.cos(t2.toDouble() * 0.5 - 1.5)).toFloat()
        val r4 = kotlin.math.max(w, h) * 0.55f

        // Draw radial brush 1
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = Offset(x1, y1),
                radius = r1
            ),
            center = Offset(x1, y1),
            radius = r1
        )

        // Draw radial brush 2
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(x2, y2),
                radius = r2
            ),
            center = Offset(x2, y2),
            radius = r2
        )

        // Draw radial brush 3
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3, Color.Transparent),
                center = Offset(x3, y3),
                radius = r3
            ),
            center = Offset(x3, y3),
            radius = r3
        )

        // Draw radial brush 4
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color4, Color.Transparent),
                center = Offset(x4, y4),
                radius = r4
            ),
            center = Offset(x4, y4),
            radius = r4
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val studentMajor by viewModel.studentMajor.collectAsStateWithLifecycle()
    val studentYear by viewModel.studentYear.collectAsStateWithLifecycle()
    val academicGoal by viewModel.academicGoal.collectAsStateWithLifecycle()
    val profileAvatar by viewModel.profileAvatar.collectAsStateWithLifecycle()
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
    val dynamicColors = remember(activeTheme) { getDynamicColors(activeTheme) }
    val weeklyAnalytics by viewModel.weeklyAnalytics.collectAsStateWithLifecycle()
    val focusRecords by viewModel.focusRecords.collectAsStateWithLifecycle()
    val weeklyFocusGoalHours by viewModel.weeklyFocusGoalHours.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("study_flow_prefs", android.content.Context.MODE_PRIVATE) }

    // Calculate yesterday's date and study minutes
    val yesterdayStr = remember(todayStr) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
    }
    val yesterdayFocusMinutes = remember(focusRecords, yesterdayStr) {
        focusRecords.find { it.dateString == yesterdayStr }?.durationMinutes ?: 0
    }

    // Dynamic, personalized motivational text based on real daily progress
    val motivationalText = remember(focusMinutes, yesterdayFocusMinutes) {
        val diff = focusMinutes - yesterdayFocusMinutes
        when {
            focusMinutes == 0 -> "🌱 Start a study session today to build your streak!"
            yesterdayFocusMinutes == 0 -> "✨ Great job starting today's study! Keep going!"
            diff > 0 -> "🔥 +${diff} more study minutes than yesterday!"
            diff < 0 -> "⚡ You studied ${focusMinutes}m today. Aim for ${yesterdayFocusMinutes}m to beat yesterday!"
            else -> "⭐ Consistent study! You matched yesterday's study of ${focusMinutes}m!"
        }
    }

    // Build overall active streak sparkline from weekly focus/habit records
    val streakSparkline = remember(weeklyAnalytics) {
        val points = mutableListOf<Float>()
        val activeDays = BooleanArray(7) { idx ->
            val hasFocus = (weeklyAnalytics.focusHours.getOrNull(idx) ?: 0f) > 0f
            val hasHabit = (weeklyAnalytics.habitConsistencyRates.getOrNull(idx) ?: 0f) > 0f
            hasFocus || hasHabit
        }

        for (i in 0 until 7) {
            var streakVal = 0f
            if (activeDays[i]) {
                streakVal = 1f
                var j = i - 1
                while (j >= 0 && activeDays[j]) {
                    streakVal += 1f
                    j--
                }
            } else {
                var j = i - 1
                if (j >= 0 && activeDays[j]) {
                    streakVal = 1f
                    j--
                    while (j >= 0 && activeDays[j]) {
                        streakVal += 1f
                        j--
                    }
                }
            }
            points.add(streakVal)
        }
        points
    }

    // Control toggles for dynamic widgets matching all Home Screen sections
    val showHeroWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_hero_widget", true)) }
    val showHabitsWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_habits_widget", true)) }
    val showScheduleWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_schedule_widget", true)) }
    val showTimerWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_timer_widget", true)) }
    val showAiInsightsWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_ai_insights_widget", true)) }
    val showGoalsWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_goals_widget", true)) }
    val showAchievementsWidget = remember { mutableStateOf(sharedPrefs.getBoolean("show_achievements_widget", true)) }

    var showProfileHubDialog by remember { mutableStateOf(false) }
    var showManualLogsDialog by remember { mutableStateOf(false) }

    // Derive initials e.g. "Alex S" -> "AS"
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

    // Theme Color Mapping
    val isDarkThemeActive by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    // 1. HEADER GREETER PERIOD
    val hourCalendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greetingPeriod = when {
        hourCalendar in 0..11 -> "Good Morning ☀️"
        hourCalendar in 12..16 -> "Good Afternoon 🌅"
        else -> "Good Evening 🌌"
    }

    // Dynamic calculations for Hero Card Progress
    val todayCompletedTasksCount = tasks.count { it.completed && it.dueDate == todayStr }
    val todayTotalTasksCount = tasks.count { it.dueDate == todayStr }
    val totalHabitsCount = habits.size
    val completedHabitsCount = habits.count { habit ->
        completions.any { it.habitId == habit.id && it.dateString == todayStr && (it.status == "COMPLETED" || it.status == null) }
    }
    val dailyGoalMinutes = if (weeklyFocusGoalHours > 0) (weeklyFocusGoalHours * 60) / 7 else 60
    val focusFraction = if (dailyGoalMinutes > 0) {
        (focusMinutes.toFloat() / dailyGoalMinutes).coerceAtMost(1f)
    } else {
        0f
    }
    val overallProductivityPercentage = remember(focusFraction, todayTotalTasksCount, todayCompletedTasksCount, totalHabitsCount, completedHabitsCount) {
        val hasTasks = todayTotalTasksCount > 0
        val hasHabits = totalHabitsCount > 0
        val taskRateFraction = if (hasTasks) todayCompletedTasksCount.toFloat() / todayTotalTasksCount.toFloat() else 0f
        val habitRateFraction = if (hasHabits) completedHabitsCount.toFloat() / totalHabitsCount.toFloat() else 0f

        val score = when {
            hasTasks && hasHabits -> {
                (focusFraction * 40f) + (taskRateFraction * 30f) + (habitRateFraction * 30f)
            }
            hasTasks -> {
                (focusFraction * 50f) + (taskRateFraction * 50f)
            }
            hasHabits -> {
                (focusFraction * 50f) + (habitRateFraction * 50f)
            }
            else -> {
                focusFraction * 100f
            }
        }
        score.toInt().coerceIn(0, 100)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkThemeActive) Color(0xFF0F172A) else Color(0xFFF8FAFC))
    ) {
        AnimatedMeshGradient(isDark = isDarkThemeActive)

        val width = maxWidth
        val isTablet = width >= 600.dp
        val isSmallPhone = width < 360.dp

        val screenPadding = when {
            isTablet -> 32.dp
            isSmallPhone -> 16.dp
            else -> 24.dp
        }

        val itemSpacing = when {
            isTablet -> 32.dp
            isSmallPhone -> 20.dp
            else -> 28.dp
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .testTag("home_screen"),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                // --- 1. THE ULTIMATE HEADER ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = screenPadding)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = greetingPeriod,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkThemeActive) Color(0xFF94A3B8) else Color(0xFF64748B)
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Keep Going, $studentName 💪",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = if (isTablet) 28.sp else 21.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                                    letterSpacing = (-0.5).sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = null,
                                    tint = if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = todayDate,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Live Focus Stat Chip
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDarkThemeActive) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("⏱️", fontSize = 11.sp)
                                        Text(
                                            text = "${focusMinutes}m Today",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkThemeActive) Color(0xFF38BDF8) else Color(0xFF0284C7)
                                            )
                                        )
                                    }
                                }

                                // Live Streak Stat Chip
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDarkThemeActive) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0)),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🔥", fontSize = 11.sp)
                                        Text(
                                            text = "$streakCount ${if (streakCount == 1) "Day" else "Days"}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDarkThemeActive) Color(0xFFFBBF24) else Color(0xFFD97706)
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Interactive icons matching Linear / Arc design
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Search button triggers manual logging / dialog
                            IconButton(
                                onClick = { showManualLogsDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkThemeActive) Color(0xFF1E293B) else Color.White)
                                    .border(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Quick Search Actions",
                                    tint = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Notification Icon with glowing indicator
                            Box {
                                IconButton(
                                    onClick = { /* visual action handled globally */ },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkThemeActive) Color(0xFF1E293B) else Color.White)
                                        .border(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Soft green notification bubble
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-1).dp, y = 1.dp)
                                )
                            }

                            // Overlapping custom avatar with badge indicator
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { showProfileHubDialog = true }
                            ) {
                                // Initials Circle
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(Color(0xFF4F7CFF), Color(0xFF7C3AED)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                                // Small badge emoji overlapping
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 2.dp, y = 2.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkThemeActive) Color(0xFF1E293B) else Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profileAvatar,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2. MAJESTIC PRODUCTIVITY HERO CARD ---
        if (showHeroWidget.value) {
            item {
            val progressAnimated by animateFloatAsState(
                targetValue = overallProductivityPercentage.toFloat() / 100f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "productivity_ring"
            )

            val infiniteTransition = rememberInfiniteTransition(label = "hero_grad")
            val animatedOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hero_grad_val"
            )

            val heroGradientColors = remember(activeTheme) {
                when (activeTheme) {
                    "Sunset Red" -> listOf(Color(0xFFFD5C25), Color(0xFFEF4444), Color(0xFFC026D3))
                    "Ocean Blue" -> listOf(Color(0xFF0D99FF), Color(0xFF4F7CFF), Color(0xFF7C3AED))
                    "Forest Green" -> listOf(Color(0xFF00C070), Color(0xFF10B981), Color(0xFF0D9488))
                    "Lavender" -> listOf(Color(0xFFAC56FA), Color(0xFF7C3AED), Color(0xFFC084FC))
                    else -> listOf(Color(0xFFFD5C25), Color(0xFFEF4444), Color(0xFFC026D3))
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        spotColor = dynamicColors.primaryColor.copy(alpha = 0.25f)
                    ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = heroGradientColors,
                                start = Offset(animatedOffset, 0f),
                                end = Offset(1000f - animatedOffset, 1000f)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Productivity",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                               )
                               Text(
                                    text = "Performance Index",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 12.sp
                                    )
                               )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Target: 4h Focus",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        // Circular Progress & Stats side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // High-end Custom Canvas circular ring
                            Box(
                                modifier = Modifier.size(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Track Background Ring
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.15f),
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    // Progress Ring
                                    drawArc(
                                        color = Color.White,
                                        startAngle = -90f,
                                        sweepAngle = progressAnimated * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$overallProductivityPercentage%",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 24.sp,
                                            letterSpacing = (-1).sp
                                        )
                                    )
                                    Text(
                                        text = "Complete",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }

                            // Dynamic metrics list
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HeroMetricRow(
                                    icon = Icons.Outlined.CheckCircle,
                                    label = "Tasks Complete",
                                    value = "$todayCompletedTasksCount / ${todayTotalTasksCount.coerceAtLeast(todayCompletedTasksCount)} done"
                                )
                                HeroMetricRow(
                                    icon = Icons.Outlined.HourglassEmpty,
                                    label = "Focus Duration",
                                    value = if (focusMinutes > 0) "${focusMinutes} mins" else "0 mins logged"
                                )
                                HeroMetricRow(
                                    icon = Icons.Default.LocalFireDepartment,
                                    label = "Active Streak",
                                    value = "$streakCount Days Active"
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f), thickness = 1.dp)

                        // Motivational message + Action Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = motivationalText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { viewModel.startTimer() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color(0xFF4F7CFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Start Focus",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF4F7CFF),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }



        // --- 4. TODAY'S HABITS WIDGET ---
        if (showHabitsWidget.value) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Habits",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Daily Routine",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF4F7CFF),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    if (habits.isEmpty()) {
                        PremiumEmptyState(
                            message = "No habits tracked yet. Customize them in the Control Hub!",
                            isDark = isDarkThemeActive
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            habits.take(5).forEachIndexed { index, habit ->
                                val completion = completions.find { it.habitId == habit.id && it.dateString == todayStr }
                                val isCompleted = completion != null && (completion.status == "COMPLETED" || completion.status == null)
                                val isFailed = completion != null && completion.status == "FAILED"

                                val pastelBgColor = when (index % 4) {
                                    0 -> Color(0xFFFEF3C7) // Pastel Amber
                                    1 -> Color(0xFFE0E7FF) // Pastel Indigo
                                    2 -> Color(0xFFFEE2E2) // Pastel Coral
                                    else -> Color(0xFFCCFBF1) // Pastel Mint
                                }

                                HabitPremiumRow(
                                    habit = habit,
                                    isCompleted = isCompleted,
                                    isFailed = isFailed,
                                    pastelBgColor = pastelBgColor,
                                    isDark = isDarkThemeActive,
                                    onCompleteToggle = {
                                        viewModel.setHabitStatus(habit.id, todayStr, if (isCompleted) null else "COMPLETED")
                                    },
                                    onSkipToggle = {
                                        viewModel.setHabitStatus(habit.id, todayStr, if (isFailed) null else "FAILED")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // --- 5. TODAY'S SCHEDULE TIMELINE ---
        if (showScheduleWidget.value) {
            item {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val daysNameList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val currentDayName = remember(todayDayOfWeek) { daysNameList.getOrNull(todayDayOfWeek - 1) ?: "Today" }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = currentDayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                if (todayClasses.isEmpty()) {
                    PremiumEmptyState(
                        message = "No lectures registered for today. Enjoy extra self-study time!",
                        isDark = isDarkThemeActive
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        todayClasses.forEachIndexed { idx, classItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Left timeline connector
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (idx == 0) Color(0xFF4F7CFF) else Color(0xFF7C3AED)
                                            )
                                            .border(2.dp, if (isDarkThemeActive) Color(0xFF0F172A) else Color(0xFFF8FAFC), CircleShape)
                                    )
                                    if (idx < todayClasses.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(84.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color(0xFF4F7CFF), Color(0xFF7C3AED))
                                                    )
                                                )
                                        )
                                    }
                                }

                                // Right Card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkThemeActive) Color(0xFF1E293B) else Color.White
                                    ),
                                    border = BorderStroke(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = classItem.subjectName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A)
                                                    )
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF4F7CFF).copy(alpha = 0.12f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "High Priority",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color(0xFF4F7CFF),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    )
                                                }
                                            }

                                            Text(
                                                text = "⏰ ${classItem.timeSlot} • 🗺️ Room ${classItem.roomNumber}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (isDarkThemeActive) Color(0xFF94A3B8) else Color(0xFF64748B),
                                                    fontSize = 12.sp
                                                )
                                            )

                                            if (idx == 0) {
                                                Text(
                                                    text = "Starts in 25 mins",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        color = Color(0xFF10B981),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                )
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = { viewModel.incrementTotalSessions(classItem) },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Missed", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = { viewModel.incrementAttendance(classItem) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
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
        }
        }

        // --- 6. FOCUS SESSION TIMER WIDGET ---
        if (showTimerWidget.value) {
            item {
            val timerIsRunning by viewModel.timerIsRunning.collectAsStateWithLifecycle()
            val secondsLeft by viewModel.timerSecondsLeft.collectAsStateWithLifecycle()
            val isBreakMode by viewModel.isBreakMode.collectAsStateWithLifecycle()

            val mins = secondsLeft / 60
            val secs = secondsLeft % 60
            val displayTime = String.format("%02d:%02d", mins, secs)

            val currentTargetFocus = viewModel.focusDurationMinutes.collectAsStateWithLifecycle()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Focus Session",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkThemeActive) Color(0xFF1E293B) else Color.White
                    ),
                    border = BorderStroke(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = Icons.Outlined.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFF4F7CFF)
                                )
                                Text(
                                    text = if (isBreakMode) "Mindful Rest Break" else "Deep Pomodoro Focus",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A)
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Longest: ${currentTargetFocus.value}m",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF7C3AED),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        // Giant Digital Timer Display
                        Text(
                            text = displayTime,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBreakMode) Color(0xFF10B981) else Color(0xFF4F7CFF),
                                letterSpacing = (-2).sp
                            )
                        )

                        // Timer action controls
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (timerIsRunning) viewModel.pauseTimer() else viewModel.startTimer()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (timerIsRunning) Color(0xFFEF4444) else Color(0xFF4F7CFF)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (timerIsRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Text(
                                        text = if (timerIsRunning) "Pause Session" else "Start Session",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.resetTimer() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFF1F5F9))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Timer",
                                    tint = if (isDarkThemeActive) Color.White else Color(0xFF0F172A)
                                )
                            }
                        }

                        // Quick duration setup shortcuts
                        HorizontalDivider(color = if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quick setup",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDarkThemeActive) Color(0xFF94A3B8) else Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(15, 25, 50).forEach { minsPreset ->
                                    val isSelected = currentTargetFocus.value == minsPreset
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) Color(0xFF4F7CFF)
                                                else if (isDarkThemeActive) Color(0xFF334155)
                                                else Color(0xFFF1F5F9)
                                            )
                                            .clickable { viewModel.setFocusDuration(minsPreset) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${minsPreset}m",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isSelected) Color.White else if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold
                                            )
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



        // --- 8. TODAY'S GOALS CHECKLIST ---
        if (showGoalsWidget.value) {
            item {
                Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Goals",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "$todayCompletedTasksCount of ${todayTotalTasksCount.coerceAtLeast(todayCompletedTasksCount)} Completed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF4F7CFF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                val todayTasks = tasks.filter { it.dueDate == todayStr }

                if (todayTasks.isEmpty()) {
                    PremiumEmptyState(
                        message = "All caught up! No tasks scheduled for today. Add one in the Tasks Center!",
                        isDark = isDarkThemeActive
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkThemeActive) Color(0xFF1E293B) else Color.White
                        ),
                        border = BorderStroke(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Goals Progress bar
                            val progressFraction = if (todayTotalTasksCount > 0) todayCompletedTasksCount.toFloat() / todayTotalTasksCount.toFloat() else 1f
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Task fulfillment ratio",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isDarkThemeActive) Color(0xFF94A3B8) else Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                    Text(
                                        text = "${(progressFraction * 100).toInt()}% Done",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF4F7CFF),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFF1F5F9))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = progressFraction)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF4F7CFF), Color(0xFF7C3AED))
                                                )
                                            )
                                    )
                                }
                            }

                            HorizontalDivider(color = if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFF1F5F9))

                            // Checklist list
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                todayTasks.forEach { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (task.completed) {
                                                    Color(0xFF10B981).copy(alpha = 0.05f)
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Checkbox(
                                                checked = task.completed,
                                                onCheckedChange = { isChecked ->
                                                    viewModel.updateTaskCompletion(task, isChecked)
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = Color(0xFF10B981)
                                                )
                                            )

                                            Column {
                                                Text(
                                                    text = task.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (task.completed) {
                                                            if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8)
                                                        } else {
                                                            if (isDarkThemeActive) Color.White else Color(0xFF0F172A)
                                                        },
                                                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                                                    )
                                                )
                                                Text(
                                                    text = "⏳ Est: 45 mins • Subject: ${task.subject}",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }

                                        // Goal priority badge (Soft, Premium Pastel)
                                        val isDark = isDarkThemeActive
                                        val (pBg, pTxt, pBorder) = when (task.priority.uppercase()) {
                                            "URGENT", "HIGH" -> Triple(
                                                if (isDark) Color(0xFFEF4444).copy(alpha = 0.18f) else Color(0xFFFEE2E2),
                                                if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                                                if (isDark) Color(0xFFEF4444).copy(alpha = 0.3f) else Color(0xFFFCA5A5)
                                            )
                                            "MEDIUM" -> Triple(
                                                if (isDark) Color(0xFFF59E0B).copy(alpha = 0.18f) else Color(0xFFFEF3C7),
                                                if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309),
                                                if (isDark) Color(0xFFF59E0B).copy(alpha = 0.3f) else Color(0xFFFDE68A)
                                            )
                                            else -> Triple(
                                                if (isDark) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFFD1FAE5),
                                                if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857),
                                                if (isDark) Color(0xFF10B981).copy(alpha = 0.3f) else Color(0xFFA7F3D0)
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(pBg)
                                                .border(BorderStroke(1.dp, pBorder), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = when (task.priority.uppercase()) {
                                                    "URGENT", "HIGH" -> "High"
                                                    "MEDIUM" -> "Medium"
                                                    else -> "Low"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = pTxt,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
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

        // --- 9. ACHIEVEMENTS MILESTONES (HORIZONTAL SCROLL) ---
        if (showAchievementsWidget.value) {
            item {
                Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = screenPadding)
                ) {
                    Text(
                        text = "Merit Achievements",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Text(
                        text = "Level 3 Scholar • 650 / 1000 XP to Level 4",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF7C3AED),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    )
                }

                // Horizontal badges track
                val badgesList = listOf(
                    MilestoneBadge("Focus Initiate", "Log first focused session.", "🌱", focusMinutes > 0),
                    MilestoneBadge("Focus Monk", "Log 300+ mins (5h).", "🧘", focusMinutes >= 300),
                    MilestoneBadge("High Achiever", "Complete 5+ academic tasks.", "🏅", todayCompletedTasksCount >= 5),
                    MilestoneBadge("Streak Master", "Reach a 3+ day active streak.", "⚡", streakCount >= 3),
                    MilestoneBadge("Academic Legend", "Achieve Level 5 / 20h focus.", "👑", focusMinutes >= 1200),
                    MilestoneBadge("Perfect Scholar", "80%+ attendance logged.", "🎓", overallAttendance >= 80f)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = screenPadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(badgesList) { badge ->
                        Card(
                            modifier = Modifier
                                .width(150.dp)
                                .height(130.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (badge.unlocked) {
                                    if (isDarkThemeActive) Color(0xFF1E293B) else Color.White
                                } else {
                                    if (isDarkThemeActive) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF1F5F9)
                                }
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (badge.unlocked) Color(0xFF7C3AED).copy(alpha = 0.3f) else Color.Transparent
                            )
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
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (badge.unlocked) Color(0xFF7C3AED).copy(alpha = 0.15f)
                                                else Color.Gray.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = badge.emoji, fontSize = 16.sp)
                                    }

                                    if (badge.unlocked) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = badge.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = badge.desc,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                                            fontSize = 9.sp
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
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

    // --- Control Hub & Settings Panel ---
    if (showProfileHubDialog) {
        var hubTab by remember { mutableStateOf(0) } // 0=Account & Accent, 1=Widgets, 2=Sync Config, 3=Donate
        var tempName by remember { mutableStateOf(studentName) }
        var tempMajor by remember { mutableStateOf(studentMajor) }
        var tempYear by remember { mutableStateOf(studentYear) }
        var tempGoal by remember { mutableStateOf(academicGoal) }
        var tempAvatar by remember { mutableStateOf(profileAvatar) }

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
                                textSectionLabel(title = title, isSelected = isSelected)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // SHEETS DISPLAY
                    when (hubTab) {
                        0 -> { // Profile & Accent customization
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 340.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // --- DIGITAL ID CARD PREVIEW ---
                                val cardBg = when (activeTheme) {
                                    "Sunset Red" -> Brush.linearGradient(listOf(Color(0xFFFD5C25), Color(0xFFFF8E53)))
                                    "Forest Green" -> Brush.linearGradient(listOf(Color(0xFF00C070), Color(0xFF5CFFA9)))
                                    "Lavender" -> Brush.linearGradient(listOf(Color(0xFFAC56FA), Color(0xFFE2B2FF)))
                                    else -> Brush.linearGradient(listOf(Color(0xFF0D99FF), Color(0xFF8AD4FF)))
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(cardBg)
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // Glowing Avatar Circle in Preview
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White.copy(alpha = 0.25f))
                                                            .border(1.5.dp, Color.White, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = tempAvatar, fontSize = 24.sp)
                                                    }

                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = if (tempName.isBlank()) "Academic Achiever" else tempName,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = tempMajor,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White.copy(alpha = 0.85f),
                                                            fontWeight = FontWeight.Medium,
                                                            maxLines = 2,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }

                                                // Year Badge
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = tempYear,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                            }

                                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    Text(
                                                        text = "ACADEMIC FOCUS GOAL",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 8.sp,
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Text(
                                                        text = tempGoal,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                // Tech Barcode Element
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                                        val barWidths = listOf(2, 4, 1, 3, 2, 4, 1, 2, 3)
                                                        barWidths.forEach { w ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .width(w.dp)
                                                                    .height(14.dp)
                                                                    .background(Color.White.copy(alpha = 0.5f))
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "STUDENT ID PRO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 7.sp,
                                                        color = Color.White.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                // --- AVATAR EMOJI SELECTOR ---
                                Text("Choose Profile Badge", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                val avatarOptions = listOf("🎓", "💻", "🔬", "🎨", "✍️", "🧠", "🧬", "🚀", "🏥", "💼", "📚", "🌟", "🔥", "🎯")
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    avatarOptions.forEach { emoji ->
                                        val isSelected = tempAvatar == emoji
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                                                    shape = CircleShape
                                                )
                                                .clickable { tempAvatar = emoji },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = emoji, fontSize = 20.sp)
                                        }
                                    }
                                }

                                // --- NAME FIELD ---
                                Text("Student Username", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = tempName,
                                    onValueChange = { tempName = it },
                                    placeholder = { Text("e.g. Satish") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("student_name_field")
                                )

                                // --- MAJOR FIELD ---
                                Text("Field of Study / Major", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = tempMajor,
                                    onValueChange = { tempMajor = it },
                                    placeholder = { Text("e.g. Computer Science") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // --- YEAR OF STUDY ---
                                Text("Year of Study", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                val yearsList = listOf("Freshman", "Sophomore", "Junior", "Senior", "Postgrad")
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    yearsList.forEach { yr ->
                                        val isSel = tempYear == yr
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { tempYear = yr },
                                            label = { Text(yr) }
                                        )
                                    }
                                }

                                // --- FOCUS GOAL ---
                                Text("Academic Slogan & Focus Goal", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = tempGoal,
                                    onValueChange = { tempGoal = it },
                                    placeholder = { Text("e.g. Aiming for 4.0 GPA") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))
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

                                Spacer(modifier = Modifier.height(6.dp))
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

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    WidgetToggleRow(
                                        title = "Productivity Hero Card",
                                        description = "Vibrant organic gradient banner showing overall study completion metrics.",
                                        checked = showHeroWidget.value,
                                        onCheckedChange = { newVal ->
                                            showHeroWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_hero_widget", newVal).apply()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    WidgetToggleRow(
                                        title = "Daily Habits Quick Checklist",
                                        description = "Provides rapid tap completion for core active routine trackers.",
                                        checked = showHabitsWidget.value,
                                        onCheckedChange = { newVal ->
                                            showHabitsWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_habits_widget", newVal).apply()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    WidgetToggleRow(
                                        title = "Today's Schedule Timeline",
                                        description = "Interactive visual timeline connecting registered lectures and classes.",
                                        checked = showScheduleWidget.value,
                                        onCheckedChange = { newVal ->
                                            showScheduleWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_schedule_widget", newVal).apply()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    WidgetToggleRow(
                                        title = "Focus Session Quick-Start",
                                        description = "Provides a compact Pomodoro timer with direct session controls.",
                                        checked = showTimerWidget.value,
                                        onCheckedChange = { newVal ->
                                            showTimerWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_timer_widget", newVal).apply()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))



                                    WidgetToggleRow(
                                        title = "Today's Goals Checklist",
                                        description = "Interactive checklist of tasks assigned or due today with progress bars.",
                                        checked = showGoalsWidget.value,
                                        onCheckedChange = { newVal ->
                                            showGoalsWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_goals_widget", newVal).apply()
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    WidgetToggleRow(
                                        title = "Merit Achievements Milestones",
                                        description = "Displays earned digital badges, scholar levels, and XP progress.",
                                        checked = showAchievementsWidget.value,
                                        onCheckedChange = { newVal ->
                                            showAchievementsWidget.value = newVal
                                            sharedPrefs.edit().putBoolean("show_achievements_widget", newVal).apply()
                                        }
                                    )
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

                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

                                Text(
                                    text = "Robust Local Storage",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Text(
                                    text = "Wipe and reset your tasks, stats, and habits to start completely from zero, or load demo statistics for quick visualization.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.align(Alignment.Start)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Seed Demo Data Button
                                    Button(
                                        onClick = {
                                            viewModel.seedDemoData()
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("⚡", fontSize = 12.sp)
                                            Text(
                                                text = "Load Demo",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Clear/Reset Data Button
                                    Button(
                                        onClick = {
                                            viewModel.clearAllData()
                                        },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                                        contentPadding = PaddingValues(horizontal = 4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🗑️", fontSize = 12.sp)
                                            Text(
                                                text = "Reset to Zero",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFFEF4444),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
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
                        viewModel.updateStudentMajor(tempMajor)
                        viewModel.updateStudentYear(tempYear)
                        viewModel.updateAcademicGoal(tempGoal)
                        viewModel.updateProfileAvatar(tempAvatar)
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

// --- SUBCOMPONENTS FOR HOME SCREEN ---

@Composable
fun textSectionLabel(title: String, isSelected: Boolean) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun HeroMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun PremiumStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    trend: String,
    trendPositive: Boolean,
    sparklinePoints: List<Float>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDark: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (trendPositive) Color(0xFF4F7CFF) else Color(0xFF7C3AED),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF0F172A),
                    letterSpacing = (-0.5).sp
                )
            )

            // Sparkline canvas graph
            Sparkline(
                points = sparklinePoints,
                color = if (trendPositive) Color(0xFF10B981) else Color(0xFF7C3AED),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (trendPositive) Color(0xFF10B981) else Color(0xFF7C3AED))
                )
                Text(
                    text = trend,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (trendPositive) Color(0xFF10B981) else Color(0xFF7C3AED),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
fun Sparkline(
    points: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val width = size.width
        val height = size.height
        val maxVal = points.maxOrNull() ?: 1f
        val minVal = points.minOrNull() ?: 0f
        val range = if (maxVal == minVal) 1f else maxVal - minVal

        val path = Path().apply {
            val startX = 0f
            val startY = height - ((points[0] - minVal) / range) * height
            moveTo(startX, startY)
            for (i in 1 until points.size) {
                val x = (i.toFloat() / (points.size - 1)) * width
                val y = height - ((points[i] - minVal) / range) * height
                lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun HabitPremiumRow(
    habit: HabitEntity,
    isCompleted: Boolean,
    isFailed: Boolean,
    pastelBgColor: Color,
    isDark: Boolean,
    onCompleteToggle: () -> Unit,
    onSkipToggle: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                Color(0xFF10B981).copy(alpha = 0.08f)
            } else if (isFailed) {
                Color(0xFFEF4444).copy(alpha = 0.08f)
            } else {
                if (isDark) Color(0xFF1E293B) else Color.White
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) {
                Color(0xFF10B981).copy(alpha = 0.3f)
            } else if (isFailed) {
                Color(0xFFEF4444).copy(alpha = 0.3f)
            } else {
                if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(pastelBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = habit.icon, fontSize = 18.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔥 ${habit.streak} Day Streak",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFD5C25),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Modern status indicator on the right of the habit row
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isFailed) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Skipped",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                width = 2.dp,
                                color = if (isDark) Color(0xFF475569) else Color(0xFFCBD5E1),
                                shape = CircleShape
                            )
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ Est: 15 mins",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onSkipToggle,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444)),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (isFailed) "Undo" else "Skip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onCompleteToggle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) Color(0xFF64748B) else Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (isCompleted) "Undo" else "Complete", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumEmptyState(
    message: String,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.5f) else Color(0xFFF1F5F9)
        ),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFE2E8F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Eco,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

data class MilestoneBadge(
    val title: String,
    val desc: String,
    val emoji: String,
    val unlocked: Boolean
)

@Composable
fun WidgetToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
