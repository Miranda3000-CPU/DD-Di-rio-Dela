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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Opacity
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
    onDismiss: () -> Unit,
    onSave: (startDate: LocalDate, periodLengthDays: Int, flowIntensity: String, symptoms: String, notes: String) -> Unit
) {
    val PortugueseLocale = Locale("pt", "BR")
    val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", PortugueseLocale)

    var selectedDate by remember { mutableStateOf(initialDate) }
    var showInlineCalendar by remember { mutableStateOf(false) }
    var calendarYearMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    var periodLengthDays by remember { mutableIntStateOf(5) }
    var selectedFlow by remember { mutableStateOf("MÉDIO") }
    var notesText by remember { mutableStateOf("") }

    // Enhanced Flow Options with visual icons
    val flowOptions = listOf(
        Pair("LEVE", "💧 Leve"),
        Pair("MÉDIO", "💧💧 Médio"),
        Pair("INTENSO", "💧💧💧 Intenso"),
        Pair("ESCAPE", "🩸 Escape")
    )

    // Moods & Feelings
    val moodOptions = listOf("😊 Tranquila", "😰 Ansiosa", "🥺 Sensível", "😠 Irritada", "😴 Cansada", "⚡ Produtiva", "😭 Triste")
    val selectedMoods = remember { mutableStateListOf<String>() }

    // Physical Symptoms
    val physicalSymptoms = listOf("⚡ Cólica", "🤕 Dor de cabeça", "🎈 Inchaço", "🍒 Mamas sensíveis", "✨ Acne", "🤢 Náusea", "🦴 Dor lombar", "💤 Insônia")
    val selectedSymptoms = remember { mutableStateListOf<String>() }

    // Cervical Mucus / Fluid
    val mucusOptions = listOf("Seco", "Cremoso", "Aquoso", "Clara de Ovo (Fértil)")
    var selectedMucus by remember { mutableStateOf("") }

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
                        text = "Registrar Menstruação",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Diário Dela • Coleta completa",
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
                                text = "Data de Início da Menstruação",
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
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(PortugueseLocale) else it.toString() }

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
                                // Calendar Header Navigation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { calendarYearMonth = calendarYearMonth.minusMonths(1) }) {
                                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
                                    }

                                    val monthYearName = calendarYearMonth.month
                                        .getDisplayName(TextStyle.FULL, PortugueseLocale)
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

                                // Days of Week Header
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

                                // Calendar Days Grid
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

                // 2. Period Duration Stepper
                Column {
                    Text(
                        text = "Duração Estimada da Menstruação",
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

                // 3. Flow Intensity Selector
                Column {
                    Text(
                        text = "Intensidade do Fluxo",
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

                // 4. Mood Selector
                Column {
                    Text(
                        text = "Humor & Sentimentos",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moodOptions.forEach { mood ->
                            val isSelected = selectedMoods.contains(mood)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) selectedMoods.remove(mood)
                                    else selectedMoods.add(mood)
                                },
                                label = { Text(mood, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // 5. Symptoms Selector
                Column {
                    Text(
                        text = "Sintomas Físicos",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        physicalSymptoms.forEach { symptom ->
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

                // 6. Cervical Mucus
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
                                onClick = {
                                    selectedMucus = if (isSelected) "" else mucus
                                },
                                label = { Text(mucus, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                // 7. Notes text field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observações (opcional)") },
                    placeholder = { Text("Ex: Medicamentos, nivel de estresse, atípicos...") },
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
                    val combinedSymptomsList = mutableListOf<String>()
                    combinedSymptomsList.addAll(selectedSymptoms)
                    if (selectedMoods.isNotEmpty()) {
                        combinedSymptomsList.add("Humor: ${selectedMoods.joinToString("/")}")
                    }
                    if (selectedMucus.isNotBlank()) {
                        combinedSymptomsList.add("Fluido: $selectedMucus")
                    }

                    onSave(
                        selectedDate,
                        periodLengthDays,
                        selectedFlow,
                        combinedSymptomsList.joinToString(", "),
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
