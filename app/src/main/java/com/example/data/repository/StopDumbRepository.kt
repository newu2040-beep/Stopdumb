package com.example.data.repository

import android.content.Context
import com.example.data.database.StopDumbDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class StopDumbRepository(private val db: StopDumbDatabase) {

    val appUsages: Flow<List<AppUsageRecord>> = db.appUsageDao().getAllUsagesFlow()
    val focusSessions: Flow<List<FocusSessionRecord>> = db.focusSessionDao().getAllSessionsFlow()
    val habits: Flow<List<HabitRecord>> = db.habitDao().getAllHabitsFlow()
    val wellnessLogs: Flow<List<WellnessLogRecord>> = db.wellnessLogDao().getAllWellnessLogsFlow()
    val blockedAttempts: Flow<List<BlockedAttemptRecord>> = db.blockedAttemptDao().getRecentAttemptsFlow()
    val appBlocks: Flow<List<AppBlockConfig>> = db.appBlockConfigDao().getBlockedAppsFlow()
    val globalSettings: Flow<GlobalWellnessSettings?> = db.globalSettingsDao().getSettingsFlow()

    fun getUsagesForDayFlow(dayTimestamp: Long): Flow<List<AppUsageRecord>> {
        return db.appUsageDao().getUsagesByDayFlow(dayTimestamp)
    }

    suspend fun insertUsages(usages: List<AppUsageRecord>) {
        db.appUsageDao().insertUsages(usages)
    }

    suspend fun insertSession(session: FocusSessionRecord) {
        db.focusSessionDao().insertSession(session)
    }

    suspend fun insertHabit(habit: HabitRecord) {
        db.habitDao().insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitRecord) {
        db.habitDao().updateHabit(habit)
    }

    suspend fun deleteHabit(id: Int) {
        db.habitDao().deleteHabit(id)
    }

    suspend fun insertWellnessLog(log: WellnessLogRecord) {
        db.wellnessLogDao().insertWellnessLog(log)
    }

    suspend fun insertBlockedAttempt(attempt: BlockedAttemptRecord) {
        db.blockedAttemptDao().insertAttempt(attempt)
    }

    suspend fun clearBlockedAttempts() {
        db.blockedAttemptDao().clearAttempts()
    }

    suspend fun updateAppBlockConfig(config: AppBlockConfig) {
        db.appBlockConfigDao().updateBlockConfig(config)
    }

    suspend fun insertBlockConfigs(configs: List<AppBlockConfig>) {
        db.appBlockConfigDao().insertBlockConfigs(configs)
    }

    suspend fun saveGlobalSettings(settings: GlobalWellnessSettings) {
        db.globalSettingsDao().saveSettings(settings)
    }

    suspend fun getSettingsSync(): GlobalWellnessSettings? {
        return db.globalSettingsDao().getSettingsSync()
    }

    suspend fun getBlockedAppsSync(): List<AppBlockConfig> {
        return db.appBlockConfigDao().getBlockedAppsSync()
    }

    // Automatically populates the database with realistic wellbeing, habits, and app usage records
    // if the database is initially empty, to showcase beautiful analytics dashboards immediately.
    suspend fun checkAndPrepopulate() {
        val settings = db.globalSettingsDao().getSettingsSync()
        if (settings != null) return

        // Save default initial settings
        val initialSettings = GlobalWellnessSettings(
            id = 1,
            currentTheme = "Mint",
            isAmoledBlack = false,
            totalPickupsToday = 34,
            totalUnlocksToday = 29,
            lockscreenQuoteIndex = 0,
            isFreezeActive = false,
            isHardcoreModeActive = false,
            isInterruptionQuestionsActive = true,
            lastRealityCheckTimestamp = System.currentTimeMillis()
        )
        db.globalSettingsDao().saveSettings(initialSettings)

        // Seed Default App Blocking Options
        val blockConfigs = listOf(
            AppBlockConfig("com.instagram.android", "Instagram", isBlocked = true, dailyLimitMinutes = 30, launchCooldownSeconds = 45, currentLaunchesToday = 14),
            AppBlockConfig("com.zhiliaoapp.musically", "TikTok", isBlocked = true, dailyLimitMinutes = 15, launchCooldownSeconds = 60, currentLaunchesToday = 8),
            AppBlockConfig("com.facebook.katana", "Facebook", isBlocked = false, dailyLimitMinutes = 60, launchCooldownSeconds = 0, currentLaunchesToday = 4),
            AppBlockConfig("com.twitter.android", "X / Twitter", isBlocked = true, dailyLimitMinutes = 20, launchCooldownSeconds = 30, currentLaunchesToday = 9),
            AppBlockConfig("com.tencent.ig", "PUBG Mobile", isBlocked = true, dailyLimitMinutes = 45, launchCooldownSeconds = 120, currentLaunchesToday = 1),
            AppBlockConfig("com.netease.g95", "Subway Surfers", isBlocked = false, dailyLimitMinutes = -1, launchCooldownSeconds = 0, currentLaunchesToday = 2),
            AppBlockConfig("com.notion.com", "Notion", isBlocked = false, dailyLimitMinutes = -1, launchCooldownSeconds = 0, currentLaunchesToday = 0),
            AppBlockConfig("com.slack", "Slack", isBlocked = false, dailyLimitMinutes = -1, launchCooldownSeconds = 0, currentLaunchesToday = 0)
        )
        db.appBlockConfigDao().insertBlockConfigs(blockConfigs)

        // Seed Habit Rules
        val habitsList = listOf(
            HabitRecord(name = "Mindful eye rest (20-20-20 rule)", targetFrequency = "Daily", category = "Eye Rest", currentStreak = 4, maxStreak = 12, totalCompletions = 15, lastCompletedTimestamp = System.currentTimeMillis() - 4000000),
            HabitRecord(name = "Hydration alert check-in", targetFrequency = "Daily", category = "Hydration", currentStreak = 2, maxStreak = 5, totalCompletions = 8, lastCompletedTimestamp = System.currentTimeMillis() - 8000000),
            HabitRecord(name = "Infinite Scroll Detox check", targetFrequency = "Daily", category = "Mindfulness", currentStreak = 6, maxStreak = 6, totalCompletions = 6, lastCompletedTimestamp = System.currentTimeMillis() - 12000000),
            HabitRecord(name = "3-min breathing meditation", targetFrequency = "Daily", category = "Mindfulness", currentStreak = 0, maxStreak = 3, totalCompletions = 3, lastCompletedTimestamp = 0L)
        )
        for (habit in habitsList) {
            db.habitDao().insertHabit(habit)
        }

        // Seed historical and current usage tracking records over the last 5 days
        val appPackages = listOf(
            Triple("com.instagram.android", "Instagram", "Social"),
            Triple("com.zhiliaoapp.musically", "TikTok", "Social"),
            Triple("com.facebook.katana", "Facebook", "Social"),
            Triple("com.twitter.android", "X / Twitter", "Social"),
            Triple("com.tencent.ig", "PUBG Mobile", "Gaming"),
            Triple("com.netease.g95", "Subway Surfers", "Gaming"),
            Triple("com.notion.com", "Notion", "Productivity"),
            Triple("com.slack", "Slack", "Productivity"),
            Triple("com.google.android.apps.docs", "Google Docs", "Productivity"),
            Triple("com.stopdumb.app", "StopDumb", "Productivity")
        )

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayMidnight = cal.timeInMillis

        val seededList = mutableListOf<AppUsageRecord>()

        // 5 Days of historic data
        for (dayOffset in 0..4) {
            val dayTimestamp = todayMidnight - (dayOffset * 24 * 60 * 60 * 1000L)
            
            // Random-looking but structured usage per category
            for (p in appPackages) {
                val usageMultiplier = when(dayOffset) {
                    0 -> 0.9f // today
                    1 -> 1.4f // yesterday (more scroll heavy)
                    2 -> 1.1f
                    3 -> 0.7f // productive day!
                    4 -> 1.3f
                    else -> 1.0f
                }

                val customDuration = when (p.third) {
                    "Social" -> {
                        if (p.second == "Instagram") (55 * 60 * 1000L * usageMultiplier).toLong()
                        else if (p.second == "TikTok") (40 * 60 * 1000L * usageMultiplier).toLong()
                        else if (p.second == "Facebook") (25 * 60 * 1000L * usageMultiplier).toLong()
                        else (15 * 60 * 1000L * usageMultiplier).toLong()
                    }
                    "Gaming" -> {
                        if (p.second == "PUBG Mobile") (35 * 60 * 1000L * usageMultiplier).toLong()
                        else (12 * 60 * 1000L * usageMultiplier).toLong()
                    }
                    else -> { // Productivity
                        if (p.second == "Notion") (28 * 60 * 1000L * usageMultiplier).toLong()
                        else if (p.second == "Slack") (40 * 60 * 1000L * usageMultiplier).toLong()
                        else if (p.second == "Google Docs") (20 * 60 * 1000L * usageMultiplier).toLong()
                        else (32 * 60 * 1000L * usageMultiplier).toLong() // StopDumb app
                    }
                }

                val customCount = when (p.third) {
                    "Social" -> (22 * usageMultiplier).toInt()
                    "Gaming" -> (4 * usageMultiplier).toInt()
                    else -> (12 * usageMultiplier).toInt()
                }

                seededList.add(
                    AppUsageRecord(
                        packageName = p.first,
                        appName = p.second,
                        category = p.third,
                        durationMs = customDuration,
                        launchCount = customCount,
                        dayTimestamp = dayTimestamp
                    )
                )
            }
        }
        db.appUsageDao().insertUsages(seededList)

        // Seed Focus Sessions
        val sessionsList = listOf(
            FocusSessionRecord(category = "Deep Work", durationMinutes = 45, completed = true, rating = 5, timestamp = todayMidnight - 12 * 3600 * 1000L),
            FocusSessionRecord(category = "Pomodoro", durationMinutes = 25, completed = true, rating = 4, timestamp = todayMidnight - 20 * 3600 * 1000L),
            FocusSessionRecord(category = "Study Planner", durationMinutes = 30, completed = false, rating = 1, timestamp = todayMidnight - 36 * 3600 * 1000L),
            FocusSessionRecord(category = "Pomodoro", durationMinutes = 25, completed = true, rating = 4, timestamp = todayMidnight - 48 * 3600 * 1000L),
            FocusSessionRecord(category = "Deep Work", durationMinutes = 60, completed = true, rating = 5, timestamp = todayMidnight - 60 * 3600 * 1000L)
        )
        for (session in sessionsList) {
            db.focusSessionDao().insertSession(session)
        }

        // Seed Wellness logs
        val wellnessList = listOf(
            WellnessLogRecord(moodValue = 4, energyLevel = 4, sleepHours = 7.5f, logNote = "Good session, felt highly focused after eye rest sessions.", timestamp = todayMidnight - 10 * 3600 * 1000L),
            WellnessLogRecord(moodValue = 3, energyLevel = 2, sleepHours = 6.2f, logNote = "Felt tired and doomscrolled TikTok for 45 minutes straight.", timestamp = todayMidnight - 34 * 3600 * 1000L),
            WellnessLogRecord(moodValue = 5, energyLevel = 5, sleepHours = 8.0f, logNote = "Amazing productivity metrics! Deep work mode helped.", timestamp = todayMidnight - 58 * 3600 * 1000L)
        )
        for (log in wellnessList) {
            db.wellnessLogDao().insertWellnessLog(log)
        }

        // Seed block attempts
        val blockLogs = listOf(
            BlockedAttemptRecord(appName = "TikTok", packageName = "com.zhiliaoapp.musically", blockedReason = "Daily Limit Reached", timestamp = System.currentTimeMillis() - 7200 * 1000),
            BlockedAttemptRecord(appName = "Instagram", packageName = "com.instagram.android", blockedReason = "App Launch Cooldown Active", timestamp = System.currentTimeMillis() - 14400 * 1000),
            BlockedAttemptRecord(appName = "PUBG Mobile", packageName = "com.tencent.ig", blockedReason = "Freeze Mode Active", timestamp = System.currentTimeMillis() - 18000 * 1000),
            BlockedAttemptRecord(appName = "Instagram", packageName = "com.instagram.android", blockedReason = "Daily Limit Reached", timestamp = System.currentTimeMillis() - 25000 * 1000)
        )
        for (blog in blockLogs) {
            db.blockedAttemptDao().insertAttempt(blog)
        }
    }
}
