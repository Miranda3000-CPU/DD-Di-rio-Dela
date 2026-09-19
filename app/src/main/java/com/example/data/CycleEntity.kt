package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "cycle_records",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["startDateEpochDay"])
    ]
)
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val startDateEpochDay: Long,
    val periodLengthDays: Int = 5,
    val flowIntensity: String = "MÉDIO",
    val symptoms: String = "",
    val notes: String = "",
    val createdAtEpochMilli: Long = System.currentTimeMillis(),
    val updatedAtEpochMilli: Long = System.currentTimeMillis()
)
