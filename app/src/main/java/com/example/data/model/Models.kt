package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usages")
data class AppUsageRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appName: String,
    val category: String, // "Social", "Gaming", "Productivity", "Entertainment", "Utility"
    val durationMs: Long,
    val launchCount: Int,
    val dayTimestamp: Long // Midnight timestamp of the recorded day
)

@Entity(tableName = "focus_sessions")
data class FocusSessionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "Pomodoro", "Deep Work", "Study Planner", "Digital Detox"
    val durationMinutes: Int,
    val completed: Boolean,
    val rating: Int, // 1-5 Focus Score
    val timestamp: Long
)

@Entity(tableName = "habits")
data class HabitRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetFrequency: String, // "Daily", "Weekly"
    val category: String, // "Hydration", "Eye Rest", "Mindfulness", "Physical Break"
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastCompletedTimestamp: Long = 0L,
    val totalCompletions: Int = 0
)

@Entity(tableName = "wellness_logs")
data class WellnessLogRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moodValue: Int, // 1 = Awful, 5 = Excellent
    val energyLevel: Int, // 1 = Exhausted, 5 = Highly Productive
    val sleepHours: Float,
    val logNote: String,
    val timestamp: Long
)

@Entity(tableName = "blocked_attempts")
data class BlockedAttemptRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val blockedReason: String, // "Daily Limit Reached", "Freeze Mode Active", "Focus Hour Block"
    val timestamp: Long
)

@Entity(tableName = "app_blocks")
data class AppBlockConfig(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean,
    val dailyLimitMinutes: Int = -1, // -1 means no limit
    val launchCooldownSeconds: Int = 0, // cooldown timer between app openings
    val currentLaunchesToday: Int = 0
)

@Entity(tableName = "global_settings")
data class GlobalWellnessSettings(
    @PrimaryKey val id: Int = 1,
    val currentTheme: String = "Mint", // Pastel colors requested
    val isAmoledBlack: Boolean = false,
    val totalPickupsToday: Int = 0,
    val totalUnlocksToday: Int = 0,
    val lockscreenQuoteIndex: Int = 0,
    val isFreezeActive: Boolean = false,
    val isHardcoreModeActive: Boolean = false,
    val sleepTimeStartHour: Int = 22, // 10 PM
    val sleepTimeStartMin: Int = 0,
    val sleepTimeEndHour: Int = 6, // 6 AM
    val sleepTimeEndMin: Int = 0,
    val isInterruptionQuestionsActive: Boolean = true,
    val lastRealityCheckTimestamp: Long = 0L,
    val offlineFocusSuggestionsLevel: String = "Standard"
)
