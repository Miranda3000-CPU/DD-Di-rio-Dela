package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CycleEntity
import com.example.ui.theme.PeriodRoseBackground
import com.example.ui.theme.PeriodRosePrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.FilterChip

@Composable
fun CycleHistoryList(
    cycles: List<CycleEntity>,
    averageCycleDays: Int = 28,
    mlPredictedCycleDays: Int = 28,
    mlConfidencePercent: Int = 85,
    onDeleteCycle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val PortugueseLocale = Locale("pt", "BR")
    val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", PortugueseLocale)

    var searchQuery by remember { mutableStateOf("") }
    var sortDescending by remember { mutableStateOf(true) }
    var cycleToDelete by remember { mutableStateOf<CycleEntity?>(null) }

    val filteredCycles = remember(cycles, searchQuery, sortDescending) {
        var list = if (searchQuery.isBlank()) {
            cycles
        } else {
            cycles.filter { cycle ->
                val dateStr = LocalDate.ofEpochDay(cycle.startDateEpochDay).format(dateFormatter)
                dateStr.contains(searchQuery, ignoreCase = true) ||
                        cycle.flowIntensity.contains(searchQuery, ignoreCase = true) ||
                        cycle.symptoms.contains(searchQuery, ignoreCase = true) ||
                        cycle.notes.contains(searchQuery, ignoreCase = true)
            }
        }
        if (sortDescending) list.sortedByDescending { it.startDateEpochDay }
        else list.sortedBy { it.startDateEpochDay }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cycle_history_column"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Histórico de Ciclos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${filteredCycles.size} de ${cycles.size} registros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Chart Visualization if enough data
        if (cycles.size >= 2) {
            CycleChart(
                cycles = cycles,
                averageCycleDays = averageCycleDays,
                mlPredictedCycleDays = mlPredictedCycleDays
            )

            // ML Comparison Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Média Histórica",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$averageCycleDays dias",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Previsão TensorFlow/ML",
                            style = MaterialTheme.typography.labelMedium,
                            color = PeriodRosePrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$mlPredictedCycleDays dias",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PeriodRosePrimary
                        )
                    }
                }
            }
        }

        // 2. Search & Sort Controls
        if (cycles.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar sintomas, fluxo ou notas...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("history_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                FilterChip(
                    selected = sortDescending,
                    onClick = { sortDescending = !sortDescending },
                    label = { Text(if (sortDescending) "Recentes" else "Antigos") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (sortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("history_sort_chip")
                )
            }
        }

        // 3. Filtered Cycles List
        if (filteredCycles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = PeriodRosePrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (cycles.isEmpty()) "Nenhum ciclo registrado" else "Nenhum resultado encontrado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (cycles.isEmpty()) "Clique em 'Registrar' para iniciar seu acompanhamento." else "Tente buscar outros termos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val allSortedByEpoch = cycles.sortedByDescending { it.startDateEpochDay }

            filteredCycles.forEach { cycle ->
                val startDate = LocalDate.ofEpochDay(cycle.startDateEpochDay)
                val formattedDate = startDate.format(dateFormatter)

                // Calculate cycle interval relative to previous cycle chronologically
                val currentIndexInAll = allSortedByEpoch.indexOf(cycle)
                val intervalText = if (currentIndexInAll >= 0 && currentIndexInAll < allSortedByEpoch.size - 1) {
                    val days = cycle.startDateEpochDay - allSortedByEpoch[currentIndexInAll + 1].startDateEpochDay
                    "Ciclo de $days dias"
                } else {
                    "Primeiro registro"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cycle_history_item_${cycle.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PeriodRoseBackground,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = PeriodRosePrimary,
                                modifier = Modifier
                                    .padding(10.dp)
                                    .size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Duração: ${cycle.periodLengthDays} dias • Fluxo: ${cycle.flowIntensity}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = intervalText,
                                style = MaterialTheme.typography.labelSmall,
                                color = PeriodRosePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (cycle.symptoms.isNotEmpty() || cycle.notes.isNotEmpty()) {
                                val details = listOf(cycle.symptoms, cycle.notes)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" • ")
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        IconButton(onClick = { cycleToDelete = cycle }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remover registro",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Deletion confirmation dialog
    cycleToDelete?.let { cycle ->
        AlertDialog(
            onDismissRequest = { cycleToDelete = null },
            title = { Text("Remover registro?") },
            text = {
                val date = LocalDate.ofEpochDay(cycle.startDateEpochDay).format(dateFormatter)
                Text("Tem certeza que deseja apagar a entrada de $date? As estatísticas do ciclo serão recalculadas.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCycle(cycle.id)
                        cycleToDelete = null
                    }
                ) {
                    Text("Remover", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { cycleToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
