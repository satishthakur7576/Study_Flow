package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.StudyViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun HabitScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val completions by viewModel.completions.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    // Get the formatted date keys for Mon-Sun of the current week (YYYY-MM-DD keys)
    val weekDates = remember { viewModel.getDatesOfCurrentWeekString() }
    val todayString = remember { viewModel.getTodayDateString() }
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Scaffold(
        modifier = modifier.testTag("habit_screen"),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FintrixOrangeGradient)
                    .clickable { showAddDialog = true }
                    .testTag("add_habit_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Habit", tint = Color.White)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOP DESCRIPTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Habit Trackers 🌟",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Complete habits to fuel your mental & focus streaks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // --- EMPTY STATE IF NO HABITS ---
            if (habits.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No custom habits active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Create custom daily trackers (like 'Study 2h', 'Exercise', or 'Attend classes') to hold yourself consistent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(260.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(habits, key = { it.id }) { habit ->
                        HabitCard(
                            habit = habit,
                            weekDates = weekDates,
                            dayLabels = dayLabels,
                            todayString = todayString,
                            completions = completions,
                            onToggleDate = { dateStr, status ->
                                viewModel.setHabitStatus(habit.id, dateStr, status)
                            },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                }
            }
        }
    }

    // --- ADD HABIT DIALOG ---
    if (showAddDialog) {
        var nameInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }
        var selectedIcon by remember { mutableStateOf("🌱") }
        var selectedColorHex by remember { mutableStateOf("#FD5C25") }
        var selectedCategory by remember { mutableStateOf("Learning") }
        var selectedFrequency by remember { mutableStateOf("Daily") }
        var nameError by remember { mutableStateOf(false) }

        val iconsList = listOf("🌱", "💻", "🏃‍♂️", "📚", "🧘‍♂️", "💧", "🍳", "🧠", "💡", "🎨", "🚴", "🔥")
        val colorsList = listOf(
            "#FD5C25" to "Sunset Red",
            "#3B82F6" to "Ocean Blue",
            "#10B981" to "Forest Green",
            "#8B5CF6" to "Lavender",
            "#EC4899" to "Soft Pink"
        )
        val categoriesList = listOf("Learning", "Health", "Personal", "Other")
        val frequenciesList = listOf("Daily", "Weekly", "Monthly")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "Add New Habit ✍️",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category Banner/Info
                    Text(
                        text = "Customize your routine with colors, icons, and categories matching your goals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Habit Name",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                nameError = false
                            },
                            placeholder = { Text("e.g. Learn Coding") },
                            isError = nameError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("habit_name_input")
                        )
                        if (nameError) {
                            Text(
                                text = "Habit name cannot be blank.",
                                color = HighPriorityColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // 2. Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Description (Optional)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            placeholder = { Text("e.g. Complete 2 LeetCode questions") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. Choose Icon Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Choose Icon",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            iconsList.forEach { icon ->
                                val isSelected = selectedIcon == icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedIcon = icon },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = icon, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    // 4. Choose Color Palette
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Choose Color",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            colorsList.forEach { (colorHexVal, name) ->
                                val rgbVal = try { Color(android.graphics.Color.parseColor(colorHexVal)) } catch(e: Exception) { Color.Gray }
                                val isSelected = selectedColorHex == colorHexVal
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(rgbVal)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = colorHexVal }
                                )
                            }
                        }
                    }

                    // 5. Category Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categoriesList.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // 6. Frequency Selection
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Frequency",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            frequenciesList.forEach { freq ->
                                val isSelected = selectedFrequency == freq
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFrequency = freq },
                                    label = { Text(freq, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isBlank()) {
                            nameError = true
                        } else {
                            viewModel.addHabit(
                                name = nameInput.trim(),
                                description = descInput.trim(),
                                icon = selectedIcon,
                                colorHex = selectedColorHex,
                                category = selectedCategory,
                                frequency = selectedFrequency
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_save_habit")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    weekDates: List<String>,
    dayLabels: List<String>,
    todayString: String,
    completions: List<HabitCompletionEntity>,
    onToggleDate: (String, String?) -> Unit,
    onDelete: () -> Unit
) {
    // Calculate completions this week
    val habitCompletionsThisWeek = completions.filter {
        it.habitId == habit.id && weekDates.contains(it.dateString) && it.status == "COMPLETED"
    }
    val completionsCount = habitCompletionsThisWeek.size
    val weeklyProgress = if (weekDates.isNotEmpty()) completionsCount.toFloat() / weekDates.size.toFloat() else 0f

    // Dynamically parse habit custom color
    val customColor = remember(habit.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(habit.colorHex))
        } catch (e: Exception) {
            Color(0xFFFD5C25) // Fallback default orange
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(FintrixCardGradient)
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- HEADER: HABIT NAME & STREAK & DELETION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Emoji prefix representing Chosen Icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(customColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = habit.icon.ifBlank { "🌱" }, fontSize = 16.sp)
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = habit.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (habit.streak > 0) {
                                  Text(
                                      text = "🔥 ${habit.streak}d",
                                      style = MaterialTheme.typography.bodySmall,
                                      fontWeight = FontWeight.Bold,
                                      color = HighPriorityColor,
                                      modifier = Modifier
                                          .clip(RoundedCornerShape(4.dp))
                                          .background(HighPriorityColor.copy(alpha = 0.15f))
                                          .padding(horizontal = 6.dp, vertical = 2.dp)
                                  )
                                }
                            }

                            // Metadata details: Category • Frequency
                            Text(
                                text = "${habit.category} • ${habit.frequency}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Optional Description
                    if (habit.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 40.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_habit_button_${habit.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Habit",
                        tint = HighPriorityColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // --- WEEK PROGRESS BAR ---
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "$completionsCount of 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = customColor
                    )
                }
                LinearProgressIndicator(
                    progress = { weeklyProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = customColor,
                    trackColor = customColor.copy(alpha = 0.12f)
                )
            }

            // --- HORIZONTAL CALENDAR CHECKER STRIP ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 7) {
                    val dateKey = weekDates[i]
                    val label = dayLabels[i]

                    val completion = completions.find { it.habitId == habit.id && it.dateString == dateKey }
                    val isChecked = completion != null && (completion.status == "COMPLETED" || completion.status == null)
                    val isFailed = completion != null && completion.status == "FAILED"
                    val isToday = dateKey == todayString

                    // Background and border selectors based on checks, fails, and today's values
                    val circleBg = when {
                        isChecked -> customColor
                        isFailed -> Color(0xFFEF4444).copy(alpha = 0.15f)
                        isToday -> customColor.copy(alpha = 0.12f)
                        else -> Color.Transparent
                    }

                    val circleBorder = when {
                        isChecked -> null
                        isFailed -> BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        isToday -> BorderStroke(2.dp, customColor)
                        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                    }

                    val txtColor = when {
                        isChecked -> Color.White
                        isFailed -> Color(0xFFEF4444)
                        isToday -> customColor
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(circleBg)
                                .then(if (circleBorder != null) Modifier.border(circleBorder, CircleShape) else Modifier)
                                .clickable {
                                    val currentStatus = if (completion == null) null else (completion.status ?: "COMPLETED")
                                    val nextStatus = when (currentStatus) {
                                        "COMPLETED" -> "FAILED"
                                        "FAILED" -> null
                                        else -> "COMPLETED"
                                    }
                                    onToggleDate(dateKey, nextStatus)
                                }
                                .testTag("habit_day_circle_${habit.id}_$i"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChecked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else if (isFailed) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = txtColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
