package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyLogEntity
import com.example.ui.theme.PeriodRoseBackground
import com.example.ui.theme.PeriodRoseContainer
import com.example.ui.theme.PeriodRosePrimary
import com.example.ui.theme.SoftBorder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private data class SymptomOption(val id: String, val label: String, val emoji: String)
private data class FlowOption(val code: String, val label: String, val emoji: String)
private data class PainOption(val code: String, val label: String, val emoji: String, val subtitle: String)
private data class MoodOption(val name: String, val emoji: String)
private data class ScaleOption(val value: String, val label: String, val emoji: String)

@OptIn(ExperimentalMaterial3Api::class)
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("log_period_modal_sheet")
    ) {
        LogPeriodSheetContent(
            initialDate = initialDate,
            existingLog = existingLog,
            onDismiss = onDismiss,
            onSave = onSave
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LogPeriodSheetContent(
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
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val portugueseLocale = Locale.forLanguageTag("pt-BR")
    val fullDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", portugueseLocale)

    var selectedDate by remember { mutableStateOf(initialDate) }
    var showInlineCalendar by remember { mutableStateOf(false) }
    var calendarYearMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    var isPeriodStart by remember {
        mutableStateOf(existingLog?.let { it.flowIntensity != "SEM_FLUXO" } ?: true)
    }
    var periodLengthDays by remember { mutableIntStateOf(5) }
    var selectedFlow by remember { mutableStateOf(existingLog?.flowIntensity ?: "MÉDIO") }
    var selectedPain by remember { mutableStateOf(existingLog?.painLevel ?: "NENHUMA") }
    var selectedMood by remember { mutableStateOf(existingLog?.mood ?: "Tranquila") }
    var selectedEnergy by remember { mutableStateOf(existingLog?.energyLevel ?: "Média") }
    var selectedSleep by remember { mutableStateOf(existingLog?.sleepQuality ?: "Boa") }
    var selectedMucus by remember { mutableStateOf(existingLog?.cervicalMucus ?: "") }
    var notesText by remember { mutableStateOf(existingLog?.notes ?: "") }

    val symptomOptions = listOf(
        SymptomOption("Cólica", "Cólica", "⚡"),
        SymptomOption("Dor de cabeça", "Dor de cabeça", "🤕"),
        SymptomOption("Sensibilidade mamária", "Sensib. mamária", "🌸"),
        SymptomOption("Inchaço", "Inchaço", "🎈"),
        SymptomOption("Náusea", "Náusea", "🤢"),
        SymptomOption("Acne", "Acne", "✨"),
        SymptomOption("Cansaço", "Cansaço", "🥱"),
        SymptomOption("Alteração intestinal", "Alt. intestinal", "🍃"),
        SymptomOption("Dor lombar", "Dor lombar", "🚶‍♀️"),
        SymptomOption("Insônia", "Insônia", "🌙")
    )

    val selectedSymptoms = remember {
        mutableStateListOf<String>().apply {
            if (!existingLog?.symptoms.isNullOrBlank()) {
                val list = existingLog!!.symptoms.split(",").map { it.trim() }.filter { it.isNotBlank() }
                addAll(list)
            }
        }
    }

    val flowOptions = listOf(
        FlowOption("SEM_FLUXO", "Sem fluxo", "⚪"),
        FlowOption("LEVE", "Leve", "💧"),
        FlowOption("MÉDIO", "Médio", "💧💧"),
        FlowOption("INTENSO", "Intenso", "💧💧💧"),
        FlowOption("ESCAPE", "Escape / Spotting", "🩸")
    )

    val painOptions = listOf(
        PainOption("Nenhuma", "Nenhuma", "😊", "Sem desconforto"),
        PainOption("Leve", "Leve", "🙂", "Cólica suave"),
        PainOption("Moderada", "Moderada", "😐", "Incômodo perceptível"),
        PainOption("Forte", "Forte", "😣", "Dor intensa")
    )

    val moodOptions = listOf(
        MoodOption("Tranquila", "😌"),
        MoodOption("Feliz", "😊"),
        MoodOption("Sensível", "🥺"),
        MoodOption("Ansiosa", "😰"),
        MoodOption("Irritada", "😤"),
        MoodOption("Cansada", "🥱"),
        MoodOption("Triste", "😢")
    )

    val energyOptions = listOf(
        ScaleOption("Baixa", "Baixa", "🌙"),
        ScaleOption("Média", "Média", "⚡"),
        ScaleOption("Alta", "Alta", "🔥")
    )

    val sleepOptions = listOf(
        ScaleOption("Ruim", "Ruim", "🥱"),
        ScaleOption("Regular", "Regular", "💤"),
        ScaleOption("Boa", "Boa", "✨")
    )

    val mucusOptions = listOf(
        Pair("Seco", "⚪ Seco"),
        Pair("Pegajoso", "💧 Pegajoso"),
        Pair("Cremoso", "🥛 Cremoso"),
        Pair("Aquoso", "🌊 Aquoso"),
        Pair("Clara de Ovo (Fértil)", "⭐ Clara de Ovo (Fértil)")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Modal Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PeriodRoseContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = PeriodRosePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Registrar Dia",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Acompanhamento diário do seu ciclo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Fechar modal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Scrollable Form Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("log_period_dialog_content"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Interactive Date Picker Card
            SectionCard(
                title = "Data do Registro",
                icon = Icons.Default.CalendarMonth
            ) {
                val formattedDate = selectedDate.format(fullDateFormatter)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(portugueseLocale) else it.toString() }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick date selection shortcuts
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isToday = selectedDate == LocalDate.now()
                    val isYesterday = selectedDate == LocalDate.now().minusDays(1)
                    val is2DaysAgo = selectedDate == LocalDate.now().minusDays(2)

                    FilterChip(
                        selected = isToday,
                        onClick = {
                            selectedDate = LocalDate.now()
                            calendarYearMonth = YearMonth.from(selectedDate)
                        },
                        label = { Text("Hoje", fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal) },
                        colors = filterChipSelectedColors()
                    )
                    FilterChip(
                        selected = isYesterday,
                        onClick = {
                            selectedDate = LocalDate.now().minusDays(1)
                            calendarYearMonth = YearMonth.from(selectedDate)
                        },
                        label = { Text("Ontem", fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Normal) },
                        colors = filterChipSelectedColors()
                    )
                    FilterChip(
                        selected = is2DaysAgo,
                        onClick = {
                            selectedDate = LocalDate.now().minusDays(2)
                            calendarYearMonth = YearMonth.from(selectedDate)
                        },
                        label = { Text("2 dias atrás", fontWeight = if (is2DaysAgo) FontWeight.Bold else FontWeight.Normal) },
                        colors = filterChipSelectedColors()
                    )
                    FilterChip(
                        selected = showInlineCalendar,
                        onClick = { showInlineCalendar = !showInlineCalendar },
                        label = { Text(if (showInlineCalendar) "Fechar calendário" else "Outra data 📅") },
                        colors = filterChipSelectedColors()
                    )
                }

                // Inline Expandable Calendar Grid
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
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { calendarYearMonth = calendarYearMonth.minusMonths(1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Mês anterior",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            val monthYearName = calendarYearMonth.month
                                .getDisplayName(TextStyle.FULL, portugueseLocale)
                                .replaceFirstChar { it.uppercase() } + " ${calendarYearMonth.year}"

                            Text(
                                text = monthYearName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(
                                onClick = { calendarYearMonth = calendarYearMonth.plusMonths(1) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Próximo mês",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    modifier = Modifier.width(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val firstDay = calendarYearMonth.atDay(1)
                        val daysInMonth = calendarYearMonth.lengthOfMonth()
                        val dayOffset = firstDay.dayOfWeek.value % 7

                        var dayCounter = 1
                        val totalCells = ((daysInMonth + dayOffset + 6) / 7) * 7

                        for (row in 0 until (totalCells / 7)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
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
                                                .size(38.dp)
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
                                                style = MaterialTheme.typography.bodyMedium,
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
                                        Spacer(modifier = Modifier.size(38.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Novo ciclo menstrual e duração estimada
            SectionCard(
                title = "Ciclo Menstrual",
                icon = Icons.Default.WaterDrop
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Início de um novo ciclo?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Marque se hoje iniciou o seu primeiro dia de sangramento menstrual",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isPeriodStart,
                        onCheckedChange = { isPeriodStart = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PeriodRosePrimary
                        )
                    )
                }

                // Duração estimada com stepper ergonômico
                AnimatedVisibility(
                    visible = isPeriodStart,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .background(
                                PeriodRoseBackground,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Duração estimada do sangramento",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Estimativa de quantos dias o sangramento costuma durar:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (periodLengthDays > 1) periodLengthDays-- },
                                enabled = periodLengthDays > 1,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (periodLengthDays > 1) PeriodRoseContainer else Color.Transparent
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Diminuir dias",
                                    tint = if (periodLengthDays > 1) PeriodRosePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, SoftBorder),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = "$periodLengthDays dias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PeriodRosePrimary,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                                )
                            }

                            IconButton(
                                onClick = { if (periodLengthDays < 15) periodLengthDays++ },
                                enabled = periodLengthDays < 15,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (periodLengthDays < 15) PeriodRoseContainer else Color.Transparent
                                    )
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Aumentar dias",
                                    tint = if (periodLengthDays < 15) PeriodRosePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Intensidade do Fluxo Menstrual
            SectionCard(
                title = "Fluxo Menstrual",
                icon = Icons.Default.WaterDrop
            ) {
                Text(
                    text = "Como foi o sangramento registrado neste dia?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    flowOptions.forEach { option ->
                        val isSelected = selectedFlow == option.code
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFlow = option.code },
                            label = {
                                Text(
                                    text = "${option.emoji} ${option.label}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = filterChipSelectedColors(),
                            modifier = Modifier.heightIn(min = 44.dp)
                        )
                    }
                }
            }

            // 4. Nível de Dor / Cólica (Grid 2x2 sem compactação)
            SectionCard(
                title = "Nível de Dor & Cólica",
                icon = Icons.Default.Favorite
            ) {
                Text(
                    text = "Intensidade de dor ou incômodo corporal sentido hoje:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Layout 2x2 espaçoso com botões acessíveis e informativos
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PainSelectionCard(
                            option = painOptions[0],
                            isSelected = selectedPain.equals(painOptions[0].code, ignoreCase = true),
                            onClick = { selectedPain = painOptions[0].code },
                            modifier = Modifier.weight(1f)
                        )
                        PainSelectionCard(
                            option = painOptions[1],
                            isSelected = selectedPain.equals(painOptions[1].code, ignoreCase = true),
                            onClick = { selectedPain = painOptions[1].code },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PainSelectionCard(
                            option = painOptions[2],
                            isSelected = selectedPain.equals(painOptions[2].code, ignoreCase = true),
                            onClick = { selectedPain = painOptions[2].code },
                            modifier = Modifier.weight(1f)
                        )
                        PainSelectionCard(
                            option = painOptions[3],
                            isSelected = selectedPain.equals(painOptions[3].code, ignoreCase = true),
                            onClick = { selectedPain = painOptions[3].code },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Sintomas Físicos
            SectionCard(
                title = "Sintomas Físicos",
                icon = Icons.Default.Spa,
                badge = if (selectedSymptoms.isNotEmpty()) "${selectedSymptoms.size} selecionado(s)" else null
            ) {
                Text(
                    text = "Toque para marcar ou desmarcar os sintomas que você sentiu:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    symptomOptions.forEach { symptom ->
                        val isSelected = selectedSymptoms.contains(symptom.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedSymptoms.remove(symptom.id)
                                else selectedSymptoms.add(symptom.id)
                            },
                            label = {
                                Text(
                                    text = "${symptom.emoji} ${symptom.label}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = filterChipSelectedColors(),
                            modifier = Modifier.heightIn(min = 44.dp)
                        )
                    }
                }
            }

            // 6. Bem-Estar Geral (Humor, Energia, Sono)
            SectionCard(
                title = "Humor & Bem-Estar",
                icon = Icons.Default.SentimentSatisfied
            ) {
                // Humor
                Text(
                    text = "Como está seu humor hoje?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    moodOptions.forEach { mood ->
                        val isSelected = selectedMood.equals(mood.name, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = mood.name },
                            label = {
                                Text(
                                    text = "${mood.emoji} ${mood.name}",
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = filterChipSelectedColors(),
                            modifier = Modifier.heightIn(min = 44.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Energia
                Text(
                    text = "Nível de Energia:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    energyOptions.forEach { energy ->
                        val isSelected = selectedEnergy.equals(energy.value, ignoreCase = true)
                        SegmentedOptionItem(
                            label = "${energy.emoji} ${energy.label}",
                            isSelected = isSelected,
                            onClick = { selectedEnergy = energy.value },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Qualidade do Sono
                Text(
                    text = "Qualidade do Sono:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sleepOptions.forEach { sleep ->
                        val isSelected = selectedSleep.equals(sleep.value, ignoreCase = true)
                        SegmentedOptionItem(
                            label = "${sleep.emoji} ${sleep.label}",
                            isSelected = isSelected,
                            onClick = { selectedSleep = sleep.value },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 7. Fluido Cervical (Opcional)
            SectionCard(
                title = "Fluido Cervical",
                icon = Icons.Default.WaterDrop,
                badge = "Opcional"
            ) {
                Text(
                    text = "Ajuda a identificar a janela fértil e ovulação:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    mucusOptions.forEach { (value, label) ->
                        val isSelected = selectedMucus == value
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMucus = if (isSelected) "" else value },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = filterChipSelectedColors(),
                            modifier = Modifier.heightIn(min = 44.dp)
                        )
                    }
                }
            }

            // 8. Observações Livres
            SectionCard(
                title = "Observações & Notas",
                icon = Icons.Default.Edit,
                badge = "Opcional"
            ) {
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Anotações pessoais do dia") },
                    placeholder = { Text("Ex: Medicamentos tomados, rotina, detalhes de alimentação, etc.") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PeriodRosePrimary,
                        cursorColor = PeriodRosePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // 9. Sticky Action Bar at Bottom (Ergonômico e fácil de alcançar)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, SoftBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(0.7f),
                    border = BorderStroke(1.dp, SoftBorder)
                ) {
                    Text(
                        text = "Cancelar",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PeriodRosePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .weight(1.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salvar Registro",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    badge: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SoftBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PeriodRoseContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = PeriodRosePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PeriodRoseContainer.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PeriodRosePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PainSelectionCard(
    option: PainOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PeriodRoseContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PeriodRosePrimary else SoftBorder
        ),
        modifier = modifier
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = option.emoji,
                fontSize = 22.sp
            )
            Column {
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) PeriodRosePrimary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) PeriodRosePrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SegmentedOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) PeriodRosePrimary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) PeriodRosePrimary else SoftBorder
        ),
        modifier = modifier
            .heightIn(min = 44.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun filterChipSelectedColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = PeriodRosePrimary,
    selectedLabelColor = Color.White
)
