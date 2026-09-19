package com.example.model

import java.time.LocalDate

enum class CyclePhase(val displayName: String, val description: String) {
    MENSTRUAL("Menstruação", "Fase de sangramento menstrual"),
    FOLLICULAR("Fase Folicular", "Fase de maturação folicular"),
    FERTILE("Janela Fértil Estimada", "Dias com probabilidade de concepção"),
    OVULATION("Ovulação Estimada", "Dia estimado da liberação do óvulo"),
    LUTEAL("Fase Lútea", "Fase pós-ovulatória pré-menstrual")
}

data class ProjectedCycle(
    val cycleIndex: Int, // 1 for next cycle, 2 for cycle after, etc.
    val estimatedStartDate: LocalDate,
    val windowStart: LocalDate,
    val windowEnd: LocalDate,
    val uncertaintyDays: Int, // ± days
    val estimatedPeriodEndDate: LocalDate,
    val estimatedOvulationDate: LocalDate,
    val fertileStart: LocalDate,
    val fertileEnd: LocalDate
)

data class HealthAttentionSignal(
    val title: String,
    val message: String
)

data class CyclePrediction(
    val lastPeriodStart: LocalDate? = null,
    val averageCycleDays: Int = 28,
    val averagePeriodLengthDays: Int = 5,
    val adaptiveCycleDays: Int = 28,
    val uncertaintyDays: Int = 2,
    val nextPeriodStart: LocalDate? = null,
    val nextPeriodWindowStart: LocalDate? = null,
    val nextPeriodWindowEnd: LocalDate? = null,
    val ovulationDate: LocalDate? = null,
    val fertileStart: LocalDate? = null,
    val fertileEnd: LocalDate? = null,
    val currentPhase: CyclePhase? = null,
    val currentCycleDay: Int? = null,
    val daysUntilNextPeriod: Int? = null,
    val isPeriodOverdue: Boolean = false,
    val totalLoggedCycles: Int = 0,
    val dataConfidenceText: String = "Sem dados registrados",
    val projectedCycles90Days: List<ProjectedCycle> = emptyList(),
    val healthAttentionSignals: List<HealthAttentionSignal> = emptyList(),
    // Backward compatibility fields for legacy UI references
    val mlPredictedCycleDays: Int = adaptiveCycleDays,
    val mlConfidencePercent: Int = 85,
    val mlNextPeriodStart: LocalDate? = nextPeriodStart
)
