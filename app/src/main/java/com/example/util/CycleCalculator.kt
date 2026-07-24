package com.example.util

import com.example.data.CycleEntity
import com.example.model.CyclePhase
import com.example.model.CyclePrediction
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

object CycleCalculator {

    const val DEFAULT_CYCLE_LENGTH = 28
    const val DEFAULT_PERIOD_LENGTH = 5

    fun calculatePrediction(
        cycles: List<CycleEntity>,
        today: LocalDate = LocalDate.now()
    ): CyclePrediction {
        if (cycles.isEmpty()) {
            return CyclePrediction(
                averageCycleDays = DEFAULT_CYCLE_LENGTH,
                averagePeriodLengthDays = DEFAULT_PERIOD_LENGTH,
                totalLoggedCycles = 0
            )
        }

        val sortedCycles = cycles.sortedBy { it.startDateEpochDay }
        val totalLogged = sortedCycles.size

        // Calculate average period length
        val avgPeriodLength = sortedCycles
            .map { it.periodLengthDays }
            .average()
            .roundToInt()
            .coerceIn(2, 10)

        // Calculate average cycle length & ML EWMA prediction
        val (avgCycleDays, mlPredictedCycleDays, confidencePercent) = if (totalLogged >= 2) {
            val intervals = (1 until totalLogged).map { index ->
                (sortedCycles[index].startDateEpochDay - sortedCycles[index - 1].startDateEpochDay).toDouble()
            }
            val avg = intervals.average().roundToInt().coerceIn(21, 40)

            // EWMA (Exponentially Weighted Moving Average) giving recent cycles higher weight
            var ewma = intervals.first()
            val alpha = 0.65
            for (i in 1 until intervals.size) {
                ewma = alpha * intervals[i] + (1 - alpha) * ewma
            }
            val mlPred = ewma.roundToInt().coerceIn(21, 40)

            // Confidence calculation based on standard deviation of intervals
            val variance = intervals.map { (it - avg) * (it - avg) }.average()
            val stdDev = Math.sqrt(variance)
            val confidence = (100 - (stdDev * 5)).roundToInt().coerceIn(60, 98)

            Triple(avg, mlPred, confidence)
        } else {
            Triple(DEFAULT_CYCLE_LENGTH, DEFAULT_CYCLE_LENGTH, 80)
        }

        val lastEntity = sortedCycles.last()
        val lastPeriodStart = LocalDate.ofEpochDay(lastEntity.startDateEpochDay)
        val lastPeriodLength = lastEntity.periodLengthDays

        // Predict next period (using ML prediction as primary, avg as baseline)
        val nextPeriodStart = lastPeriodStart.plusDays(mlPredictedCycleDays.toLong())
        val mlNextPeriodStart = lastPeriodStart.plusDays(mlPredictedCycleDays.toLong())
        val ovulationDate = nextPeriodStart.minusDays(14)
        val fertileStart = ovulationDate.minusDays(5)
        val fertileEnd = ovulationDate.plusDays(1)

        val daysUntilNext = ChronoUnit.DAYS.between(today, nextPeriodStart).toInt()
        val isOverdue = today >= nextPeriodStart && ChronoUnit.DAYS.between(lastPeriodStart, today) >= lastPeriodLength

        val currentPhase = determinePhase(
            today = today,
            lastPeriodStart = lastPeriodStart,
            periodLength = lastPeriodLength,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            ovulationDate = ovulationDate,
            nextPeriodStart = nextPeriodStart
        )

        return CyclePrediction(
            lastPeriodStart = lastPeriodStart,
            averageCycleDays = avgCycleDays,
            averagePeriodLengthDays = avgPeriodLength,
            mlPredictedCycleDays = mlPredictedCycleDays,
            mlConfidencePercent = confidencePercent,
            nextPeriodStart = nextPeriodStart,
            mlNextPeriodStart = mlNextPeriodStart,
            ovulationDate = ovulationDate,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            currentPhase = currentPhase,
            daysUntilNextPeriod = daysUntilNext,
            isPeriodOverdue = isOverdue,
            totalLoggedCycles = totalLogged
        )
    }

    private fun determinePhase(
        today: LocalDate,
        lastPeriodStart: LocalDate,
        periodLength: Int,
        fertileStart: LocalDate,
        fertileEnd: LocalDate,
        ovulationDate: LocalDate,
        nextPeriodStart: LocalDate
    ): CyclePhase {
        val periodEnd = lastPeriodStart.plusDays((periodLength - 1).toLong())

        return when {
            !today.isBefore(lastPeriodStart) && !today.isAfter(periodEnd) -> CyclePhase.MENSTRUAL
            today == ovulationDate -> CyclePhase.OVULATION
            !today.isBefore(fertileStart) && !today.isAfter(fertileEnd) -> CyclePhase.FERTILE
            today.isAfter(periodEnd) && today.isBefore(fertileStart) -> CyclePhase.FOLLICULAR
            today.isAfter(fertileEnd) && today.isBefore(nextPeriodStart) -> CyclePhase.LUTEAL
            !today.isBefore(nextPeriodStart) -> CyclePhase.MENSTRUAL
            else -> CyclePhase.FOLLICULAR
        }
    }
}
