package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {

    // --- Tasks ---
    val allTasks: Flow<List<TaskEntity>> = studyDao.getAllTasksFlow()

    suspend fun insertTask(task: TaskEntity): Long {
        return studyDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        studyDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        studyDao.deleteTask(task)
    }

    // --- Habits ---
    val allHabits: Flow<List<HabitEntity>> = studyDao.getAllHabitsFlow()

    suspend fun insertHabit(habit: HabitEntity): Long {
        return studyDao.insertHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        studyDao.deleteCompletionsByHabitId(habit.id)
        studyDao.deleteHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        studyDao.updateHabit(habit)
    }

    // --- Habit Completions ---
    val allHabitCompletions: Flow<List<HabitCompletionEntity>> = studyDao.getAllHabitCompletionsFlow()

    suspend fun toggleHabitCompletion(habitId: Int, dateString: String, isCompleted: Boolean) {
        if (isCompleted) {
            studyDao.insertHabitCompletion(HabitCompletionEntity(habitId, dateString, "COMPLETED"))
        } else {
            studyDao.deleteHabitCompletion(habitId, dateString)
        }
    }

    suspend fun setHabitStatus(habitId: Int, dateString: String, status: String?) {
        if (status == null) {
            studyDao.deleteHabitCompletion(habitId, dateString)
        } else {
            studyDao.insertHabitCompletion(HabitCompletionEntity(habitId, dateString, status))
        }
    }

    // --- Focus Records ---
    val allFocusRecords: Flow<List<FocusRecordEntity>> = studyDao.getAllFocusRecordsFlow()

    suspend fun logFocusMinutes(dateString: String, deltaMinutes: Int) {
        val existing = studyDao.getFocusRecordByDate(dateString)
        if (existing != null) {
            studyDao.insertFocusRecord(
                existing.copy(durationMinutes = existing.durationMinutes + deltaMinutes)
            )
        } else {
            studyDao.insertFocusRecord(
                FocusRecordEntity(dateString = dateString, durationMinutes = deltaMinutes)
            )
        }
    }

    // --- Classes ---
    val allClasses: Flow<List<ClassScheduleEntity>> = studyDao.getAllClassesFlow()

    suspend fun insertClass(classSchedule: ClassScheduleEntity): Long {
        return studyDao.insertClass(classSchedule)
    }

    suspend fun updateClass(classSchedule: ClassScheduleEntity) {
        studyDao.updateClass(classSchedule)
    }

    suspend fun deleteClass(classSchedule: ClassScheduleEntity) {
        studyDao.deleteClass(classSchedule)
    }
}
