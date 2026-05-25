package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }

    // Sort/Filter list logic
    val filteredTasks = remember(tasks, filterState) {
        val list = when (filterState) {
            "Active" -> tasks.filter { !it.completed }
            "Completed" -> tasks.filter { it.completed }
            else -> tasks
        }
        // Group or sort by due date
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
                    .background(FintrixOrangeGradient)
                    .clickable { showAddDialog = true }
                    .testTag("add_task_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOP FILTER CHIPS ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val filters = listOf("Active", "Completed", "All")
                filters.forEach { filter ->
                    val isSelected = filterState == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { filterState = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_$filter")
                    )
                }
            }

            // --- TASK ITEMS list ---
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
                                    nextMon.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
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
