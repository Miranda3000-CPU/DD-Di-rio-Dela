package com.example.util

import com.example.data.CycleEntity
import com.example.model.CyclePhase
import com.example.model.CyclePrediction
import com.example.model.HealthAttentionSignal
import com.example.model.ProjectedCycle
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Motor de cálculo estatístico adaptativo e projeção do ciclo menstrual.
 *
 * Fundamentação científica e referências médicas:
 * 1. Li K, Urteaga I, Shea A, Vitzthum VJ, Wiggins CH, Elhadad N.
 *    "A predictive model for next cycle start date that accounts for adherence in menstrual self-tracking."
 *    JAMIA, 2021. PMID: 34534312. DOI: 10.1093/jamia/ocab182.
 * 2. American College of Obstetricians and Gynecologists (ACOG).
 *    "Menstruation in Girls and Adolescents: Using the Menstrual Cycle as a Vital Sign."
 *    (Ciclo típico: 21 a 35 dias; duração típica: 2 a 7 dias).
 * 3. "Characterizing physiological and symptomatic variation in menstrual cycles using self-tracked mobile-health data."
 *    PMID: 32509976.
 * 4. "Menstrual tracking technologies and fertility: evaluating accuracy, utility, and impact on time to pregnancy."
 *    Fertility and Sterility, 2026. PMID: 41747954.
 */
object CycleCalculator {

    const val DEFAULT_CYCLE_LENGTH = 28
    const val DEFAULT_PERIOD_LENGTH = 5
    const val PROJECTION_HORIZON_DAYS = 90L

