package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StudyDatabase
import com.example.data.StudyRepository
import com.example.ui.StudyViewModel
import com.example.ui.StudyViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

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
            MyApplicationTheme(darkTheme = false, accentName = themeAccent) {
                MainAppLayout(viewModel)
            }
        }
    }
}

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    TASKS("Tasks", Icons.Default.TaskAlt),
    TIMER("Timer", Icons.Default.Timer),
    HABITS("Habits", Icons.Default.CheckCircle),
    SCHEDULE("Schedule", Icons.Default.CalendarToday)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: StudyViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.title) },
                        label = { Text(text = tab.title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        },
        topBar = {
            if (selectedTab != AppTab.HOME) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (selectedTab) {
                                AppTab.HOME -> "StudyFlow"
                                AppTab.TASKS -> "Task Center"
                                AppTab.TIMER -> "Pomodoro Focus"
                                AppTab.HABITS -> "Habits Tracker"
                                AppTab.SCHEDULE -> "Classes Timetable"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
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
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel)
                AppTab.TASKS -> TaskScreen(viewModel = viewModel)
                AppTab.TIMER -> TimerScreen(viewModel = viewModel)
                AppTab.HABITS -> HabitScreen(viewModel = viewModel)
                AppTab.SCHEDULE -> ScheduleScreen(viewModel = viewModel)
            }
        }
    }
}
