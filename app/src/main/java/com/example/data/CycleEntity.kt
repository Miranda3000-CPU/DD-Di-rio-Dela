package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_records")
data class CycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDateEpochDay: Long,
    val periodLengthDays: Int = 5,
    val flowIntensity: String = "MÉDIO",
    val symptoms: String = "",
    val notes: String = ""
)