    fun calculatePrediction(
        cycles: List<CycleEntity>,
        today: LocalDate = LocalDate.now()
    ): CyclePrediction {
        if (cycles.isEmpty()) {
            return CyclePrediction(
                averageCycleDays = DEFAULT_CYCLE_LENGTH,
                averagePeriodLengthDays = DEFAULT_PERIOD_LENGTH,
                adaptiveCycleDays = DEFAULT_CYCLE_LENGTH,
                uncertaintyDays = 3,
                totalLoggedCycles = 0,
                dataConfidenceText = "Sem dados registrados ainda",
                projectedCycles90Days = emptyList(),
                healthAttentionSignals = emptyList()
            )
        }

        val sortedCycles = cycles.sortedBy { it.startDateEpochDay }
        val totalLogged = sortedCycles.size

        // Duração média da menstruação
        val avgPeriodLength = sortedCycles
            .map { it.periodLengthDays }
            .average()
            .roundToInt()
            .coerceIn(2, 10)

        // Cálculo estatístico de intervalos entre ciclos consecutivos
        val (avgCycleDays, adaptiveCycleDays, uncertaintyDays, confidenceText) = if (totalLogged >= 2) {
            val intervals = (1 until totalLogged).map { index ->
                (sortedCycles[index].startDateEpochDay - sortedCycles[index - 1].startDateEpochDay).toDouble()
            }
            val avg = intervals.average().roundToInt().coerceIn(20, 45)

            // Mediana para mitigar impacto de atípicos
            val sortedIntervals = intervals.sorted()
            val median = if (sortedIntervals.size % 2 == 1) {
                sortedIntervals[sortedIntervals.size / 2]
            } else {
                (sortedIntervals[sortedIntervals.size / 2 - 1] + sortedIntervals[sortedIntervals.size / 2]) / 2.0
            }

            // EWMA (Exponentially Weighted Moving Average) ponderando mais os ciclos recentes (Li et al., 2021)
            var ewma = intervals.first()
            val alpha = 0.60
            for (i in 1 until intervals.size) {
                ewma = alpha * intervals[i] + (1 - alpha) * ewma
            }

            // Combinação robusta de EWMA e Mediana
            val adaptive = ((0.7 * ewma) + (0.3 * median)).roundToInt().coerceIn(20, 45)

            // Dispersão e desvio padrão
            val variance = intervals.map { (it - avg) * (it - avg) }.average()
            val stdDev = sqrt(variance)
            val uncertainty = when {
                totalLogged == 2 -> 3
                stdDev <= 1.5 -> 2
                stdDev <= 3.0 -> 3
                stdDev <= 5.0 -> 4
                else -> 5
            }

            val text = "Baseada em $totalLogged ciclos registrados (faixa histórica de ±$uncertainty dias)"
            Tuple4(avg, adaptive, uncertainty, text)
        } else {
            val text = "Baseada em 1 ciclo registrado (referência inicial de 28 dias)"
            Tuple4(DEFAULT_CYCLE_LENGTH, DEFAULT_CYCLE_LENGTH, 3, text)
        }

        val lastEntity = sortedCycles.last()
        val lastPeriodStart = LocalDate.ofEpochDay(lastEntity.startDateEpochDay)
        val lastPeriodLength = lastEntity.periodLengthDays

        // Estimativa da próxima menstruação (Ciclo 1)
        val nextPeriodStart = lastPeriodStart.plusDays(adaptiveCycleDays.toLong())
        val nextWindowStart = nextPeriodStart.minusDays(uncertaintyDays.toLong())
        val nextWindowEnd = nextPeriodStart.plusDays(uncertaintyDays.toLong())

        // Estimativa retrospectiva da ovulação e janela fértil
        // (A literatura médica indica fase lútea em torno de 14 dias antes do próximo ciclo)
        val ovulationDate = nextPeriodStart.minusDays(14)
        val fertileStart = ovulationDate.minusDays(5)
        val fertileEnd = ovulationDate.plusDays(1)

        val daysUntilNext = ChronoUnit.DAYS.between(today, nextPeriodStart).toInt()
        val isOverdue = today >= nextPeriodStart && ChronoUnit.DAYS.between(lastPeriodStart, today) >= lastPeriodLength

        val currentCycleDay = if (!today.isBefore(lastPeriodStart)) {
            ChronoUnit.DAYS.between(lastPeriodStart, today).toInt() + 1
        } else null

        val currentPhase = determinePhase(
            today = today,
            lastPeriodStart = lastPeriodStart,
            periodLength = lastPeriodLength,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            ovulationDate = ovulationDate,
            nextPeriodStart = nextPeriodStart
        )

        // Projeções para 90 dias com incerteza crescente
        val projections90Days = calculate90DayProjections(
            firstNextPeriodStart = nextPeriodStart,
            baseAdaptiveDays = adaptiveCycleDays,
            baseUncertainty = uncertaintyDays,
            avgPeriodLength = avgPeriodLength,
            today = today
        )

        // Sinais de atenção de saúde baseados em evidências clínicas (ACOG)
        val healthSignals = evaluateHealthAttentionSignals(
            sortedCycles = sortedCycles,
            avgCycleDays = avgCycleDays,
            avgPeriodLength = avgPeriodLength,
            today = today,
            nextPeriodStart = nextPeriodStart
        )

        return CyclePrediction(
            lastPeriodStart = lastPeriodStart,
            averageCycleDays = avgCycleDays,
            averagePeriodLengthDays = avgPeriodLength,
            adaptiveCycleDays = adaptiveCycleDays,
            uncertaintyDays = uncertaintyDays,
            nextPeriodStart = nextPeriodStart,
            nextPeriodWindowStart = nextWindowStart,
            nextPeriodWindowEnd = nextWindowEnd,
            ovulationDate = ovulationDate,
            fertileStart = fertileStart,
            fertileEnd = fertileEnd,
            currentPhase = currentPhase,
            currentCycleDay = currentCycleDay,
            daysUntilNextPeriod = daysUntilNext,
            isPeriodOverdue = isOverdue,
            totalLoggedCycles = totalLogged,
            dataConfidenceText = confidenceText,
            projectedCycles90Days = projections90Days,
            healthAttentionSignals = healthSignals
        )
    }

