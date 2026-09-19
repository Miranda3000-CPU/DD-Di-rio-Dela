package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycle_records ORDER BY startDateEpochDay DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycle_records ORDER BY startDateEpochDay DESC")
    suspend fun getAllCyclesSync(): List<CycleEntity>

    @Query("SELECT * FROM cycle_records WHERE id = :id LIMIT 1")
    suspend fun getCycleById(id: Long): CycleEntity?

    @Query("SELECT * FROM cycle_records WHERE uuid = :uuid LIMIT 1")
    suspend fun getCycleByUuid(uuid: String): CycleEntity?

    @Query("SELECT * FROM cycle_records WHERE startDateEpochDay = :epochDay LIMIT 1")
    suspend fun getCycleByStartDate(epochDay: Long): CycleEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: CycleEntity)

    @Query("DELETE FROM cycle_records WHERE id = :id")
    suspend fun deleteCycleById(id: Long)

    @Query("DELETE FROM cycle_records WHERE uuid = :uuid")
    suspend fun deleteCycleByUuid(uuid: String)
}
