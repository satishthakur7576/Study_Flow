package com.example.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

class StudyViewModel(private val repository: StudyRepository, private val context: Context) : ViewModel() {

    private val sharedPrefs = context.getSharedPreferences("study_flow_prefs", Context.MODE_PRIVATE)

    // --- Student Profile ---
    private val _studentName = MutableStateFlow(sharedPrefs.getString("student_name", "Academic Achiever") ?: "Academic Achiever")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    fun updateStudentName(name: String) {
        sharedPrefs.edit().putString("student_name", name).apply()
        _studentName.value = name
    }

    private val _studentMajor = MutableStateFlow(sharedPrefs.getString("student_major", "Computer Science") ?: "Computer Science")
    val studentMajor: StateFlow<String> = _studentMajor.asStateFlow()

    fun updateStudentMajor(major: String) {
        sharedPrefs.edit().putString("student_major", major).apply()
        _studentMajor.value = major
    }

    private val _studentYear = MutableStateFlow(sharedPrefs.getString("student_year", "Sophomore") ?: "Sophomore")
    val studentYear: StateFlow<String> = _studentYear.asStateFlow()

    fun updateStudentYear(year: String) {
        sharedPrefs.edit().putString("student_year", year).apply()
        _studentYear.value = year
    }

    private val _academicGoal = MutableStateFlow(sharedPrefs.getString("academic_goal", "Aiming for 4.0 GPA 🎓") ?: "Aiming for 4.0 GPA 🎓")
    val academicGoal: StateFlow<String> = _academicGoal.asStateFlow()

    fun updateAcademicGoal(goal: String) {
        sharedPrefs.edit().putString("academic_goal", goal).apply()
        _academicGoal.value = goal
    }

    private val _profileAvatar = MutableStateFlow(sharedPrefs.getString("profile_avatar", "🎓") ?: "🎓")
    val profileAvatar: StateFlow<String> = _profileAvatar.asStateFlow()

    fun updateProfileAvatar(avatar: String) {
        sharedPrefs.edit().putString("profile_avatar", avatar).apply()
        _profileAvatar.value = avatar
    }

    // --- Customizable Accent Themes ---
    private val _themeAccent = MutableStateFlow(sharedPrefs.getString("theme_accent", "Ocean Blue") ?: "Ocean Blue")
    val themeAccent: StateFlow<String> = _themeAccent.asStateFlow()

    fun updateThemeAccent(accent: String) {
        sharedPrefs.edit().putString("theme_accent", accent).apply()
        _themeAccent.value = accent
    }

    // --- Dynamic Dark / Light Theme Mode ---
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun updateDarkTheme(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("is_dark_theme", enabled).apply()
        _isDarkTheme.value = enabled
    }

    // --- Customizable Weekly Study Goal (Hours) ---
    private val _weeklyFocusGoalHours = MutableStateFlow(sharedPrefs.getInt("weekly_focus_goal", 10))
    val weeklyFocusGoalHours: StateFlow<Int> = _weeklyFocusGoalHours.asStateFlow()

    fun updateWeeklyFocusGoal(hours: Int) {
        sharedPrefs.edit().putInt("weekly_focus_goal", hours).apply()
        _weeklyFocusGoalHours.value = hours
    }

    // --- Dynamic Date Tracking for Adaptive Resetting ---
    private val _todayDateString = MutableStateFlow(getTodayDateString())
    val todayDateString: StateFlow<String> = _todayDateString.asStateFlow()

    private val _todayDisplayDate = MutableStateFlow(getTodayDisplayDate())
    val todayDisplayDate: StateFlow<String> = _todayDisplayDate.asStateFlow()

    private val _datesOfCurrentWeekString = MutableStateFlow(getDatesOfCurrentWeekString())
    val datesOfCurrentWeekString: StateFlow<List<String>> = _datesOfCurrentWeekString.asStateFlow()

