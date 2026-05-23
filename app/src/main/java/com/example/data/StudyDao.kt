package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // --- Habits ---
    @Query("SELECT * FROM habits")
    fun getAllHabitsFlow(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    // --- Habit Completions ---
    @Query("SELECT * FROM habit_completions")
    fun getAllHabitCompletionsFlow(): Flow<List<HabitCompletionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletionEntity)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteHabitCompletion(habitId: Int, dateString: String)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsByHabitId(habitId: Int)

    // --- Focus Records ---
    @Query("SELECT * FROM focus_records ORDER BY dateString ASC")
    fun getAllFocusRecordsFlow(): Flow<List<FocusRecordEntity>>

    @Query("SELECT * FROM focus_records WHERE dateString = :dateString LIMIT 1")
    suspend fun getFocusRecordByDate(dateString: String): FocusRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusRecord(record: FocusRecordEntity)

    // --- Class Timetable ---
    @Query("SELECT * FROM classes ORDER BY dayOfWeek ASC, timeSlot ASC")
    fun getAllClassesFlow(): Flow<List<ClassScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classSchedule: ClassScheduleEntity): Long

    @Update
    suspend fun updateClass(classSchedule: ClassScheduleEntity)

    @Delete
    suspend fun deleteClass(classSchedule: ClassScheduleEntity)
}
