package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TaskEntity
import com.example.ui.StudyViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    var filterState by remember { mutableStateOf("Active") } // "All", "Active", "Completed"
    var viewMode by remember { mutableStateOf("List") } // "List" or "Board"
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    // Dynamic gradient brush from primary and secondary colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val dynamicFabGradient = remember(primaryColor, secondaryColor) {
        Brush.linearGradient(colors = listOf(primaryColor, secondaryColor))
    }

    // Sort/Filter list logic
    val filteredTasks = remember(tasks, filterState) {
        val list = when (filterState) {
            "Active" -> tasks.filter { !it.completed }
            "Completed" -> tasks.filter { it.completed }
            else -> tasks
        }
        list.sortedBy { it.dueDate }
    }

    Scaffold(
        modifier = modifier.testTag("task_screen"),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(dynamicFabGradient)
                    .clickable { showAddDialog = true }
                    .testTag("add_task_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            // --- TOP SWITCHER & FILTERS ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Mode Switcher: segmented capsule list
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val views = listOf("List", "Board")
                    views.forEach { mode ->
                        val isSelected = viewMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable { viewMode = mode }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (mode == "List") Icons.Default.List else Icons.Default.Dashboard,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Filter Chips (Only shown in List mode)
                if (viewMode == "List") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val filters = listOf("Active", "Completed")
                        filters.forEach { filter ->
                            val isSelected = filterState == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { filterState = filter }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = filter,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // Inline board helpful hint
                    Text(
                        text = "Kanban Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- MAIN INTERFACE: LIST OR BOARD ---
            if (viewMode == "List") {
                // --- LIST VIEW ---
                if (filteredTasks.isEmpty()) {
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
                                imageVector = Icons.Default.Task,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "No tasks found in \"$filterState\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Tap the '+' button to log a homework, test, or project assignment.",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp) // Cushion fab
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskListItem(
                                task = task,
                                onToggleComplete = { isChecked ->
                                    viewModel.updateTaskCompletion(task, isChecked)
                                },
                                onEdit = { taskToEdit = task },
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            } else {
                // --- KANBAN BOARD VIEW (HORIZONTALLY SCROLLABLE LANES) ---
                val urgentTasks = remember(tasks) { tasks.filter { !it.completed && (it.priority == "URGENT" || it.priority == "HIGH") } }
                val mediumTasks = remember(tasks) { tasks.filter { !it.completed && it.priority == "MEDIUM" } }
                val lowTasks = remember(tasks) { tasks.filter { !it.completed && it.priority == "LOW" } }
                val completedTasks = remember(tasks) { tasks.filter { it.completed } }

                val lanes = listOf(
                    KanbanLaneData("Urgent ⚡", urgentTasks, HighPriorityColor, "URGENT"),
                    KanbanLaneData("Medium 📈", mediumTasks, MediumPriorityColor, "MEDIUM"),
                    KanbanLaneData("Low 🌱", lowTasks, LowPriorityColor, "LOW"),
                    KanbanLaneData("Completed ✅", completedTasks, MaterialTheme.colorScheme.primary, "COMPLETED")
                )

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    lanes.forEach { lane ->
                        KanbanLaneColumn(
                            laneTitle = lane.title,
                            laneTasks = lane.tasks,
                            accentColor = lane.accentColor,
                            priorityString = lane.priority,
                            onToggleComplete = { task, completed ->
                                viewModel.updateTaskCompletion(task, completed)
                            },
                            onCyclePriority = { task ->
                                val nextPriority = when (task.priority) {
                                    "LOW" -> "MEDIUM"
                                    "MEDIUM" -> "URGENT"
                                    else -> "LOW"
                                }
                                viewModel.updateTaskDetails(task.copy(priority = nextPriority))
                            },
                            onEdit = { task -> taskToEdit = task },
                            onDelete = { task -> viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
        }
    }

    // --- ADD TASK DIALOG ---
    if (showAddDialog) {
        TaskAddEditDialog(
            title = "New Assignment",
            onDismiss = { showAddDialog = false },
            onSave = { title, subject, dueDate, priority ->
                viewModel.addTask(title, subject, dueDate, priority)
                showAddDialog = false
            }
        )
    }

    // --- EDIT TASK DIALOG ---
    if (taskToEdit != null) {
        val activeTask = taskToEdit!!
        TaskAddEditDialog(
            title = "Edit Assignment",
            initialTask = activeTask,
            onDismiss = { taskToEdit = null },
            onSave = { title, subject, dueDate, priority ->
                viewModel.updateTaskDetails(
                    activeTask.copy(
                        title = title,
                        subject = subject,
                        dueDate = dueDate,
                        priority = priority
                    )
                )
                taskToEdit = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskListItem(
    task: TaskEntity,
    onToggleComplete: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityBordersColor = when (task.priority.uppercase()) {
        "URGENT", "HIGH" -> HighPriorityColor
        "MEDIUM" -> MediumPriorityColor
        else -> LowPriorityColor
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val displayDate = remember(task.dueDate) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(task.dueDate)
            if (task.dueDate == todayStr) {
                "Today ⏰"
            } else {
                SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date!!)
            }
        } catch (e: Exception) {
            task.dueDate
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}")
            .clip(RoundedCornerShape(16.dp))
            .background(cardGradient())
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left priority margin ribbon indicator
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .heightIn(min = 72.dp)
                    .background(priorityBordersColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Checkbox
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = { onToggleComplete(it) },
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f.coerceAtMost(0.5f)) else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                    )

                    FlowRow(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Subject Tag
                        if (task.subject.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.subject,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Color-Coded Priority Badge (Soft, Premium Pastel)
                        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
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
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = when (task.priority.uppercase()) {
                                    "URGENT", "HIGH" -> "High"
                                    "MEDIUM" -> "Medium"
                                    else -> "Low"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = pTxt,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        // Due Date Display
                        Text(
                            text = "📅 $displayDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (task.dueDate == todayStr && !task.completed) HighPriorityColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                // Delete Icon Trigger
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_task_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = HighPriorityColor.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskAddEditDialog(
    title: String,
    initialTask: TaskEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, subject: String, dueDate: String, priority: String) -> Unit
) {
    var titleInput by remember { mutableStateOf(initialTask?.title ?: "") }
    var subjectInput by remember { mutableStateOf(initialTask?.subject ?: "") }
    var dateInput by remember {
        mutableStateOf(
            initialTask?.dueDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
    }
    var priorityInput by remember { mutableStateOf(initialTask?.priority ?: "MEDIUM") }
    var titleError by remember { mutableStateOf(false) }

    val priorities = listOf("LOW", "MEDIUM", "URGENT")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title Field
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Task Title", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = {
                                titleInput = it
                                titleError = false
                            },
                            placeholder = { Text("Complete mid-term report") },
                            isError = titleError,
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_title_input")
                        )
                        if (titleError) {
                            Text("Title cannot be blank.", color = HighPriorityColor, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Subject Name Field (User enters manually)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Subject / Class Tag (Optional)", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = subjectInput,
                            onValueChange = {
                                subjectInput = it
                            },
                            placeholder = { Text("e.g. Maths, Physics, CAD") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_subject_input")
                        )
                    }
                }

                // Priorities Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Priority Level", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            priorities.forEach { priority ->
                                val active = priorityInput == priority
                                val activeColor = when (priority) {
                                    "URGENT" -> HighPriorityColor
                                    "MEDIUM" -> MediumPriorityColor
                                    else -> LowPriorityColor
                                }

                                FilterChip(
                                    selected = active,
                                    onClick = { priorityInput = priority },
                                    label = {
                                        Text(
                                            priority,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = activeColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Due Date input and helper buttons
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Due Date (YYYY-MM-DD)", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = dateInput,
                            onValueChange = { dateInput = it },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_date_input")
                        )

                        // DATE QUICK SHORTCUTS
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

                            // Today Shortcut Button
                            TextButton(
                                onClick = { dateInput = sdf.format(Date()) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Today")
                            }

                            // Tomorrow Shortcut Button
                            TextButton(
                                onClick = {
                                    val tomorrow = Calendar.getInstance()
                                    tomorrow.add(Calendar.DAY_OF_YEAR, 1)
                                    dateInput = sdf.format(tomorrow.time)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Tomorrow")
                            }

                            // Next Monday Shortcut Button
                            TextButton(
                                onClick = {
                                    val nextMon = Calendar.getInstance()
                                    val dayOfWeek = nextMon.get(Calendar.DAY_OF_WEEK)
                                    val daysToSubtract = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
                                    nextMon.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                                    if (nextMon.timeInMillis <= System.currentTimeMillis()) {
                                        nextMon.add(Calendar.WEEK_OF_YEAR, 1)
                                    }
                                    dateInput = sdf.format(nextMon.time)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Next Mon")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val isTitleBlank = titleInput.isBlank()
                    
                    if (isTitleBlank) {
                        titleError = true
                    }
                    
                    if (!isTitleBlank) {
                        onSave(titleInput.trim(), subjectInput.trim(), dateInput.trim(), priorityInput)
                    }
                },
                modifier = Modifier.testTag("dialog_save_task")
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class KanbanLaneData(
    val title: String,
    val tasks: List<TaskEntity>,
    val accentColor: Color,
    val priority: String
)

@Composable
fun KanbanLaneColumn(
    laneTitle: String,
    laneTasks: List<TaskEntity>,
    accentColor: Color,
    priorityString: String,
    onToggleComplete: (TaskEntity, Boolean) -> Unit,
    onCyclePriority: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Lane Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Text(
                    text = laneTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Task Counter badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${laneTasks.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )

        // Cards List
        if (laneTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "No cards",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(laneTasks, key = { it.id }) { task ->
                    KanbanCardItem(
                        task = task,
                        accentColor = accentColor,
                        onToggleComplete = { onToggleComplete(task, it) },
                        onCyclePriority = { onCyclePriority(task) },
                        onEdit = { onEdit(task) },
                        onDelete = { onDelete(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun KanbanCardItem(
    task: TaskEntity,
    accentColor: Color,
    onToggleComplete: (Boolean) -> Unit,
    onCyclePriority: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.completed,
                        onCheckedChange = { onToggleComplete(it) },
                        modifier = Modifier.size(24.dp)
                    )
                    
                    if (task.subject.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.subject,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (!task.completed) {
                    IconButton(
                        onClick = onCyclePriority,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Cycle lane priority",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = HighPriorityColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