    private fun calculate90DayProjections(
        firstNextPeriodStart: LocalDate,
        baseAdaptiveDays: Int,
        baseUncertainty: Int,
        avgPeriodLength: Int,
        today: LocalDate
    ): List<ProjectedCycle> {
        val projections = mutableListOf<ProjectedCycle>()
        val maxHorizonDate = today.plusDays(PROJECTION_HORIZON_DAYS)

        var currentEstimatedStart = firstNextPeriodStart
        var cycleIndex = 1
        var uncertainty = baseUncertainty

        while (!currentEstimatedStart.isAfter(maxHorizonDate)) {
            val windowStart = currentEstimatedStart.minusDays(uncertainty.toLong())
            val windowEnd = currentEstimatedStart.plusDays(uncertainty.toLong())
            val estimatedPeriodEnd = currentEstimatedStart.plusDays((avgPeriodLength - 1).toLong())
            val estimatedOvulation = currentEstimatedStart.minusDays(14)
            val fertileStart = estimatedOvulation.minusDays(5)
            val fertileEnd = estimatedOvulation.plusDays(1)

            projections.add(
                ProjectedCycle(
                    cycleIndex = cycleIndex,
                    estimatedStartDate = currentEstimatedStart,
                    windowStart = windowStart,
                    windowEnd = windowEnd,
                    uncertaintyDays = uncertainty,
                    estimatedPeriodEndDate = estimatedPeriodEnd,
                    estimatedOvulationDate = estimatedOvulation,
                    fertileStart = fertileStart,
                    fertileEnd = fertileEnd
                )
            )

            // Avança para o ciclo seguinte
            currentEstimatedStart = currentEstimatedStart.plusDays(baseAdaptiveDays.toLong())
            cycleIndex++
            // A incerteza aumenta à medida que nos distanciamos no horizonte futuro
            uncertainty = (uncertainty + 2).coerceAtMost(9)
        }

        return projections
    }

    private fun evaluateHealthAttentionSignals(
        sortedCycles: List<CycleEntity>,
        avgCycleDays: Int,
        avgPeriodLength: Int,
        today: LocalDate,
        nextPeriodStart: LocalDate
    ): List<HealthAttentionSignal> {
        val signals = mutableListOf<HealthAttentionSignal>()

        if (sortedCycles.size >= 2) {
            // Ciclos persistentemente curtos (< 21 dias, segundo ACOG)
            if (avgCycleDays < 21) {
                signals.add(
                    HealthAttentionSignal(
                        title = "Ciclos mais curtos que o habitual",
                        message = "Seus ciclos recentes têm duração média de $avgCycleDays dias. Variações persistentes abaixo de 21 dias podem merecer atenção e uma conversa com um profissional de saúde."
                    )
                )
            }

            // Ciclos persistentemente longos (> 35 dias, segundo ACOG)
            if (avgCycleDays > 35) {
                signals.add(
                    HealthAttentionSignal(
                        title = "Ciclos mais longos que o habitual",
                        message = "Seus ciclos recentes têm duração média de $avgCycleDays dias. Intervalos frequentes acima de 35 dias são comuns, mas pode ser interessante conversar com seu médico para entender seus padrões."
                    )
                )
            }

            // Duração do fluxo menstrual prolongada (> 7 dias)
            if (avgPeriodLength > 7) {
                signals.add(
                    HealthAttentionSignal(
                        title = "Duração de menstruação prolongada",
                        message = "A duração média registrada da sua menstruação é de $avgPeriodLength dias. Sangramentos por mais de 7 dias podem merecer avaliação clínica."
                    )
                )
            }

            // Atraso menstrual relevante (> 10 dias após a estimativa)
            val daysOverdue = ChronoUnit.DAYS.between(nextPeriodStart, today)
            if (daysOverdue >= 10) {
                signals.add(
                    HealthAttentionSignal(
                        title = "Variação na data esperada",
                        message = "Sua menstruação está cerca de $daysOverdue dias além do estimado. Alterações temporárias podem decorrer de estresse, rotina ou sono. Se a ausência persistir, procure orientação profissional."
                    )
                )
            }
        }

        return signals
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

    private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
