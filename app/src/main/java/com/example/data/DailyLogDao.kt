package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY dateEpochDay DESC")
    fun getAllDailyLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs ORDER BY dateEpochDay DESC")
    suspend fun getAllDailyLogsSync(): List<DailyLogEntity>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    fun getDailyLogByDate(dateEpochDay: Long): Flow<DailyLogEntity?>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getDailyLogByDateSync(dateEpochDay: Long): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE uuid = :uuid LIMIT 1")
    suspend fun getDailyLogByUuid(uuid: String): DailyLogEntity?

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    fun getDailyLogsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY dateEpochDay ASC")
    suspend fun getDailyLogsInRangeSync(startEpochDay: Long, endEpochDay: Long): List<DailyLogEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDailyLog(dailyLog: DailyLogEntity): Long

    @Update
    suspend fun updateDailyLog(dailyLog: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteDailyLogById(id: Long)

    @Query("DELETE FROM daily_logs WHERE uuid = :uuid")
    suspend fun deleteDailyLogByUuid(uuid: String)
}
