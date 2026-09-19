package com.example.util

import com.example.data.CycleEntity
import com.example.model.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CycleCalculatorTest {

    private val fixedToday = LocalDate.of(2026, 9, 19)

    @Test
    fun testZeroCycles() {
        val result = CycleCalculator.calculatePrediction(emptyList(), fixedToday)
        assertEquals(0, result.totalLoggedCycles)
        assertEquals(28, result.averageCycleDays)
        assertEquals(5, result.averagePeriodLengthDays)
        assertNull(result.lastPeriodStart)
        assertNull(result.nextPeriodStart)
        assertNull(result.currentPhase)
        assertTrue(result.projectedCycles90Days.isEmpty())
        assertTrue(result.healthAttentionSignals.isEmpty())
        assertEquals("Sem dados registrados ainda", result.dataConfidenceText)
    }

    @Test
    fun testSingleCycle() {
        // 1 cycle starting 10 days ago (2026-09-09)
        val cycle = CycleEntity(
            startDateEpochDay = fixedToday.minusDays(10).toEpochDay(),
            periodLengthDays = 4
        )
        val result = CycleCalculator.calculatePrediction(listOf(cycle), fixedToday)
        assertEquals(1, result.totalLoggedCycles)
        assertEquals(28, result.averageCycleDays)
        assertEquals(4, result.averagePeriodLengthDays)
        assertEquals(fixedToday.minusDays(10), result.lastPeriodStart)
        // Next period is 28 days after lastPeriodStart -> 18 days from fixedToday
        val expectedNext = fixedToday.minusDays(10).plusDays(28)
        assertEquals(expectedNext, result.nextPeriodStart)
        assertEquals(18, result.daysUntilNextPeriod)
        assertEquals(11, result.currentCycleDay)
        assertTrue(result.projectedCycles90Days.isNotEmpty())
    }

    @Test
    fun testRegularCycles() {
        // 4 consecutive cycles of exactly 28 days
        // Cycle 1: 84 days ago
        // Cycle 2: 56 days ago
        // Cycle 3: 28 days ago
        // Cycle 4: 0 days ago (today)
        val cycles = listOf(
            CycleEntity(startDateEpochDay = fixedToday.minusDays(84).toEpochDay(), periodLengthDays = 5),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(56).toEpochDay(), periodLengthDays = 5),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(28).toEpochDay(), periodLengthDays = 5),
            CycleEntity(startDateEpochDay = fixedToday.toEpochDay(), periodLengthDays = 5)
        )
        val result = CycleCalculator.calculatePrediction(cycles, fixedToday)
        assertEquals(4, result.totalLoggedCycles)
        assertEquals(28, result.averageCycleDays)
        assertEquals(28, result.adaptiveCycleDays)
        assertEquals(2, result.uncertaintyDays) // Very low dispersion -> uncertainty 2 days
        assertEquals(fixedToday.plusDays(28), result.nextPeriodStart)
        assertEquals(fixedToday.plusDays(26), result.nextPeriodWindowStart)
        assertEquals(fixedToday.plusDays(30), result.nextPeriodWindowEnd)
        assertEquals(CyclePhase.MENSTRUAL, result.currentPhase)
        assertEquals(1, result.currentCycleDay)
    }

    @Test
    fun testIrregularCyclesAndDispersion() {
        // Intervals: 24, 34, 26, 36 (highly variable)
        val day0 = fixedToday.minusDays(120)
        val day1 = day0.plusDays(24)
        val day2 = day1.plusDays(34)
        val day3 = day2.plusDays(26)
        val day4 = day3.plusDays(36)

        val cycles = listOf(
            CycleEntity(startDateEpochDay = day0.toEpochDay()),
            CycleEntity(startDateEpochDay = day1.toEpochDay()),
            CycleEntity(startDateEpochDay = day2.toEpochDay()),
            CycleEntity(startDateEpochDay = day3.toEpochDay()),
            CycleEntity(startDateEpochDay = day4.toEpochDay())
        )
        val result = CycleCalculator.calculatePrediction(cycles, fixedToday)
        assertEquals(5, result.totalLoggedCycles)
        // High variation leads to higher uncertainty days (>= 4 days)
        assertTrue("Incerteza deve ser de pelo menos 4 dias para ciclos irregulares", result.uncertaintyDays >= 4)
    }

    @Test
    fun testShortCyclesHealthSignal() {
        // Average cycle < 21 days (e.g. 19 days)
        val cycles = listOf(
            CycleEntity(startDateEpochDay = fixedToday.minusDays(57).toEpochDay()),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(38).toEpochDay()),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(19).toEpochDay())
        )
        val result = CycleCalculator.calculatePrediction(cycles, fixedToday)
        assertTrue(result.averageCycleDays < 21)
        val hasShortCycleAlert = result.healthAttentionSignals.any { it.title.contains("curtos", ignoreCase = true) }
        assertTrue("Deve emitir sinal de atenção para ciclos curtos", hasShortCycleAlert)
    }

    @Test
    fun testLongCyclesHealthSignal() {
        // Average cycle > 35 days (e.g. 38 days)
        val cycles = listOf(
            CycleEntity(startDateEpochDay = fixedToday.minusDays(76).toEpochDay()),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(38).toEpochDay())
        )
        val result = CycleCalculator.calculatePrediction(cycles, fixedToday)
        assertTrue(result.averageCycleDays > 35)
        val hasLongCycleAlert = result.healthAttentionSignals.any { it.title.contains("longos", ignoreCase = true) }
        assertTrue("Deve emitir sinal de atenção para ciclos longos", hasLongCycleAlert)
    }

    @Test
    fun testProlongedBleedingSignal() {
        // Period length > 7 days (e.g. 8 days)
        val cycles = listOf(
            CycleEntity(startDateEpochDay = fixedToday.minusDays(56).toEpochDay(), periodLengthDays = 9),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(28).toEpochDay(), periodLengthDays = 8)
        )
        val result = CycleCalculator.calculatePrediction(cycles, fixedToday)
        assertTrue(result.averagePeriodLengthDays > 7)
        val hasBleedingAlert = result.healthAttentionSignals.any { it.title.contains("prolongada", ignoreCase = true) }
        assertTrue("Deve emitir sinal de atenção para sangramento prolongado", hasBleedingAlert)
    }

    @Test
    fun testNinetyDayProjectionsAndGrowingUncertainty() {
        // 3 regular cycles of 28 days, latest cycle 14 days ago
        val cycles = listOf(
            CycleEntity(startDateEpochDay = fixedToday.minusDays(70).toEpochDay()),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(42).toEpochDay()),
            CycleEntity(startDateEpochDay = fixedToday.minusDays(14).toEpochDay())
        )
        val result = CycleCalculator.calculatePrediction(cycles, fixedToday)
        val projections = result.projectedCycles90Days

        // Within 90 days from today, with a 28-day cycle, we should have around 3 to 4 projected cycles
        assertTrue("Deve ter ao menos 3 projeções no horizonte de 90 dias", projections.size >= 3)

        // Verify uncertainty is strictly growing as we look further into the future
        for (i in 1 until projections.size) {
            assertTrue(
                "A incerteza deve aumentar ou se manter no horizonte futuro",
                projections[i].uncertaintyDays >= projections[i - 1].uncertaintyDays
            )
            assertTrue(
                "A data estimada do ciclo seguinte deve ser posterior à anterior",
                projections[i].estimatedStartDate.isAfter(projections[i - 1].estimatedStartDate)
            )
        }
    }

    @Test
    fun testPhaseTransitions() {
        // Cycle starts 2026-09-01, period 5 days, cycle length 28 days -> next period 2026-09-29
        // Ovulation: 2026-09-29 - 14 days = 2026-09-15
        // Fertile start: 2026-09-10
        // Fertile end: 2026-09-16
        val cycle = CycleEntity(
            startDateEpochDay = LocalDate.of(2026, 9, 1).toEpochDay(),
            periodLengthDays = 5
        )

        // Day 2 (Sep 2): Menstrual
        val p1 = CycleCalculator.calculatePrediction(listOf(cycle), LocalDate.of(2026, 9, 2))
        assertEquals(CyclePhase.MENSTRUAL, p1.currentPhase)

        // Day 7 (Sep 7): Follicular
        val p2 = CycleCalculator.calculatePrediction(listOf(cycle), LocalDate.of(2026, 9, 7))
        assertEquals(CyclePhase.FOLLICULAR, p2.currentPhase)

        // Day 12 (Sep 12): Fertile
        val p3 = CycleCalculator.calculatePrediction(listOf(cycle), LocalDate.of(2026, 9, 12))
        assertEquals(CyclePhase.FERTILE, p3.currentPhase)

        // Day 15 (Sep 15): Ovulation
        val p4 = CycleCalculator.calculatePrediction(listOf(cycle), LocalDate.of(2026, 9, 15))
        assertEquals(CyclePhase.OVULATION, p4.currentPhase)

        // Day 20 (Sep 20): Luteal
        val p5 = CycleCalculator.calculatePrediction(listOf(cycle), LocalDate.of(2026, 9, 20))
        assertEquals(CyclePhase.LUTEAL, p5.currentPhase)
    }

    @Test
    fun testNewLoggedPeriodUpdatesPredictionImmediately() {
        // Initial state: cycle on 2026-08-01
        val initialCycle = CycleEntity(startDateEpochDay = LocalDate.of(2026, 8, 1).toEpochDay(), periodLengthDays = 5)
        val predBefore = CycleCalculator.calculatePrediction(listOf(initialCycle), fixedToday)
        val initialNext = predBefore.nextPeriodStart

        // User logs a new period on 2026-08-30 (29-day cycle)
        val newCycle = CycleEntity(startDateEpochDay = LocalDate.of(2026, 8, 30).toEpochDay(), periodLengthDays = 5)
        val predAfter = CycleCalculator.calculatePrediction(listOf(initialCycle, newCycle), fixedToday)

        // New next period must be recalculated based on 2026-08-30
        assertNotNull(predAfter.nextPeriodStart)
        assertTrue(predAfter.nextPeriodStart!!.isAfter(initialNext))
        assertEquals(2, predAfter.totalLoggedCycles)
        assertEquals(LocalDate.of(2026, 8, 30), predAfter.lastPeriodStart)
    }
}
