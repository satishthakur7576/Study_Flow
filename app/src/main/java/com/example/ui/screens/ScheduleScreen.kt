package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.RotateLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClassScheduleEntity
import com.example.ui.StudyViewModel
import com.example.ui.theme.*
import java.util.*

@Composable
fun ScheduleScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()

    // Determine current day of week (1 = Mon ... 7 = Sun)
    val todayDayOfWeek by viewModel.todayDayOfWeek.collectAsStateWithLifecycle()

    var selectedDayTab by remember { mutableStateOf(1) }
    LaunchedEffect(todayDayOfWeek) {
        selectedDayTab = todayDayOfWeek
    }
    var showAddDialog by remember { mutableStateOf(false) }

    val daysNameList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val daysShortList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val classesOfSelectedDay = remember(classes, selectedDayTab) {
        classes.filter { it.dayOfWeek == selectedDayTab }.sortedBy { it.timeSlot }
    }

    Scaffold(
        modifier = modifier.testTag("schedule_screen"),
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FintrixOrangeGradient)
                    .clickable { showAddDialog = true }
                    .testTag("add_class_fab"),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Class", tint = Color.White)
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
            // --- TOP HEADER ---
            Column {
                Text(
                    text = "Class Timetable 📅",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Track room assignments & maintain high lecture attendance.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // --- WEEKDAY TAB SELECTORS ROW ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..7) {
                    val label = daysShortList[i - 1]
                    val isSelected = selectedDayTab == i
                    val isToday = todayDayOfWeek == i

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDayTab = i }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White
                                else if (isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "${daysNameList[selectedDayTab - 1]}'s Lectures",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // --- SELECTED DAY CLASSES LIST ---
            if (classesOfSelectedDay.isEmpty()) {
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
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "No Lectures Scheduled!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "No subjects registered for ${daysNameList[selectedDayTab - 1]}. Fulfill core self-study, research, or take off!",
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
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(classesOfSelectedDay, key = { it.id }) { classItem ->
                        ClassCard(
                            classItem = classItem,
                            onIncrementAttended = { viewModel.incrementAttendance(classItem) },
                            onIncrementTotal = { viewModel.incrementTotalSessions(classItem) },
                            onResetAttendance = { viewModel.resetAttendance(classItem) },
                            onDelete = { viewModel.deleteClass(classItem) }
                        )
                    }
                }
            }
        }
    }

    // --- ADD CLASS DIALOG ---
    if (showAddDialog) {
        var subjectInput by remember { mutableStateOf("") }
        var timeInput by remember { mutableStateOf("") }
        var roomInput by remember { mutableStateOf("") }
        var daySelect by remember { mutableStateOf(selectedDayTab) }
        var selectError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Timetable Entry") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Subject Name Field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Subject Name", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = subjectInput,
                            onValueChange = {
                                subjectInput = it
                                selectError = false
                            },
                            placeholder = { Text("Advanced CAD") },
                            singleLine = true,
                            isError = selectError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("class_subject_input")
                        )
                        if (selectError) {
                            Text("Subject name is mandatory.", color = HighPriorityColor, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    // Time Slot
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Time Slot", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = timeInput,
                            onValueChange = { timeInput = it },
                            placeholder = { Text("e.g. 10:00 - 11:30") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("class_time_input")
                        )
                    }

                    // Room Number
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Room Number / Link", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = roomInput,
                            onValueChange = { roomInput = it },
                            placeholder = { Text("Sem-302 / Virtual") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("class_room_input")
                        )
                    }

                    // Weekday Choice
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Schedule Weekday", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            daysShortList.forEachIndexed { index, shortName ->
                                val targetNum = index + 1
                                val isActive = daySelect == targetNum
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isActive) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .clickable { daySelect = targetNum }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shortName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectInput.trim().isBlank()) {
                            selectError = true
                        } else {
                            viewModel.addClass(
                                subjectName = subjectInput.trim(),
                                timeSlot = if (timeInput.isNotBlank()) timeInput.trim() else "N/A",
                                roomNumber = if (roomInput.isNotBlank()) roomInput.trim() else "N/A",
                                dayOfWeek = daySelect
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_save_class")
                ) {
                    Text("Register")
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
fun ClassCard(
    classItem: ClassScheduleEntity,
    onIncrementAttended: () -> Unit,
    onIncrementTotal: () -> Unit,
    onResetAttendance: () -> Unit,
    onDelete: () -> Unit
) {
    val rate = if (classItem.totalCount == 0) 0f else (classItem.attendedCount.toFloat() / classItem.totalCount.toFloat()) * 100f
    val rateColor = when {
        rate >= 75f -> LowPriorityColor // Safe green
        rate >= 50f -> MediumPriorityColor // Caution orange
        else -> HighPriorityColor // Alarm red!
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardGradient())
            .testTag("class_card_${classItem.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- HEADER INFO ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = classItem.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⏰ ${classItem.timeSlot}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "🚪 Room: ${classItem.roomNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("delete_class_button_${classItem.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove class Link",
                        tint = HighPriorityColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

            // --- ATTENDANCE TRACKER LAYOUT ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Attendance Rate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${classItem.attendedCount} / ${classItem.totalCount} Attended",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(rateColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "%.0f%%".format(rate),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = rateColor
                            )
                        }
                    }
                }

                // Interaction controls for recording attendance
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    IconButton(
                        onClick = onResetAttendance,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("reset_attendance_${classItem.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateLeft,
                            contentDescription = "Reset Attendance",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Skipped/Attended incrementers
                    TextButton(
                        onClick = onIncrementTotal,
                        modifier = Modifier.testTag("skip_attendance_${classItem.id}")
                    ) {
                        Text("Missed", color = HighPriorityColor, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onIncrementAttended,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("attend_class_${classItem.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = LowPriorityColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Attend", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
