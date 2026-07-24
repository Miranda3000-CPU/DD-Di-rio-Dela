package com.example.data

import kotlinx.coroutines.flow.Flow

class CycleRepository(private val dao: CycleDao) {

    val allCycles: Flow<List<CycleEntity>> = dao.getAllCycles()

    suspend fun insertCycle(cycle: CycleEntity): Long {
        return dao.insertCycle(cycle)
    }

    suspend fun deleteCycle(id: Long) {
        dao.deleteCycleById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
