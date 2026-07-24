package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CycleEntity
import com.example.model.CyclePrediction
import com.example.ui.theme.FertileBluePrimary
import com.example.ui.theme.OvulationPurplePrimary
import com.example.ui.theme.PeriodRosePrimary
import com.example.ui.theme.PeriodRoseSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun VisualCalendar(
    cycles: List<CycleEntity>,
    prediction: CyclePrediction,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onLogDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var showTourDialog by remember { mutableStateOf(false) }

    val PortugueseLocale = Locale("pt", "BR")

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfMonth = currentYearMonth.atDay(1)
    val firstDayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7

    val weekDays = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Month Header Navigation & Tour Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Mês anterior"
                    )
                }

                val monthYearText = currentYearMonth.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", PortugueseLocale)
                ).replaceFirstChar { if (it.isLowerCase()) it.titlecase(PortugueseLocale) else it.toString() }

                Text(
                    text = monthYearText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    IconButton(onClick = { showTourDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Guia de Cores / Tour",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Próximo mês"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Labels
            Row(modifier = Modifier.fillMaxWidth()) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Grid
            val totalCells = firstDayOfWeekOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until totalRows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOfWeekOffset + 1

                            if (cellIndex in firstDayOfWeekOffset until (firstDayOfWeekOffset + daysInMonth)) {
                                val date = currentYearMonth.atDay(dayNumber)
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()

                                val dayInfo = getDayPhaseInfo(date, cycles, prediction)

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    dayInfo = dayInfo,
                                    onClick = { onDateSelected(date) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend Row
            CalendarLegendRow()

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day Info & Action Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateFormatted = selectedDate.format(
                        DateTimeFormatter.ofPattern("dd 'de' MMMM", PortugueseLocale)
                    )
                    val selectedInfo = getDayPhaseInfo(selectedDate, cycles, prediction)

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dateFormatted,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = selectedInfo.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = selectedInfo.color
                        )
                    }

                    OutlinedButton(
                        onClick = { onLogDateClick(selectedDate) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showTourDialog) {
        AlertDialog(
            onDismissRequest = { showTourDialog = false },
            title = { Text("Guia do Calendário", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Conheça o significado das cores do seu calendário:")
                    TourItem(color = PeriodRosePrimary, title = "Menstruação Registrada", desc = "Dias de sangramento informados por você.")
                    TourItem(color = FertileBluePrimary, title = "Período Fértil", desc = "Janela com maior probabilidade de concepção.")
                    TourItem(color = OvulationPurplePrimary, title = "Dia da Ovulação", desc = "Dia estimado de liberação do óvulo.")
                    TourItem(color = PeriodRoseSecondary, title = "Previsão de Menstruação", desc = "Estimativa do próximo ciclo calculada pelo modelo.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showTourDialog = false }) {
                    Text("Entendi", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun TourItem(color: Color, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class DayPhaseInfo(
    val label: String,
    val color: Color,
    val isPeriod: Boolean = false,
    val isFertile: Boolean = false,
    val isOvulation: Boolean = false,
    val isPredictedPeriod: Boolean = false
)

private fun getDayPhaseInfo(
    date: LocalDate,
    cycles: List<CycleEntity>,
    prediction: CyclePrediction
): DayPhaseInfo {
    // 1. Check logged period records
    cycles.forEach { cycle ->
        val start = LocalDate.ofEpochDay(cycle.startDateEpochDay)
        val end = start.plusDays((cycle.periodLengthDays - 1).toLong())
        if (!date.isBefore(start) && !date.isAfter(end)) {
            return DayPhaseInfo("Menstruação Registrada", PeriodRosePrimary, isPeriod = true)
        }
    }

    // 2. Check predicted ovulation
    if (prediction.ovulationDate == date) {
        return DayPhaseInfo("Dia da Ovulação Estimado", OvulationPurplePrimary, isOvulation = true)
    }

    // 3. Check fertile window
    if (prediction.fertileStart != null && prediction.fertileEnd != null) {
        if (!date.isBefore(prediction.fertileStart) && !date.isAfter(prediction.fertileEnd)) {
            return DayPhaseInfo("Período Fértil Estimado", FertileBluePrimary, isFertile = true)
        }
    }

    // 4. Check predicted next period
    if (prediction.nextPeriodStart != null) {
        val predEnd = prediction.nextPeriodStart.plusDays((prediction.averagePeriodLengthDays - 1).toLong())
        if (!date.isBefore(prediction.nextPeriodStart) && !date.isAfter(predEnd)) {
            return DayPhaseInfo("Previsão de Menstruação", PeriodRoseSecondary, isPredictedPeriod = true)
        }
    }

    return DayPhaseInfo("Fase Normal do Ciclo", Color.Gray)
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    dayInfo: DayPhaseInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cellBackground = when {
        dayInfo.isPeriod -> PeriodRosePrimary
        dayInfo.isOvulation -> OvulationPurplePrimary
        dayInfo.isFertile -> FertileBluePrimary
        dayInfo.isPredictedPeriod -> PeriodRoseSecondary.copy(alpha = 0.4f)
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }

    val textColor = when {
        dayInfo.isPeriod || dayInfo.isOvulation || dayInfo.isFertile -> Color.White
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(cellBackground)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayNumber.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected || isToday || dayInfo.isPeriod) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun CalendarLegendRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = PeriodRosePrimary, label = "Menstruação")
        LegendDot(color = FertileBluePrimary, label = "Fértil")
        LegendDot(color = OvulationPurplePrimary, label = "Ovulação")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        ) { }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
