package com.example.data

import kotlinx.coroutines.flow.Flow

class CycleRepository(
    private val cycleDao: CycleDao,
    private val dailyLogDao: DailyLogDao
) {
    // Cycles
    val allCycles: Flow<List<CycleEntity>> = cycleDao.getAllCycles()

    suspend fun getAllCyclesSync(): List<CycleEntity> = cycleDao.getAllCyclesSync()

    suspend fun getCycleById(id: Long): CycleEntity? = cycleDao.getCycleById(id)

    suspend fun getCycleByUuid(uuid: String): CycleEntity? = cycleDao.getCycleByUuid(uuid)

    suspend fun insertCycle(cycle: CycleEntity): Long = cycleDao.insertCycle(cycle)

    suspend fun updateCycle(cycle: CycleEntity) = cycleDao.updateCycle(cycle)

    suspend fun deleteCycle(id: Long) = cycleDao.deleteCycleById(id)

    suspend fun deleteCycleByUuid(uuid: String) = cycleDao.deleteCycleByUuid(uuid)

    // Daily Logs
    val allDailyLogs: Flow<List<DailyLogEntity>> = dailyLogDao.getAllDailyLogs()

    suspend fun getAllDailyLogsSync(): List<DailyLogEntity> = dailyLogDao.getAllDailyLogsSync()

    fun getDailyLogForDate(dateEpochDay: Long): Flow<DailyLogEntity?> = dailyLogDao.getDailyLogByDate(dateEpochDay)

    suspend fun getDailyLogForDateSync(dateEpochDay: Long): DailyLogEntity? = dailyLogDao.getDailyLogByDateSync(dateEpochDay)

    suspend fun insertDailyLog(log: DailyLogEntity): Long = dailyLogDao.insertDailyLog(log)

    suspend fun updateDailyLog(log: DailyLogEntity) = dailyLogDao.updateDailyLog(log)

    suspend fun deleteDailyLog(id: Long) = dailyLogDao.deleteDailyLogById(id)

    suspend fun deleteDailyLogByUuid(uuid: String) = dailyLogDao.deleteDailyLogByUuid(uuid)
}