    private val _todayDayOfWeek = MutableStateFlow(calculateTodayDayOfWeek())
    val todayDayOfWeek: StateFlow<Int> = _todayDayOfWeek.asStateFlow()

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            updateCurrentDates()
        }
    }

    // --- Database Flows ---
    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Register for system time/date broadcast changes
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(dateChangeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(dateChangeReceiver, filter)
            }
        } catch (t: Throwable) {
            // Log or ignore to guarantee application doesn't crash on restrictive OS platforms
        }

        viewModelScope.launch {
            // Out of the box, we start with a clean zero state.
            val isSeeded = sharedPrefs.getBoolean("database_seeded", false)
            if (!isSeeded) {
                sharedPrefs.edit().putBoolean("database_seeded", true).apply()
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

    fun seedDemoData() {
        viewModelScope.launch {
            // Seed habits if they don't exist
            repository.insertHabit(HabitEntity(name = "Exercise", icon = "🏋️", category = "Physical", frequency = "Daily", streak = 4))
            repository.insertHabit(HabitEntity(name = "Read Books", icon = "📚", category = "Intellectual", frequency = "Daily", streak = 6))
            repository.insertHabit(HabitEntity(name = "Wake up early", icon = "🌅", category = "Routine", frequency = "Daily", streak = 5))
            repository.insertHabit(HabitEntity(name = "Learn Coding", icon = "💻", category = "Work", frequency = "Daily", streak = 10))

            // Seed Focus Records if empty to make Yearly Productivity Trend Chart work beautifully
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH) // 0-indexed, up to 11
            for (m in 0..currentMonth) {
                val monthStr = String.format("%02d", m + 1)
                val baseMins = 150 + m * 30 + (if (m % 2 == 0) 60 else 0)
                repository.logFocusMinutes("$year-$monthStr-05", baseMins)
                repository.logFocusMinutes("$year-$monthStr-12", baseMins + 50)
                repository.logFocusMinutes("$year-$monthStr-20", baseMins - 40)
                repository.logFocusMinutes("$year-$monthStr-28", baseMins + 30)
            }

            // Seed some Today and historical tasks to give excellent default user feedback and make trend look full
            val monthStr = String.format("%02d", currentMonth + 1)
            
            // Active and completed tasks for today/this month
            repository.insertTask(TaskEntity(title = "Read Chapter 4 Architecture", subject = "Computer Science", dueDate = "$year-$monthStr-17", priority = "HIGH", completed = false))
            repository.insertTask(TaskEntity(title = "Revise Database Queries", subject = "Database Systems", dueDate = "$year-$monthStr-17", priority = "MEDIUM", completed = true))
            repository.insertTask(TaskEntity(title = "Finish Chemistry Lab Report", subject = "Chemistry", dueDate = "$year-$monthStr-17", priority = "LOW", completed = false))

            // Historical completed tasks across previous months to enrich the trend chart
            for (m in 0..currentMonth) {
                val mStr = String.format("%02d", m + 1)
                repository.insertTask(TaskEntity(title = "Completed Project Prep", subject = "General Academics", dueDate = "$year-$mStr-10", priority = "LOW", completed = true))
                repository.insertTask(TaskEntity(title = "Assigned Homework Sheet", subject = "General Academics", dueDate = "$year-$mStr-22", priority = "MEDIUM", completed = true))
            }
            
            // Re-sync liveness counters
            recalculateHabitStreaks()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            // Reset other SharedPreferences stats
            sharedPrefs.edit()
                .putInt("weekly_focus_goal", 10)
                .putInt("focus_duration_minutes", 25)
                .putInt("break_duration_minutes", 5)
                .apply()
            _weeklyFocusGoalHours.value = 10
            _focusDurationMinutes.value = 25
            _breakDurationMinutes.value = 5
            _timerSecondsLeft.value = 25 * 60
            _timerIsRunning.value = false
            timerJob?.cancel()
        }
    }

    // --- Pomodoro Timer Functions ---
    fun startTimer() {
        if (_timerIsRunning.value) return
        if (_timerSecondsLeft.value <= 0) {
            _timerSecondsLeft.value = if (_isBreakMode.value) _breakDurationMinutes.value * 60 else _focusDurationMinutes.value * 60
        }
        if (_timerSecondsLeft.value <= 0) {
            _timerSecondsLeft.value = 25 * 60 // Safe fallback
        }
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
            // Omit ToneGenerator to prevent SIGSEGV native crashes in headless, virtualized VM audio platforms.

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
    val todayTasksSummary: StateFlow<TodayTasksStats> = combine(tasks, todayDateString) { taskList, todayStr ->
        val dueToday = taskList.filter { it.dueDate == todayStr }
        val completedToday = dueToday.count { it.completed }
        TodayTasksStats(dueCount = dueToday.size, completedCount = completedToday)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayTasksStats(0, 0))

    val todayFocusMinutes: StateFlow<Int> = combine(focusRecords, todayDateString) { list, todayStr ->
        list.find { it.dateString == todayStr }?.durationMinutes ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overallAttendancePercentage: StateFlow<Float> = classes.map { list ->
        val totalAttended = list.sumOf { it.attendedCount }
        val totalSessions = list.sumOf { it.totalCount }
        if (totalSessions == 0) 0f else (totalAttended.toFloat() / totalSessions.toFloat()) * 100f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val currentStreak: StateFlow<Int> = combine(completions, focusRecords, todayDateString) { hCompletes, fRecords, _ ->
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

    // --- Lifetime Statistics Compilation ---
    val lifetimeAnalytics: StateFlow<LifetimeAnalyticsStats> = combine(
        focusRecords, tasks, habits, completions, classes
    ) { focusRecordsList, tasksList, habitsList, completionsList, classesList ->
        val totalFocusMin = focusRecordsList.sumOf { it.durationMinutes }
        val totalCreatedTasks = tasksList.size
        val totalCompletedTasks = tasksList.count { it.completed }
        val totalHabitComp = completionsList.filter { it.status == "COMPLETED" || it.status == null }.size
        
        val completionsByHabit = completionsList
            .filter { it.status == "COMPLETED" || it.status == null }
            .groupBy { it.habitId }
        val topHabitIdAndCount = completionsByHabit.maxByOrNull { it.value.size }
        val topHabit = topHabitIdAndCount?.let { entry ->
            habitsList.find { it.id == entry.key }
        }
        val topHabitName = topHabit?.name ?: "None"
        val topHabitIcon = topHabit?.icon ?: "🎯"

        val totalAttended = classesList.sumOf { it.attendedCount }
        val totalClassSessions = classesList.sumOf { it.totalCount }
        val attendancePct = if (totalClassSessions == 0) 0f else (totalAttended.toFloat() / totalClassSessions.toFloat()) * 100f

        var badgesCount = 0
        if (totalFocusMin >= 300) badgesCount++ // Focus Monk (5+ hours)
        if (totalFocusMin > 0) badgesCount++   // Focus Initiate
        if (totalCompletedTasks >= 5) badgesCount++ // High-Achiever
        if (totalHabitComp >= 10) badgesCount++ // Unstoppable
        if (attendancePct >= 80f && totalClassSessions > 0) badgesCount++ // Committed Scholar

        LifetimeAnalyticsStats(
            totalFocusMinutes = totalFocusMin,
            totalTasksCreated = totalCreatedTasks,
            totalTasksCompleted = totalCompletedTasks,
            totalHabitCompletions = totalHabitComp,
            topHabitName = topHabitName,
            topHabitIcon = topHabitIcon,
            overallAttendancePercentage = attendancePct,
            totalClassesAttended = totalAttended,
            totalClassesCount = totalClassSessions,
            activeBadgesCount = badgesCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LifetimeAnalyticsStats())

    // --- Dynamic Daily Contributions Heatmap Data ---
    val dailyContributions: StateFlow<Map<String, Int>> = combine(
        focusRecords, completions, tasks
    ) { focusRecordsList, completionsList, tasksList ->
        val contributionsMap = mutableMapOf<String, Int>()

        // 1. Focus record contributions (1 point for every 15 minutes, minimum 1 point if focused)
        focusRecordsList.forEach { record ->
            val date = record.dateString
            val points = if (record.durationMinutes <= 0) 0 else (record.durationMinutes / 15).coerceAtLeast(1)
            contributionsMap[date] = (contributionsMap[date] ?: 0) + points
        }

        // 2. Habit completions count: 1 point for each completed habit on that day
        completionsList.filter { it.status == "COMPLETED" || it.status == null }.forEach { comp ->
            val date = comp.dateString
            contributionsMap[date] = (contributionsMap[date] ?: 0) + 1
        }

        // 3. Task completions count: 1 point for each completed task on its due date
        tasksList.filter { it.completed }.forEach { task ->
            val date = task.dueDate
            contributionsMap[date] = (contributionsMap[date] ?: 0) + 1
        }

        contributionsMap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

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
        // Find Monday of current week in a robust, locale-independent way
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        for (i in 0 until 7) {
            dates.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dates
    }

    private fun calculateTodayDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        return if (day == Calendar.SUNDAY) 7 else day - 1
    }

    fun updateCurrentDates() {
        val todayStr = getTodayDateString()
        if (_todayDateString.value != todayStr) {
            _todayDateString.value = todayStr
            _todayDisplayDate.value = getTodayDisplayDate()
            _datesOfCurrentWeekString.value = getDatesOfCurrentWeekString()
            _todayDayOfWeek.value = calculateTodayDayOfWeek()
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(dateChangeReceiver)
        } catch (t: Throwable) {
            // Ignore if already unregistered or not registered
        }
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

data class LifetimeAnalyticsStats(
    val totalFocusMinutes: Int = 0,
    val totalTasksCreated: Int = 0,
    val totalTasksCompleted: Int = 0,
    val totalHabitCompletions: Int = 0,
    val topHabitName: String = "None",
    val topHabitIcon: String = "🎯",
    val overallAttendancePercentage: Float = 0f,
    val totalClassesAttended: Int = 0,
    val totalClassesCount: Int = 0,
    val activeBadgesCount: Int = 0
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
