package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val dueDate: String, // "YYYY-MM-DD"
    val priority: String, // "LOW", "MEDIUM", "URGENT"
    val completed: Boolean = false
)

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val streak: Int = 0,
    val description: String = "",
    val icon: String = "🌱", // Default emoji icon
    val colorHex: String = "#FD5C25", // Carbon Orange Accent
    val category: String = "Learning", // "Learning", "Health", "Personal", "Other"
    val frequency: String = "Daily" // "Daily", "Weekly", "Monthly"
)

@Entity(tableName = "habit_completions", primaryKeys = ["habitId", "dateString"])
data class HabitCompletionEntity(
    val habitId: Int,
    val dateString: String // "YYYY-MM-DD"
)

@Entity(tableName = "focus_records")
data class FocusRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateString: String, // "YYYY-MM-DD"
    val durationMinutes: Int
)

@Entity(tableName = "classes")
data class ClassScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val timeSlot: String, // e.g. "09:00 - 10:30"
    val roomNumber: String,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val attendedCount: Int = 0,
    val totalCount: Int = 0
)
