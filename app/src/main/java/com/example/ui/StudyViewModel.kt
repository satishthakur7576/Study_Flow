package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StudyViewModel(private val repository: StudyRepository, context: Context) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("study_flow_prefs", Context.MODE_PRIVATE)

    // --- Student Profile ---
    private val _studentName = MutableStateFlow(sharedPrefs.getString("student_name", "Academic Achiever") ?: "Academic Achiever")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    fun updateStudentName(name: String) {
        sharedPrefs.edit().putString("student_name", name).apply()
        _studentName.value = name
    }

    // --- Customizable Accent Themes ---
    private val _themeAccent = MutableStateFlow(sharedPrefs.getString("theme_accent", "Ocean Blue") ?: "Ocean Blue")
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    fun updateThemeAccent(accent: String) {
        sharedPrefs.edit().putString("theme_accent", accent).apply()
        _themeAccent.value = accent
    }

    // --- Database Flows ---
    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Wait briefly to make sure repository flow starts up, or read directly
            val currentHabits = repository.allHabits.first()
            if (currentHabits.isEmpty()) {
                repository.insertHabit(HabitEntity(name = "Exercise", icon = "🏋️", category = "Physical", frequency = "Daily", streak = 4))
                repository.insertHabit(HabitEntity(name = "Read Books", icon = "📚", category = "Intellectual", frequency = "Daily", streak = 6))
                repository.insertHabit(HabitEntity(name = "Wake up early", icon = "🌅", category = "Routine", frequency = "Daily", streak = 5))
                repository.insertHabit(HabitEntity(name = "Learn Coding", icon = "💻", category = "Work", frequency = "Daily", streak = 10))
            }
        }
    }

    val completions: StateFlow<List<HabitCompletionEntity>> = repository.allHabitCompletions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusRecords: StateFlow<List<FocusRecordEntity>> = repository.allFocusRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val classes: StateFlow<List<ClassScheduleEntity>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Pomodoro Timer State ---
    private val _focusDurationMinutes = MutableStateFlow(sharedPrefs.getInt("focus_duration_minutes", 25))
    val focusDurationMinutes: StateFlow<Int> = _focusDurationMinutes.asStateFlow()

    private val _breakDurationMinutes = MutableStateFlow(sharedPrefs.getInt("break_duration_minutes", 5))
    val breakDurationMinutes: StateFlow<Int> = _breakDurationMinutes.asStateFlow()

    private val _timerSecondsLeft = MutableStateFlow(sharedPrefs.getInt("focus_duration_minutes", 25) * 60)
    val timerSecondsLeft: StateFlow<Int> = _timerSecondsLeft.asStateFlow()

    private val _timerIsRunning = MutableStateFlow(false)
    val timerIsRunning: StateFlow<Boolean> = _timerIsRunning.asStateFlow()

    private val _isBreakMode = MutableStateFlow(false)
    val isBreakMode: StateFlow<Boolean> = _isBreakMode.asStateFlow()

    private val _sessionCount = MutableStateFlow(1)
    val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    private val _totalSessions = MutableStateFlow(4)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()

    private var timerJob: Job? = null

    fun setFocusDuration(minutes: Int) {
        val clampMins = minutes.coerceIn(1, 180)
        sharedPrefs.edit().putInt("focus_duration_minutes", clampMins).apply()
        _focusDurationMinutes.value = clampMins
        if (!_isBreakMode.value && !_timerIsRunning.value) {
            _timerSecondsLeft.value = clampMins * 60
        }
    }

    fun setBreakDuration(minutes: Int) {
        val clampMins = minutes.coerceIn(1, 60)
        sharedPrefs.edit().putInt("break_duration_minutes", clampMins).apply()
        _breakDurationMinutes.value = clampMins
        if (_isBreakMode.value && !_timerIsRunning.value) {
            _timerSecondsLeft.value = clampMins * 60
        }
    }

    // --- Task CRUD Handles ---
    fun addTask(title: String, subject: String, dueDate: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    subject = subject,
                    dueDate = dueDate,
                    priority = priority,
                    completed = false
                )
            )
        }
    }

    fun updateTaskCompletion(task: TaskEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTask(task.copy(completed = isCompleted))
        }
    }

    fun updateTaskDetails(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Habit CRUD Handles ---
    fun addHabit(
        name: String,
        description: String = "",
        icon: String = "🌱",
        colorHex: String = "#FD5C25",
        category: String = "Learning",
        frequency: String = "Daily"
    ) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    name = name,
                    description = description,
                    icon = icon,
                    colorHex = colorHex,
                    category = category,
                    frequency = frequency
                )
            )
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitCompletion(habitId: Int, dateString: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, dateString, isCompleted)
            recalculateHabitStreaks()
        }
    }

    fun setHabitStatus(habitId: Int, dateString: String, status: String?) {
        viewModelScope.launch {
            repository.setHabitStatus(habitId, dateString, status)
            recalculateHabitStreaks()
        }
    }

    // --- Class CRUD Handles ---
    fun addClass(subjectName: String, timeSlot: String, roomNumber: String, dayOfWeek: Int) {
        viewModelScope.launch {
            repository.insertClass(
                ClassScheduleEntity(
                    subjectName = subjectName,
                    timeSlot = timeSlot,
                    roomNumber = roomNumber,
                    dayOfWeek = dayOfWeek,
                    attendedCount = 0,
                    totalCount = 0
                )
            )
        }
    }

    fun incrementAttendance(classItem: ClassScheduleEntity) {
        viewModelScope.launch {
            repository.updateClass(
                classItem.copy(
                    attendedCount = classItem.attendedCount + 1,
                    totalCount = classItem.totalCount + 1
                )
            )
        }
    }

    fun incrementTotalSessions(classItem: ClassScheduleEntity) {
        viewModelScope.launch {
            repository.updateClass(
                classItem.copy(
                    totalCount = classItem.totalCount + 1
                )
            )
        }
    }

    fun resetAttendance(classItem: ClassScheduleEntity) {
        viewModelScope.launch {
            repository.updateClass(
                classItem.copy(
                    attendedCount = 0,
                    totalCount = 0
                )
            )
        }
    }

    fun deleteClass(classItem: ClassScheduleEntity) {
        viewModelScope.launch {
            repository.deleteClass(classItem)
        }
    }

    // --- Pomodoro Timer Functions ---
    fun startTimer() {
        if (_timerIsRunning.value) return
        _timerIsRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timerSecondsLeft.value > 0) {
                delay(1000)
                _timerSecondsLeft.value -= 1
            }
            onTimerSectionComplete()
        }
    }

    fun pauseTimer() {
        _timerIsRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _timerIsRunning.value = false
        timerJob?.cancel()
        _timerSecondsLeft.value = if (_isBreakMode.value) _breakDurationMinutes.value * 60 else _focusDurationMinutes.value * 60
    }

    fun skipSession() {
        _timerIsRunning.value = false
        timerJob?.cancel()
        onTimerSectionComplete()
    }

    fun addManualFocusMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.logFocusMinutes(getTodayDateString(), minutes)
        }
    }

    private fun onTimerSectionComplete() {
        viewModelScope.launch {
            try {
                // Play soft beep alert
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
                delay(600)
                toneGen.release()
            } catch (t: Throwable) {
                // Ignore audio-related runtime bugs
            }

            if (!_isBreakMode.value) {
                // Work session ended: Log focus minutes dynamically
                repository.logFocusMinutes(getTodayDateString(), _focusDurationMinutes.value)

                _isBreakMode.value = true
                _timerSecondsLeft.value = _breakDurationMinutes.value * 60
            } else {
                // Break ended: Increment session
                _isBreakMode.value = false
                _timerSecondsLeft.value = _focusDurationMinutes.value * 60

                val currentSession = _sessionCount.value
                val maxSessions = _totalSessions.value
                if (currentSession >= maxSessions) {
                    _sessionCount.value = 1
                } else {
                    _sessionCount.value = currentSession + 1
                }
            }

            _timerIsRunning.value = false
            // Auto start next session if desired
            startTimer()
        }
    }

    // --- Calculated/Derived Metrics for Today ---
    val todayTasksSummary: StateFlow<TodayTasksStats> = tasks.map { taskList ->
        val todayStr = getTodayDateString()
        val dueToday = taskList.filter { it.dueDate == todayStr }
        val completedToday = dueToday.count { it.completed }
        TodayTasksStats(dueCount = dueToday.size, completedCount = completedToday)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayTasksStats(0, 0))

    val todayFocusMinutes: StateFlow<Int> = focusRecords.map { list ->
        val todayStr = getTodayDateString()
        list.find { it.dateString == todayStr }?.durationMinutes ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overallAttendancePercentage: StateFlow<Float> = classes.map { list ->
        val totalAttended = list.sumOf { it.attendedCount }
        val totalSessions = list.sumOf { it.totalCount }
        if (totalSessions == 0) 0f else (totalAttended.toFloat() / totalSessions.toFloat()) * 100f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentStreak: StateFlow<Int> = combine(completions, focusRecords) { hCompletes, fRecords ->
        calculateStreakCount(hCompletes, fRecords)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Habit Streak Updates ---
    private fun recalculateHabitStreaks() {
        viewModelScope.launch {
            val habitsList = repository.allHabits.first()
            val completesList = repository.allHabitCompletions.first()
            for (habit in habitsList) {
                val streak = calculateSingleHabitStreak(habit.id, completesList)
                if (habit.streak != streak) {
                    repository.updateHabit(habit.copy(streak = streak))
                }
            }
        }
    }

    private fun calculateSingleHabitStreak(habitId: Int, completions: List<HabitCompletionEntity>): Int {
        val habitCompletions = completions.filter { it.habitId == habitId && (it.status == "COMPLETED" || it.status == null) }.map { it.dateString }.toSet()
        if (habitCompletions.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // Start checking from today
        var checkedDate = sdf.format(cal.time)
        if (habitCompletions.contains(checkedDate)) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkedDate = sdf.format(cal.time)
            while (habitCompletions.contains(checkedDate)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkedDate = sdf.format(cal.time)
            }
        } else {
            // If not completed today, check yesterday as the starting point
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkedDate = sdf.format(cal.time)
            while (habitCompletions.contains(checkedDate)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkedDate = sdf.format(cal.time)
            }
        }
        return streak
    }

    private fun calculateStreakCount(
        completions: List<HabitCompletionEntity>,
        focusRecords: List<FocusRecordEntity>
    ): Int {
        val completedDatesSet = completions.filter { it.status == "COMPLETED" || it.status == null }.map { it.dateString }.toSet()
        val focusedDatesSet = focusRecords.filter { it.durationMinutes > 0 }.map { it.dateString }.toSet()

        val activeDates = completedDatesSet.union(focusedDatesSet)
        if (activeDates.isEmpty()) return 0

        var streak = 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        var checkedDate = sdf.format(cal.time)
        if (activeDates.contains(checkedDate)) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkedDate = sdf.format(cal.time)
            while (activeDates.contains(checkedDate)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkedDate = sdf.format(cal.time)
            }
        } else {
            // Check yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1)
            checkedDate = sdf.format(cal.time)
            while (activeDates.contains(checkedDate)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
                checkedDate = sdf.format(cal.time)
            }
        }
        return streak
    }

    // --- Weekly Statistics Compilation ---
    val weeklyAnalytics: StateFlow<WeeklyAnalyticsStats> = combine(
        focusRecords, tasks, habits, completions
    ) { focusRecordsList, tasksList, habitsList, completionsList ->
        compileWeeklyStats(focusRecordsList, tasksList, habitsList, completionsList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyAnalyticsStats())

    private fun compileWeeklyStats(
        focus: List<FocusRecordEntity>,
        tasksList: List<TaskEntity>,
        habitsList: List<HabitEntity>,
        completionsList: List<HabitCompletionEntity>
    ): WeeklyAnalyticsStats {
        val weekDates = getDatesOfCurrentWeekString() // 7 dates: Mon-Sun
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        val dailyFocusHours = mutableListOf<Float>()
        val dailyTaskCompletionRates = mutableListOf<Float>()
        val dailyHabitConsistency = mutableListOf<Float>()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in 0 until 7) {
            val dateStr = weekDates[i]

            // Focus hours (duration divided by 60)
            val focusRecord = focus.find { it.dateString == dateStr }
            val focusHrs = (focusRecord?.durationMinutes ?: 0) / 60f
            dailyFocusHours.add(focusHrs)

            // Task completion rate (completed due on that day divided by total due on that day)
            val tasksDueOnDay = tasksList.filter { it.dueDate == dateStr }
            val taskRate = if (tasksDueOnDay.isEmpty()) {
                0f
            } else {
                val completedOnDay = tasksDueOnDay.count { it.completed }
                (completedOnDay.toFloat() / tasksDueOnDay.size.toFloat()) * 100f
            }
            dailyTaskCompletionRates.add(taskRate)

            // Habit consistency (percentage of active habits checked on this day)
            val activeCompletions = completionsList.filter { it.dateString == dateStr && (it.status == "COMPLETED" || it.status == null) }
            val habitRate = if (habitsList.isEmpty()) {
                0f
            } else {
                (activeCompletions.size.toFloat() / habitsList.size.toFloat()) * 100f
            }
            dailyHabitConsistency.add(habitRate)
        }

        return WeeklyAnalyticsStats(
            days = dayNames,
            focusHours = dailyFocusHours,
            taskCompletionRates = dailyTaskCompletionRates,
            habitConsistencyRates = dailyHabitConsistency
        )
    }

    // --- Static Utility Helpers for Date Strings ---
    fun getTodayDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return format.format(Date())
    }

    fun getTodayDisplayDate(): String {
        // e.g. "Wednesday, May 20, 2026"
        val format = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.US)
        return format.format(Date())
    }

    /**
     * Gets formatted dates representing Monday to Sunday of the current week.
     */
    fun getDatesOfCurrentWeekString(): List<String> {
        val dates = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (i in 0 until 7) {
            dates.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dates
    }
}

// --- Supporting DTO Data Classes ---
data class TodayTasksStats(val dueCount: Int, val completedCount: Int)

data class WeeklyAnalyticsStats(
    val days: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val focusHours: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val taskCompletionRates: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f),
    val habitConsistencyRates: List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f)
)

class StudyViewModelFactory(private val repository: StudyRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
