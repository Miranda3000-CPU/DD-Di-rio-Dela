package com.example.model

import java.time.LocalDate

enum class CyclePhase(val displayName: String, val description: String) {
    MENSTRUAL("Menstruação", "Fase de sangramento menstrual"),
    FOLLICULAR("Fase Folicular", "Fase de maturação dos folículos"),
    FERTILE("Período Fértil", "Período com maior probabilidade de gravidez"),
    OVULATION("Dia da Ovulação", "Dia estimado da liberação do óvulo"),
    LUTEAL("Fase Lútea", "Fase pré-menstrual pós-ovulação")
}

data class CyclePrediction(
    val lastPeriodStart: LocalDate? = null,
    val averageCycleDays: Int = 28,
    val averagePeriodLengthDays: Int = 5,
    val mlPredictedCycleDays: Int = 28,
    val mlConfidencePercent: Int = 85,
    val nextPeriodStart: LocalDate? = null,
    val mlNextPeriodStart: LocalDate? = null,
    val ovulationDate: LocalDate? = null,
    val fertileStart: LocalDate? = null,
    val fertileEnd: LocalDate? = null,
    val currentPhase: CyclePhase? = null,
    val daysUntilNextPeriod: Int? = null,
    val isPeriodOverdue: Boolean = false,
    val totalLoggedCycles: Int = 0
)
