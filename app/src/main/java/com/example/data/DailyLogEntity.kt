package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "daily_logs",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["dateEpochDay"], unique = true)
    ]
)
data class DailyLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val dateEpochDay: Long,
    val flowIntensity: String? = null,
    val painLevel: String? = null,
    val symptoms: String = "",
    val mood: String? = null,
    val energyLevel: String? = null,
    val sleepQuality: String? = null,
    val cervicalMucus: String? = null,
    val notes: String = "",
    val createdAtEpochMilli: Long = System.currentTimeMillis(),
    val updatedAtEpochMilli: Long = System.currentTimeMillis()
)
