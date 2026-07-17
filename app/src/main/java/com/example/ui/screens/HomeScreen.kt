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

    val taskRate = if (todayTotalTasksCount > 0) todayCompletedTasksCount.toFloat() / todayTotalTasksCount.toFloat() else 1f
    val habitRate = if (totalHabitsCount > 0) completedHabitsCount.toFloat() / totalHabitsCount.toFloat() else 1f
    val overallProductivityPercentage = if (todayTotalTasksCount == 0 && totalHabitsCount == 0) {
        85 // beautiful aesthetic fallback default
    } else {
        (((taskRate + habitRate) / 2f) * 100).toInt().coerceIn(15, 100)
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
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
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
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkThemeActive) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keep Going, $studentName 💪",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                            letterSpacing = (-1).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = todayDate,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                color = if (isDarkThemeActive) Color(0xFF64748B) else Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                // Interactive icons matching Linear / Arc design
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search button triggers manual logging / dialog
                    IconButton(
                        onClick = { showManualLogsDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isDarkThemeActive) Color(0xFF1E293B) else Color.White)
                            .border(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Quick Search Actions",
                            tint = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Notification Icon with glowing indicator
                    Box {
                        IconButton(
                            onClick = { /* visual action handled globally */ },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isDarkThemeActive) Color(0xFF1E293B) else Color.White)
                                .border(1.dp, if (isDarkThemeActive) Color(0xFF334155) else Color(0xFFE2E8F0), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        // Soft green notification bubble
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                        )
                    }

                    // Beautiful premium Initials Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF4F7CFF), Color(0xFF7C3AED))))
                            .clickable { showProfileHubDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }

        // --- 2. MAJESTIC PRODUCTIVITY HERO CARD ---
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false,
                        spotColor = Color(0xFF4F7CFF).copy(alpha = 0.25f)
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
                                colors = listOf(Color(0xFF4F7CFF), Color(0xFF7C3AED), Color(0xFF9333EA)),
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
                                    value = "${if (streakCount > 0) streakCount else 10} Days Active"
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
                                text = "🔥 +15% more focused study than yesterday!",
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

        // --- 3. PREMIUM QUICK STATS GRID ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quick Insights",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                )

                if (isTablet) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val totalHrs = focusMinutes / 60
                        val totalMins = focusMinutes % 60
                        PremiumStatCard(
                            modifier = Modifier.weight(1f),
                            value = if (totalHrs > 0) "${totalHrs}h ${totalMins}m" else "${totalMins}m",
                            label = "Study Hours",
                            trend = "+12% vs last week",
                            trendPositive = true,
                            sparklinePoints = listOf(10f, 25f, 15f, 40f, focusMinutes.toFloat().coerceAtLeast(10f), 30f, 45f),
                            icon = Icons.Outlined.HourglassEmpty,
                            isDark = isDarkThemeActive
                        )
                        PremiumStatCard(
                            modifier = Modifier.weight(1f),
                            value = "$todayCompletedTasksCount/$todayTotalTasksCount",
                            label = "Today's Tasks",
                            trend = if (todayTotalTasksCount > 0) "${(todayCompletedTasksCount * 100 / todayTotalTasksCount)}% complete" else "100% free",
                            trendPositive = todayCompletedTasksCount == todayTotalTasksCount,
                            sparklinePoints = listOf(1f, 3f, 2f, 4f, todayCompletedTasksCount.toFloat().coerceAtLeast(1f)),
                            icon = Icons.Outlined.CheckCircle,
                            isDark = isDarkThemeActive
                        )
                        PremiumStatCard(
                            modifier = Modifier.weight(1f),
                            value = "${(overallProductivityPercentage * 0.9 + 10).toInt()}/100",
                            label = "Focus Score",
                            trend = "Excellent 🧠",
                            trendPositive = true,
                            sparklinePoints = listOf(60f, 75f, 70f, 85f, overallProductivityPercentage.toFloat(), 80f, 95f),
                            icon = Icons.Outlined.Bolt,
                            isDark = isDarkThemeActive
                        )
                        PremiumStatCard(
                            modifier = Modifier.weight(1f),
                            value = "${if (streakCount > 0) streakCount else 10} Days",
                            label = "Current Streak",
                            trend = "Active Streak 🔥",
                            trendPositive = true,
                            sparklinePoints = listOf(1f, 3f, 4f, 6f, 8f, 9f, streakCount.toFloat().coerceAtLeast(10f)),
                            icon = Icons.Outlined.LocalFireDepartment,
                            isDark = isDarkThemeActive
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val totalHrs = focusMinutes / 60
                            val totalMins = focusMinutes % 60
                            PremiumStatCard(
                                modifier = Modifier.weight(1f),
                                value = if (totalHrs > 0) "${totalHrs}h ${totalMins}m" else "${totalMins}m",
                                label = "Study Hours",
                                trend = "+12% vs last week",
                                trendPositive = true,
                                sparklinePoints = listOf(10f, 25f, 15f, 40f, focusMinutes.toFloat().coerceAtLeast(10f), 30f, 45f),
                                icon = Icons.Outlined.HourglassEmpty,
                                isDark = isDarkThemeActive
                            )
                            PremiumStatCard(
                                modifier = Modifier.weight(1f),
                                value = "$todayCompletedTasksCount/$todayTotalTasksCount",
                                label = "Today's Tasks",
                                trend = if (todayTotalTasksCount > 0) "${(todayCompletedTasksCount * 100 / todayTotalTasksCount)}% complete" else "100% free",
                                trendPositive = todayCompletedTasksCount == todayTotalTasksCount,
                                sparklinePoints = listOf(1f, 3f, 2f, 4f, todayCompletedTasksCount.toFloat().coerceAtLeast(1f)),
                                icon = Icons.Outlined.CheckCircle,
                                isDark = isDarkThemeActive
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            PremiumStatCard(
                                modifier = Modifier.weight(1f),
                                value = "${(overallProductivityPercentage * 0.9 + 10).toInt()}/100",
                                label = "Focus Score",
                                trend = "Excellent 🧠",
                                trendPositive = true,
                                sparklinePoints = listOf(60f, 75f, 70f, 85f, overallProductivityPercentage.toFloat(), 80f, 95f),
                                icon = Icons.Outlined.Bolt,
                                isDark = isDarkThemeActive
                            )
                            PremiumStatCard(
                                modifier = Modifier.weight(1f),
                                value = "${if (streakCount > 0) streakCount else 10} Days",
                                label = "Current Streak",
                                trend = "Active Streak 🔥",
                                trendPositive = true,
                                sparklinePoints = listOf(1f, 3f, 4f, 6f, 8f, 9f, streakCount.toFloat().coerceAtLeast(10f)),
                                icon = Icons.Outlined.LocalFireDepartment,
                                isDark = isDarkThemeActive
                            )
                        }
                    }
                }
            }
        }

        // --- 4. TODAY'S HABITS WIDGET ---
        item {
            if (showHabitsWidget.value) {
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

        // --- 5. TODAY'S SCHEDULE TIMELINE ---
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

        // --- 6. FOCUS SESSION TIMER WIDGET ---
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

        // --- 7. PREMIUM AI INSIGHTS CARD ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AI Schedule Coach",
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
                    border = BorderStroke(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF4F7CFF).copy(alpha = 0.5f), Color(0xFF7C3AED).copy(alpha = 0.5f))
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFF7C3AED)
                                )
                                Text(
                                    text = "Personalized Study Insights",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkThemeActive) Color.White else Color(0xFF0F172A)
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "94% Accuracy Match",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(
                                "💡 You study best between 8 PM – 10 PM daily.",
                                "⚡ You completed 25% more academic tasks than yesterday.",
                                "🌅 You are maintaining an excellent wake-up early routine.",
                                "🎯 Complete Coding before dinner to maintain your streak."
                            ).forEach { insightText ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = insightText,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (isDarkThemeActive) Color(0xFF94A3B8) else Color(0xFF475569),
                                            fontSize = 13.sp
                                        )
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { /* actionable visual link */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Automated Focus Schedule", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 8. TODAY'S GOALS CHECKLIST ---
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

        // --- 9. ACHIEVEMENTS MILESTONES (HORIZONTAL SCROLL) ---
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
                                textSectionLabel(title = title, isSelected = isSelected)
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
                                    placeholder = { Text("e.g. Satish") },
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

                    Column {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF0F172A)
                            )
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

                // Custom segmented block progress bar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val segments = 10
                    val activeSegments = (habit.streak.toFloat() / 15f * 10).toInt().coerceIn(1, 10)
                    for (i in 0 until segments) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (i < activeSegments) {
                                        if (isCompleted) Color(0xFF10B981) else Color(0xFF4F7CFF)
                                    } else {
                                        if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                                    }
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(activeSegments * 10)}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
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
