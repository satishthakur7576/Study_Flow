package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.StudyDatabase
import com.example.data.StudyRepository
import com.example.ui.StudyViewModel
import com.example.ui.StudyViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Database, Repository and ViewModel
        val database = StudyDatabase.getDatabase(applicationContext)
        val repository = StudyRepository(database.studyDao)
        val viewModel: StudyViewModel by viewModels {
            StudyViewModelFactory(repository, applicationContext)
        }

        enableEdgeToEdge()
        setContent {
            val themeAccent by viewModel.themeAccent.collectAsState()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme, accentName = themeAccent) {
                MainAppLayout(viewModel)
            }
        }
    }
}

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    ANALYTICS("Stats", Icons.Default.BarChart),
    TASKS("Tasks", Icons.Default.TaskAlt),
    TIMER("Timer", Icons.Default.Timer),
    HABITS("Habits", Icons.Default.CheckCircle),
    SCHEDULE("Classes", Icons.Default.CalendarToday)
}

@Composable
fun AdaptiveTabLabel(text: String, color: Color = Color.Unspecified) {
    Text(
        text = text,
        fontSize = 9.sp,
        letterSpacing = (-0.3).sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
        color = color
    )
}

data class ChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: StudyViewModel) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Global Overlay States
    var showCommandPalette by remember { mutableStateOf(false) }
    var showAiAssistant by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }

    // Student & Study data states for Sidebar / Overlays
    val studentName by viewModel.studentName.collectAsState()
    val studentYear by viewModel.studentYear.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val activeTheme by viewModel.themeAccent.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val todayFocusMinutes by viewModel.todayFocusMinutes.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val classes by viewModel.classes.collectAsState()

    // Global AI assistant chat history
    val aiChatHistory = remember {
        mutableStateListOf(
            ChatMessage("FocusBot", "Hello! I am FocusBot, your real-time academic assistant. I see you've completed several tasks and tracked your schedule. How can I optimize your study routine today?", isUser = false)
        )
    }

    // Dynamic gradient brush from theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val dynamicBrush = remember(primaryColor, secondaryColor) {
        Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Sidebar Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(dynamicBrush),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = studentName.take(2).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column {
                                Text(
                                    text = studentName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = studentYear,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Sidebar Navigation Tabs list
                        Text(
                            text = "NAVIGATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        AppTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        else Color.Transparent
                                    )
                                    .clickable {
                                        selectedTab = tab
                                        scope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Bottom of Sidebar
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        // Quick Accent Switcher in Sidebar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val themeAccents = listOf("Ocean", "Lavender", "Forest", "Sunset")
                            themeAccents.forEach { accent ->
                                val isSelected = activeTheme == accent
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            val realAccent = when (accent) {
                                                "Ocean" -> "Ocean Blue"
                                                "Lavender" -> "Lavender"
                                                "Forest" -> "Forest Green"
                                                else -> "Sunset Orange"
                                            }
                                            viewModel.updateThemeAccent(realAccent)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = accent.take(1),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .height(66.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(24.dp), clip = false),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                val activeColor = MaterialTheme.colorScheme.primary
                                val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                
                                val animatedScale by animateFloatAsState(
                                    targetValue = if (isSelected) 1.15f else 1.0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                                    label = "tab_scale"
                                )
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                            selectedTab = tab
                                        }
                                        .padding(vertical = 4.dp)
                                        .testTag("nav_item_${tab.name.lowercase()}"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) activeColor else inactiveColor,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .scale(animatedScale)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) activeColor else inactiveColor,
                                            letterSpacing = (-0.4).sp
                                        ),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Clip
                                    )
                                }
                            }
                        }
                    }
                }
            },
            topBar = {
                if (selectedTab != AppTab.HOME) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = when (selectedTab) {
                                    AppTab.HOME -> "StudyFlow Pro"
                                    AppTab.ANALYTICS -> "Analytics & Progress"
                                    AppTab.TASKS -> "Task Center"
                                    AppTab.TIMER -> "Pomodoro Focus"
                                    AppTab.HABITS -> "Habits Tracker"
                                    AppTab.SCHEDULE -> "Classes Timetable"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Sidebar Menu",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            // Search: Command Palette trigger
                            IconButton(onClick = { showCommandPalette = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Command Palette",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Bell: Notifications trigger
                            IconButton(onClick = { showNotifications = true }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "System Notifications",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                color = MaterialTheme.colorScheme.background
            ) {
                when (selectedTab) {
                    AppTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTimer = { selectedTab = AppTab.TIMER }
                    )
                    AppTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                    AppTab.TASKS -> TaskScreen(viewModel = viewModel)
                    AppTab.TIMER -> TimerScreen(viewModel = viewModel)
                    AppTab.HABITS -> HabitScreen(viewModel = viewModel)
                    AppTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
                }
            }
        }
    }

    // --- GLOBAL OVERLAY 1: SEARCH / COMMAND PALETTE (⌘K) ---
    if (showCommandPalette) {
        var query by remember { mutableStateOf("") }
        val filteredTasks = remember(query, tasks) {
            if (query.isBlank()) emptyList()
            else tasks.filter { it.title.contains(query, ignoreCase = true) }
        }

        Dialog(onDismissRequest = { showCommandPalette = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search actions or tasks...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⌘K", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Suggestions / Results
                    Text(
                        text = if (query.isBlank()) "QUICK ACTIONS" else "MATCHING TASKS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )

                    if (query.isBlank()) {
                        val actions = listOf(
                            Pair("Start Pomodoro Timer ⏱️") { selectedTab = AppTab.TIMER; showCommandPalette = false },
                            Pair("Switch Theme Accent 🎨") { scope.launch { drawerState.open() }; showCommandPalette = false },
                            Pair("Toggle Dark / Light Mode 🌗") { viewModel.updateDarkTheme(!isDarkTheme); showCommandPalette = false }
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(actions) { action ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { action.second() }
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = action.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    } else {
                        if (filteredTasks.isEmpty()) {
                            Text(
                                text = "No tasks match \"$query\"",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(filteredTasks) { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                selectedTab = AppTab.TASKS
                                                showCommandPalette = false
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                            Text(text = "Subject: ${task.subject} • Due: ${task.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Close Button
                    TextButton(
                        onClick = { showCommandPalette = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }



    // --- GLOBAL OVERLAY 3: SYSTEM NOTIFICATION CENTER ---
    if (showNotifications) {
        val systemNotifications = listOf(
            Pair("🎯 Streak Achieved!", "Perfect 4-day habit checklist streak maintained. High consistency is paying off!"),
            Pair("📅 Upcoming Mid-Term", "You have homework assignments due in the next 48 hours. Double check due dates in Task Center."),
            Pair("💡 FocusBot Architecture Advice", "Your average focus session duration is 25 minutes. Optimize rest breaks using the Pomodoro module.")
        )

        Dialog(onDismissRequest = { showNotifications = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Notifications Center", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showNotifications = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close notifications")
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(systemNotifications) { notification ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = notification.first, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = notification.second, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = { showNotifications = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }
    }
}

private fun getSimulatedResponse(userText: String): String {
    return when {
        userText.contains("streak", ignoreCase = true) -> {
            "Exceptional work! You've maintained a perfect active streak. Rest is as vital as focus—schedule a 10-minute mindful break in the Pomodoro tab tonight."
        }
        userText.contains("procrastination", ignoreCase = true) || userText.contains("recovery", ignoreCase = true) -> {
            "To break inertia, use the 5-minute Pomodoro rule in the Timer screen. Commit to working for just 5 minutes. Often, building starting momentum is 90% of the battle!"
        }
        userText.contains("schedule", ignoreCase = true) || userText.contains("class", ignoreCase = true) -> {
            "You have regular lectures mapped out. Keeping consistent logs will push your overall lecture attendance over the target 90% milestone!"
        }
        else -> {
            "Fascinating inquiry. I suggest prioritizing tasks tagged 'Urgent' on your Kanban Board first, then checking off your daily habits before 8 PM to preserve high consistency!"
        }
    }
}
