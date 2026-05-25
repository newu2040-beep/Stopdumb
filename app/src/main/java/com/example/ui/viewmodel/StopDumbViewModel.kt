package com.example.ui.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.StopDumbDatabase
import com.example.data.model.*
import com.example.data.repository.StopDumbRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat

class StopDumbViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StopDumbRepository

    init {
        val db = StopDumbDatabase.getDatabase(application)
        repository = StopDumbRepository(db)
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }

        // Fetch real system apps
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val packages = pm.getInstalledPackages(0)
                val filtered = packages.filter { 
                    it.applicationInfo?.let { info -> (info.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0 } ?: false
                }.mapNotNull { pkg ->
                    pkg.applicationInfo?.let { info ->
                        AppInfo(
                            packageName = pkg.packageName,
                            appName = pm.getApplicationLabel(info).toString(),
                            category = if (pkg.packageName.contains("game", ignoreCase = true)) "Gaming" else "Social"
                        )
                    }
                }.sortedBy { it.appName }
                _installedApps.value = filtered
            } catch (e: Exception) {
                // Fallback or empty
            }
        }
        
        // Real-time stat simulation to give "live" feeling
        viewModelScope.launch {
            while(true) {
                delay(120000) // Every 2 minutes
                // Randomly increment a stat
                val current = globalSettings.value
                val chance = (1..100).random()
                if (chance > 80) {
                    repository.saveGlobalSettings(current.copy(totalPickupsToday = current.totalPickupsToday + 1))
                } else if (chance < 10) {
                    repository.saveGlobalSettings(current.copy(totalUnlocksToday = current.totalUnlocksToday + 1))
                }
            }
        }
    }

    // Reactive database data streams
    val appUsages: StateFlow<List<AppUsageRecord>> = repository.appUsages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSessionRecord>> = repository.focusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitRecord>> = repository.habits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wellnessLogs: StateFlow<List<WellnessLogRecord>> = repository.wellnessLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockedAttempts: StateFlow<List<BlockedAttemptRecord>> = repository.blockedAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appBlocks: StateFlow<List<AppBlockConfig>> = repository.appBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val globalSettings: StateFlow<GlobalWellnessSettings> = repository.globalSettings
        .map { it ?: GlobalWellnessSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalWellnessSettings())

    // Daily Analysis computed state
    val dailyAnalysis: StateFlow<DailyAnalysisData> = combine(appUsages, focusSessions, globalSettings) { usages, sessions, settings ->
        calculateDailyAnalysis(usages, sessions, settings)
    }
    .flowOn(Dispatchers.Default)
    .distinctUntilChanged()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyAnalysisData())

    // UI state states
    var activeTab by mutableStateOf("Dashboard") // Dashboard, Distraction, Productivity, Wellness, Themes
    var activeSubScreen by mutableStateOf<String?>(null) // null, "Breathing", "PomodoroTimer", "HabitEditor", "WellnessLog", "DailyAnalysis"

    // Real System Apps state
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // Live Pomodoro / Deep Work Timer states
    var pomodoroTimeMinutes by mutableStateOf(25)
    var pomodoroSecondsRemaining by mutableStateOf(25 * 60)
    var isTimerRunning by mutableStateOf(false)
    var timerCategory by mutableStateOf("Deep Work") // Pomodoro, Deep Work, Study Planner, Digital Detox
    private var timerJob: Job? = null

    // Mindful Breathing Exercises states
    var breathingPhase by mutableStateOf("Ready") // Inhale, Hold, Exhale, Breathe Out, Ready
    var breathingTicksRemaining by mutableStateOf(0)
    var breathingCycleCounter by mutableStateOf(0)
    var isBreathingActive by mutableStateOf(false)
    private var breathingJob: Job? = null

    // Simulation Popups for addictive app blocks and prompts
    var activeSimulationDialog by mutableStateOf<String?>(null) // null, "IntentionCheck", "LimitsAlert", "RealityCheck"
    var simAppPackName by mutableStateOf("")
    var simAppName by mutableStateOf("")
    var intentionInputText by mutableStateOf("")
    var appOpeningCountSimulated by mutableStateOf(0)

    // Motivational Quote Bank
    val inspirationalQuotes = listOf(
        "Dumb scrolling feeds the void. Deep work builds your future.",
        "Your attention is the gold currency of the 21st century. Don't donate it to algorithms.",
        "Look around you. Real life doesn't look back at you through a blue light filter.",
        "A distraction-free hour is worth three hours of multi-tasking noise.",
        "Make your phone a utility, not a slot machine.",
        "Your dopamine belongs to your goals, not social notifications."
    )

    // Habits actions
    fun addHabit(name: String, category: String, frequency: String) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitRecord(
                    name = name,
                    targetFrequency = frequency,
                    category = category,
                    currentStreak = 0,
                    maxStreak = 0,
                    totalCompletions = 0,
                    lastCompletedTimestamp = 0L
                )
            )
        }
    }

    fun completeHabit(habit: HabitRecord) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val diff = now - habit.lastCompletedTimestamp
            val isNextDay = diff > 16 * 3600 * 1000 // 16 hours window

            val newStreak = if (habit.lastCompletedTimestamp == 0L || isNextDay) {
                habit.currentStreak + 1
            } else habit.currentStreak

            val newMax = if (newStreak > habit.maxStreak) newStreak else habit.maxStreak

            repository.updateHabit(
                habit.copy(
                    currentStreak = newStreak,
                    maxStreak = newMax,
                    totalCompletions = habit.totalCompletions + 1,
                    lastCompletedTimestamp = now
                )
            )
            
            // Add a positive usage metric back as helper
            addProductiveAppUsageMs(1000 * 60 * 5) // add 5 mins productivity
        }
    }

    fun removeHabit(id: Int) {
        viewModelScope.launch {
            repository.deleteHabit(id)
        }
    }

    // App Blocker list modifications
    fun toggleAppBlock(config: AppBlockConfig) {
        viewModelScope.launch {
            repository.updateAppBlockConfig(config.copy(isBlocked = !config.isBlocked))
        }
    }

    fun updateAppLimits(config: AppBlockConfig, limitMin: Int, cooldownSec: Int) {
        viewModelScope.launch {
            repository.updateAppBlockConfig(
                config.copy(
                    dailyLimitMinutes = limitMin,
                    launchCooldownSeconds = cooldownSec
                )
            )
        }
    }

    // Daily pickups/unlock simulation increment
    fun triggerPhysicalInteraction(type: String) {
        viewModelScope.launch {
            val current = globalSettings.value
            val updated = if (type == "pickup") {
                current.copy(totalPickupsToday = current.totalPickupsToday + 1)
            } else {
                current.copy(totalUnlocksToday = current.totalUnlocksToday + 1)
            }
            repository.saveGlobalSettings(updated)
        }
    }

    // Theme changes
    fun changeTheme(themeName: String) {
        viewModelScope.launch {
            repository.saveGlobalSettings(globalSettings.value.copy(currentTheme = themeName))
        }
    }

    fun toggleAmoledMode(active: Boolean) {
        viewModelScope.launch {
            repository.saveGlobalSettings(globalSettings.value.copy(isAmoledBlack = active))
        }
    }

    fun toggleInterruptionInterventions(active: Boolean) {
        viewModelScope.launch {
            repository.saveGlobalSettings(globalSettings.value.copy(isInterruptionQuestionsActive = active))
        }
    }

    fun toggleHardcoreMode(active: Boolean) {
        viewModelScope.launch {
            repository.saveGlobalSettings(globalSettings.value.copy(isHardcoreModeActive = active))
        }
    }

    fun toggleFreezeMode(active: Boolean) {
        viewModelScope.launch {
            repository.saveGlobalSettings(globalSettings.value.copy(isFreezeActive = active))
        }
    }

    // Wellness scale
    fun registerWellnessLog(mood: Int, energy: Int, sleepHours: Float, note: String) {
        viewModelScope.launch {
            repository.insertWellnessLog(
                WellnessLogRecord(
                    moodValue = mood,
                    energyLevel = energy,
                    sleepHours = sleepHours,
                    logNote = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // Pomodoro Timer control
    fun selectTimerConfig(minutes: Int, category: String) {
        pomodoroTimeMinutes = minutes
        pomodoroSecondsRemaining = minutes * 60
        timerCategory = category
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        timerJob = viewModelScope.launch {
            while (pomodoroSecondsRemaining > 0) {
                delay(1000)
                pomodoroSecondsRemaining--
            }
            // Timer complete! Save to DB
            repository.insertSession(
                FocusSessionRecord(
                    category = timerCategory,
                    durationMinutes = pomodoroTimeMinutes,
                    completed = true,
                    rating = 5, // Top focus rating
                    timestamp = System.currentTimeMillis()
                )
            )
            // Add app usage for StopDumb productivity
            addProductiveAppUsageMs(pomodoroTimeMinutes * 60 * 1000L)

            isTimerRunning = false
            pomodoroSecondsRemaining = pomodoroTimeMinutes * 60
        }
    }

    fun pauseTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        isTimerRunning = false
        timerJob?.cancel()
        pomodoroSecondsRemaining = pomodoroTimeMinutes * 60
    }

    // Breathing exercises control
    fun startBreathingExercise() {
        if (isBreathingActive) return
        isBreathingActive = true
        breathingCycleCounter = 0
        breathingPhase = "Get Ready"
        breathingTicksRemaining = 3

        breathingJob = viewModelScope.launch {
            delay(3000) // initial warm up
            while (isBreathingActive) {
                // Inhale (4s)
                breathingPhase = "Inhale"
                breathingTicksRemaining = 4
                while (breathingTicksRemaining > 0) {
                    delay(1000)
                    breathingTicksRemaining--
                }

                // Hold (4s)
                breathingPhase = "Hold Breath"
                breathingTicksRemaining = 4
                while (breathingTicksRemaining > 0) {
                    delay(1000)
                    breathingTicksRemaining--
                }

                // Exhale (4s)
                breathingPhase = "Exhale"
                breathingTicksRemaining = 4
                while (breathingTicksRemaining > 0) {
                    delay(1000)
                    breathingTicksRemaining--
                }

                // Hold (4s)
                breathingPhase = "Rest"
                breathingTicksRemaining = 4
                while (breathingTicksRemaining > 0) {
                    delay(1000)
                    breathingTicksRemaining--
                }

                breathingCycleCounter++
                if (breathingCycleCounter >= 4) { // Complete 4 cycles (about 1 minute)
                    isBreathingActive = false
                    // Record focus rating
                    repository.insertSession(
                        FocusSessionRecord(
                            category = "Digital Detox",
                            durationMinutes = 1,
                            completed = true,
                            rating = 4,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    fun stopBreathingExercise() {
        isBreathingActive = false
        breathingJob?.cancel()
        breathingPhase = "Ready"
        breathingTicksRemaining = 0
    }

    // Simulated app launches
    fun simulateAppLaunch(packageName: String, label: String, category: String) {
        simAppPackName = packageName
        simAppName = label
        intentionInputText = ""

        viewModelScope.launch {
            // Find if this app has limits
            val configs = repository.getBlockedAppsSync()
            val appConfig = configs.find { it.packageName == packageName }

            val settings = repository.getSettingsSync() ?: GlobalWellnessSettings()

            // 1. Check if freeze mode is active
            if (settings.isFreezeActive && category != "Productivity") {
                triggerAppBlockedRecord(label, packageName, "Freeze Mode Active")
                activeSimulationDialog = "LimitsAlert" // Shows emergency lock screen
                return@launch
            }

            // 2. Check limits and intervention
            if (appConfig != null && appConfig.isBlocked) {
                appOpeningCountSimulated = appConfig.currentLaunchesToday + 1
                
                // Increment launch in block log
                repository.updateAppBlockConfig(appConfig.copy(currentLaunchesToday = appOpeningCountSimulated))

                if (appConfig.dailyLimitMinutes > 0 && appConfig.currentLaunchesToday * 3 >= appConfig.dailyLimitMinutes) {
                    // Simulated limit exceeded
                    triggerAppBlockedRecord(label, packageName, "Daily Limit Reached")
                    activeSimulationDialog = "LimitsAlert"
                    return@launch
                }

                if (settings.isInterruptionQuestionsActive) {
                    activeSimulationDialog = "IntentionCheck" // Ask "Why are you opening this?"
                    return@launch
                }
            }

            // Standard opening success - Add distracting launch logs
            addDistractingAppUsageMs(packageName, label, category, 1000 * 60 * 15) // simulates 15 mins look-around
            activeSimulationDialog = "RealityCheck" // Triggers scrolling check-in alert!
        }
    }

    fun bypassSimulatedBlock(reason: String) {
        // Log bypassed opening
        activeSimulationDialog = "RealityCheck"
        viewModelScope.launch {
            addDistractingAppUsageMs(simAppPackName, simAppName, "Social", 1000 * 60 * 5)
        }
    }

    fun closeSimulationDialogs() {
        activeSimulationDialog = null
    }

    private suspend fun triggerAppBlockedRecord(appName: String, packageName: String, reason: String) {
        repository.insertBlockedAttempt(
            BlockedAttemptRecord(
                appName = appName,
                packageName = packageName,
                blockedReason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun addDistractingAppUsageMs(packageName: String, label: String, category: String, durationMs: Long) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayMidnight = cal.timeInMillis

        val currentUsages = repository.appUsages.firstOrNull() ?: emptyList()
        val match = currentUsages.find { it.packageName == packageName && it.dayTimestamp == todayMidnight }

        val updatedRecord = if (match != null) {
            match.copy(
                durationMs = match.durationMs + durationMs,
                launchCount = match.launchCount + 1
            )
        } else {
            AppUsageRecord(
                packageName = packageName,
                appName = label,
                category = category,
                durationMs = durationMs,
                launchCount = 1,
                dayTimestamp = todayMidnight
            )
        }
        repository.insertUsages(listOf(updatedRecord))
    }

    private suspend fun addProductiveAppUsageMs(durationMs: Long) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayMidnight = cal.timeInMillis

        val currentUsages = repository.appUsages.firstOrNull() ?: emptyList()
        val match = currentUsages.find { it.packageName == "com.stopdumb.app" && it.dayTimestamp == todayMidnight }

        val updatedRecord = if (match != null) {
            match.copy(
                durationMs = match.durationMs + durationMs,
                launchCount = match.launchCount + 1
            )
        } else {
            AppUsageRecord(
                packageName = "com.stopdumb.app",
                appName = "StopDumb",
                category = "Productivity",
                durationMs = durationMs,
                launchCount = 1,
                dayTimestamp = todayMidnight
            )
        }
        repository.insertUsages(listOf(updatedRecord))
    }

    private fun calculateDailyAnalysis(
        usages: List<AppUsageRecord>,
        sessions: List<FocusSessionRecord>,
        settings: GlobalWellnessSettings
    ): DailyAnalysisData {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val today = cal.timeInMillis

        val todayUsages = usages.filter { it.dayTimestamp == today }
        val todaySessions = sessions.filter { it.timestamp >= today }

        val totalMs = todayUsages.sumOf { it.durationMs }
        val productiveMs = todayUsages.filter { it.category == "Productivity" }.sumOf { it.durationMs }
        val socialMs = todayUsages.filter { it.category == "Social" }.sumOf { it.durationMs }
        val gamingMs = todayUsages.filter { it.category == "Gaming" }.sumOf { it.durationMs }
        val distractingMs = socialMs + gamingMs

        val topSink = todayUsages.filter { it.category != "Productivity" }
            .maxByOrNull { it.durationMs }

        val focusScore = if (totalMs > 0) {
            val base = (productiveMs.toFloat() / totalMs.toFloat() * 100).toInt()
            // Boost for completed sessions
            val sessionBoost = todaySessions.filter { it.completed }.size * 5
            (base + sessionBoost).coerceIn(0, 100)
        } else 100

        val recommendation = when {
            distractingMs > productiveMs -> "Your dopamine levels are spiked. Take a 5-minute breathing break to reset."
            productiveMs > 1000 * 60 * 60 * 2 -> "Incredible deep work flow! Protect your momentum by doing a quick detox session."
            settings.totalPickupsToday > 50 -> "Frequent pickups detected. Try putting your phone in another room for 30 minutes."
            else -> "Steady discipline. You're outperforming 85% of users today."
        }

        return DailyAnalysisData(
            totalTimeMs = totalMs,
            productiveTimeMs = productiveMs,
            distractingTimeMs = distractingMs,
            focusScore = focusScore,
            topSinkName = topSink?.appName ?: "None",
            topSinkTimeMs = topSink?.durationMs ?: 0L,
            recommendation = recommendation,
            pickups = settings.totalPickupsToday,
            unlocks = settings.totalUnlocksToday
        )
    }
}

data class DailyAnalysisData(
    val totalTimeMs: Long = 0,
    val productiveTimeMs: Long = 0,
    val distractingTimeMs: Long = 0,
    val focusScore: Int = 0,
    val topSinkName: String = "None",
    val topSinkTimeMs: Long = 0,
    val recommendation: String = "Keep building focus.",
    val pickups: Int = 0,
    val unlocks: Int = 0
)
