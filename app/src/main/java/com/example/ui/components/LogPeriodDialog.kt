package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.DailyLogEntity
import com.example.ui.theme.PeriodRoseContainer
import com.example.ui.theme.PeriodRosePrimary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogPeriodDialog(
    initialDate: LocalDate = LocalDate.now(),
    existingLog: DailyLogEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        startDate: LocalDate,
        isPeriodStart: Boolean,
        periodLengthDays: Int,
        flowIntensity: String,
        painLevel: String,
        symptoms: String,
        mood: String,
        energyLevel: String,
        sleepQuality: String,
        cervicalMucus: String,
        notes: String
    ) -> Unit
) {
    val portugueseLocale = Locale.forLanguageTag("pt-BR")
    val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", portugueseLocale)

    var selectedDate by remember { mutableStateOf(initialDate) }
    var showInlineCalendar by remember { mutableStateOf(false) }
    var calendarYearMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    var isPeriodStart by remember { mutableStateOf(true) }
    var periodLengthDays by remember { mutableIntStateOf(5) }
    var selectedFlow by remember { mutableStateOf(existingLog?.flowIntensity ?: "MÉDIO") }
    var selectedPain by remember { mutableStateOf(existingLog?.painLevel ?: "NENHUMA") }
    var selectedMood by remember { mutableStateOf(existingLog?.mood ?: "Tranquila") }
    var selectedEnergy by remember { mutableStateOf(existingLog?.energyLevel ?: "Média") }
    var selectedSleep by remember { mutableStateOf(existingLog?.sleepQuality ?: "Boa") }
    var selectedMucus by remember { mutableStateOf(existingLog?.cervicalMucus ?: "") }
    var notesText by remember { mutableStateOf(existingLog?.notes ?: "") }

    // Physical Symptoms
    val symptomOptions = listOf(
        "Cólica", "Dor de cabeça", "Sensibilidade mamária", "Inchaço",
        "Náusea", "Acne", "Cansaço", "Alteração intestinal", "Dor lombar", "Insônia"
    )
    val selectedSymptoms = remember {
        mutableStateListOf<String>().apply {
            if (!existingLog?.symptoms.isNullOrBlank()) {
                val list = existingLog!!.symptoms.split(",").map { it.trim() }
                addAll(list)
            }
        }
    }

    val flowOptions = listOf(
        Pair("SEM_FLUXO", "Sem sangramento"),
        Pair("LEVE", "💧 Leve"),
        Pair("MÉDIO", "💧💧 Médio"),
        Pair("INTENSO", "💧💧💧 Intenso"),
        Pair("ESCAPE", "🩸 Escape")
    )

    val painOptions = listOf("Nenhuma", "Leve", "Moderada", "Forte")
    val moodOptions = listOf("Tranquila", "Feliz", "Sensível", "Ansiosa", "Irritada", "Cansada", "Triste")
    val energyOptions = listOf("Baixa", "Média", "Alta")
    val sleepOptions = listOf("Ruim", "Regular", "Boa")
    val mucusOptions = listOf("Seco", "Pegajoso", "Cremoso", "Aquoso", "Clara de Ovo (Fértil)")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PeriodRoseContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = PeriodRosePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Registrar Dia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DD • Diário Dela",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .testTag("log_period_dialog_content"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Interactive Date Picker Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Data Selecionada",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            IconButton(
                                onClick = { showInlineCalendar = !showInlineCalendar },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Abrir calendário",
                                    tint = PeriodRosePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val formattedDate = selectedDate.format(fullDateFormatter)
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(portugueseLocale) else it.toString() }

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick date selection shortcuts
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilterChip(
                                selected = selectedDate == LocalDate.now(),
                                onClick = {
                                    selectedDate = LocalDate.now()
                                    calendarYearMonth = YearMonth.from(selectedDate)
                                },
                                label = { Text("Hoje", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = selectedDate == LocalDate.now().minusDays(1),
                                onClick = {
                                    selectedDate = LocalDate.now().minusDays(1)
                                    calendarYearMonth = YearMonth.from(selectedDate)
                                },
                                label = { Text("Ontem", fontSize = 12.sp) }
                            )
                            FilterChip(
                                selected = selectedDate == LocalDate.now().minusDays(2),
                                onClick = {
                                    selectedDate = LocalDate.now().minusDays(2)
                                    calendarYearMonth = YearMonth.from(selectedDate)
                                },
                                label = { Text("2 dias atrás", fontSize = 12.sp) }
                            )
                        }

                        // Inline Calendar Grid (Expandable)
                        AnimatedVisibility(
                            visible = showInlineCalendar,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { calendarYearMonth = calendarYearMonth.minusMonths(1) }) {
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
                                    }

                                    val monthYearName = calendarYearMonth.month
                                        .getDisplayName(TextStyle.FULL, portugueseLocale)
                                        .replaceFirstChar { it.uppercase() } + " ${calendarYearMonth.year}"

                                    Text(
                                        text = monthYearName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(onClick = { calendarYearMonth = calendarYearMonth.plusMonths(1) }) {
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
                                    }
                                }

                                val weekDays = listOf("D", "S", "T", "Q", "Q", "S", "S")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    weekDays.forEach { day ->
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.width(32.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val firstDay = calendarYearMonth.atDay(1)
                                val daysInMonth = calendarYearMonth.lengthOfMonth()
                                val dayOffset = firstDay.dayOfWeek.value % 7

                                var dayCounter = 1
                                val totalCells = ((daysInMonth + dayOffset + 6) / 7) * 7

                                for (row in 0 until (totalCells / 7)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        for (col in 0 until 7) {
                                            val cellIndex = row * 7 + col
                                            if (cellIndex >= dayOffset && dayCounter <= daysInMonth) {
                                                val date = calendarYearMonth.atDay(dayCounter)
                                                val isSelected = date == selectedDate
                                                val isToday = date == LocalDate.now()

                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when {
                                                                isSelected -> PeriodRosePrimary
                                                                isToday -> PeriodRoseContainer
                                                                else -> Color.Transparent
                                                            }
                                                        )
                                                        .clickable {
                                                            selectedDate = date
                                                            showInlineCalendar = false
                                                        }
                                                ) {
                                                    Text(
                                                        text = dayCounter.toString(),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                        color = when {
                                                             isSelected -> Color.White
                                                             isToday -> PeriodRosePrimary
                                                             else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                }
                                                dayCounter++
                                            } else {
                                                Spacer(modifier = Modifier.size(34.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. É início de uma nova menstruação?
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Novo ciclo menstrual?",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Marque se hoje iniciou uma nova menstruação",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilterChip(
                        selected = isPeriodStart,
                        onClick = { isPeriodStart = !isPeriodStart },
                        label = { Text(if (isPeriodStart) "Sim, início" else "Apenas diário") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PeriodRosePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // 3. Period Duration Stepper (if it's period start)
                if (isPeriodStart) {
                    Column {
                        Text(
                            text = "Duração Estimada do Sangramento",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (periodLengthDays > 1) periodLengthDays-- },
                                enabled = periodLengthDays > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Diminuir dias")
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PeriodRoseContainer,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "$periodLengthDays dias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PeriodRosePrimary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            IconButton(
                                onClick = { if (periodLengthDays < 15) periodLengthDays++ },
                                enabled = periodLengthDays < 15
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar dias")
                            }
                        }
                    }
                }

                // 4. Intensidade do Fluxo
                Column {
                    Text(
                        text = "Fluxo Menstrual",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        flowOptions.forEach { (code, label) ->
                            FilterChip(
                                selected = selectedFlow == code,
                                onClick = { selectedFlow = code },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PeriodRosePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 5. Nível de Dor
                Column {
                    Text(
                        text = "Nível de Dor",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        painOptions.forEach { pain ->
                            FilterChip(
                                selected = selectedPain.equals(pain, ignoreCase = true),
                                onClick = { selectedPain = pain },
                                label = { Text(pain, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 6. Sintomas Físicos
                Column {
                    Text(
                        text = "Sintomas Registrados",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        symptomOptions.forEach { symptom ->
                            val isSelected = selectedSymptoms.contains(symptom)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedSymptoms.remove(symptom)
                                    else selectedSymptoms.add(symptom)
                                },
                                label = { Text(symptom, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // 7. Bem-estar (Humor, Energia, Sono)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bem-Estar",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Humor
                    Text(text = "Humor:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        moodOptions.forEach { mood ->
                            FilterChip(
                                selected = selectedMood == mood,
                                onClick = { selectedMood = mood },
                                label = { Text(mood, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Energia
                    Text(text = "Energia:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        energyOptions.forEach { energy ->
                            FilterChip(
                                selected = selectedEnergy == energy,
                                onClick = { selectedEnergy = energy },
                                label = { Text(energy, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Sono
                    Text(text = "Qualidade do Sono:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sleepOptions.forEach { sleep ->
                            FilterChip(
                                selected = selectedSleep == sleep,
                                onClick = { selectedSleep = sleep },
                                label = { Text(sleep, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 8. Cervical Mucus (Opcional)
                Column {
                    Text(
                        text = "Fluido Cervical (Opcional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        mucusOptions.forEach { mucus ->
                            val isSelected = selectedMucus == mucus
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMucus = if (isSelected) "" else mucus },
                                label = { Text(mucus, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // 9. Observações Livres
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observações (opcional)") },
                    placeholder = { Text("Ex: Medicamentos, rotina, detalhes do dia...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PeriodRosePrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        selectedDate,
                        isPeriodStart,
                        periodLengthDays,
                        selectedFlow,
                        selectedPain,
                        selectedSymptoms.joinToString(", "),
                        selectedMood,
                        selectedEnergy,
                        selectedSleep,
                        selectedMucus,
                        notesText
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeriodRosePrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Salvar Registro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
