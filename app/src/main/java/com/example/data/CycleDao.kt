package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycle_records ORDER BY startDateEpochDay DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: CycleEntity): Long

    @Query("DELETE FROM cycle_records WHERE id = :id")
    suspend fun deleteCycleById(id: Long)

    @Query("DELETE FROM cycle_records")
    suspend fun clearAll()
}
