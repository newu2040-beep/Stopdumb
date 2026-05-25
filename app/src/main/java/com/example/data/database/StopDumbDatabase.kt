package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Query("SELECT * FROM app_usages ORDER BY durationMs DESC")
    fun getAllUsagesFlow(): Flow<List<AppUsageRecord>>

    @Query("SELECT * FROM app_usages WHERE dayTimestamp = :dayTimestamp ORDER BY durationMs DESC")
    fun getUsagesByDayFlow(dayTimestamp: Long): Flow<List<AppUsageRecord>>

    @Query("SELECT * FROM app_usages WHERE dayTimestamp = :dayTimestamp")
    suspend fun getUsagesByDaySync(dayTimestamp: Long): List<AppUsageRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsages(usages: List<AppUsageRecord>)

    @Query("DELETE FROM app_usages")
    suspend fun clearAllUsages()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<FocusSessionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionRecord)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY lastCompletedTimestamp DESC")
    fun getAllHabitsFlow(): Flow<List<HabitRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitRecord)

    @Update
    suspend fun updateHabit(habit: HabitRecord)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabit(id: Int)
}

@Dao
interface WellnessLogDao {
    @Query("SELECT * FROM wellness_logs ORDER BY timestamp DESC")
    fun getAllWellnessLogsFlow(): Flow<List<WellnessLogRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWellnessLog(log: WellnessLogRecord)
}

@Dao
interface BlockedAttemptDao {
    @Query("SELECT * FROM blocked_attempts ORDER BY timestamp DESC LIMIT 50")
    fun getRecentAttemptsFlow(): Flow<List<BlockedAttemptRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: BlockedAttemptRecord)

    @Query("DELETE FROM blocked_attempts")
    suspend fun clearAttempts()
}

@Dao
interface AppBlockConfigDao {
    @Query("SELECT * FROM app_blocks")
    fun getBlockedAppsFlow(): Flow<List<AppBlockConfig>>

    @Query("SELECT * FROM app_blocks")
    suspend fun getBlockedAppsSync(): List<AppBlockConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockConfigs(configs: List<AppBlockConfig>)

    @Update
    suspend fun updateBlockConfig(config: AppBlockConfig)
}

@Dao
interface GlobalSettingsDao {
    @Query("SELECT * FROM global_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<GlobalWellnessSettings?>

    @Query("SELECT * FROM global_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): GlobalWellnessSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GlobalWellnessSettings)
}

@Database(
    entities = [
        AppUsageRecord::class,
        FocusSessionRecord::class,
        HabitRecord::class,
        WellnessLogRecord::class,
        BlockedAttemptRecord::class,
        AppBlockConfig::class,
        GlobalWellnessSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StopDumbDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun habitDao(): HabitDao
    abstract fun wellnessLogDao(): WellnessLogDao
    abstract fun blockedAttemptDao(): BlockedAttemptDao
    abstract fun appBlockConfigDao(): AppBlockConfigDao
    abstract fun globalSettingsDao(): GlobalSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: StopDumbDatabase? = null

        fun getDatabase(context: android.content.Context): StopDumbDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StopDumbDatabase::class.java,
                    "stopdumb_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
