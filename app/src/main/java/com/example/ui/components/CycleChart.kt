package com.example.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CycleEntity
import com.example.ui.theme.OvulationPurplePrimary
import com.example.ui.theme.PeriodRosePrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CycleChart(
    cycles: List<CycleEntity>,
    averageCycleDays: Int,
    mlPredictedCycleDays: Int,
    modifier: Modifier = Modifier
) {
    if (cycles.size < 2) return

    val portugueseLocale = Locale.forLanguageTag("pt-BR")
    var selectedChartType by remember { mutableIntStateOf(0) } // 0: Duração do ciclo, 1: Duração da menstruação, 2: Sintomas
    var pageOffset by remember { mutableIntStateOf(0) }

    val sortedCycles = cycles.sortedBy { it.startDateEpochDay }

    // Intervals pairs
    val allIntervals = (1 until sortedCycles.size).map { i ->
        val days = (sortedCycles[i].startDateEpochDay - sortedCycles[i - 1].startDateEpochDay).toInt()
        val date = LocalDate.ofEpochDay(sortedCycles[i].startDateEpochDay)
        val label = date.format(DateTimeFormatter.ofPattern("MMM/yy", portugueseLocale))
            .replaceFirstChar { it.uppercase() }
        Pair(label, days.coerceIn(15, 60))
    }

    // Period lengths
    val allPeriodLengths = sortedCycles.map { cycle ->
        val date = LocalDate.ofEpochDay(cycle.startDateEpochDay)
        val label = date.format(DateTimeFormatter.ofPattern("MMM/yy", portugueseLocale))
            .replaceFirstChar { it.uppercase() }
        Pair(label, cycle.periodLengthDays.coerceIn(1, 15))
    }

    // Symptom counts
    val symptomFrequency = remember(cycles) {
        val countMap = mutableMapOf<String, Int>()
        cycles.forEach { cycle ->
            if (cycle.symptoms.isNotBlank()) {
                cycle.symptoms.split(",", "•", "/")
                    .map { it.trim() }
                    .filter { it.isNotBlank() && !it.startsWith("Humor:") && !it.startsWith("Fluido:") }
                    .forEach { symptom ->
                        countMap[symptom] = (countMap[symptom] ?: 0) + 1
                    }
            }
        }
        countMap.entries.sortedByDescending { it.value }.take(5)
    }

    val maxDisplayItems = 6
    val maxPages = (allIntervals.size + maxDisplayItems - 1) / maxDisplayItems

    // Paginated intervals for navigation
    val displayedIntervals = remember(allIntervals, pageOffset) {
        val fromIndex = (allIntervals.size - (pageOffset + 1) * maxDisplayItems).coerceAtLeast(0)
        val toIndex = (allIntervals.size - pageOffset * maxDisplayItems).coerceAtMost(allIntervals.size)
        if (fromIndex < toIndex) allIntervals.subList(fromIndex, toIndex) else allIntervals.takeLast(maxDisplayItems)
    }

    val displayedPeriodLengths = remember(allPeriodLengths, pageOffset) {
        val fromIndex = (allPeriodLengths.size - (pageOffset + 1) * maxDisplayItems).coerceAtLeast(0)
        val toIndex = (allPeriodLengths.size - pageOffset * maxDisplayItems).coerceAtMost(allPeriodLengths.size)
        if (fromIndex < toIndex) allPeriodLengths.subList(fromIndex, toIndex) else allPeriodLengths.takeLast(maxDisplayItems)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cycle_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Análise Visual de Padrões",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (selectedChartType) {
                            0 -> "Como seu ciclo evoluiu ao longo do tempo"
                            1 -> "Variação na duração do sangramento"
                            else -> "Sintomas que você registrou com mais frequência"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = PeriodRosePrimary.copy(alpha = 0.12f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Média: ${averageCycleDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PeriodRosePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedChartType == 0,
                    onClick = { selectedChartType = 0 },
                    label = { Text("Ciclos (dias)", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedChartType == 1,
                    onClick = { selectedChartType = 1 },
                    label = { Text("Menstruação", fontSize = 12.sp) }
                )
                FilterChip(
                    selected = selectedChartType == 2,
                    onClick = { selectedChartType = 2 },
                    label = { Text("Sintomas", fontSize = 12.sp) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedChartType) {
                // 0: Duração dos Ciclos (Linha contínua + linha adaptativa tracejada)
                0 -> {
                    val lineColor = PeriodRosePrimary
                    val adaptiveLineColor = OvulationPurplePrimary
                    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx()

                        val maxDays = (displayedIntervals.maxOfOrNull { it.second } ?: 35).coerceAtLeast(35) + 3
                        val minDays = (displayedIntervals.minOfOrNull { it.second } ?: 21).coerceAtMost(21) - 3
                        val range = (maxDays - minDays).toFloat().coerceAtLeast(1f)

                        val stepX = if (displayedIntervals.size > 1) canvasWidth / (displayedIntervals.size - 1) else canvasWidth
                        val points = displayedIntervals.mapIndexed { index, pair ->
                            val x = index * stepX
                            val y = canvasHeight - ((pair.second - minDays) / range) * canvasHeight
                            Offset(x, y)
                        }

                        // Grid lines
                        for (i in 0..3) {
                            val gridY = canvasHeight * (i.toFloat() / 3)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, gridY),
                                end = Offset(canvasWidth, gridY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Adaptive Model Baseline (dashed line)
                        val adaptiveY = canvasHeight - ((mlPredictedCycleDays - minDays) / range) * canvasHeight
                        if (adaptiveY in 0f..canvasHeight) {
                            drawLine(
                                color = adaptiveLineColor.copy(alpha = 0.7f),
                                start = Offset(0f, adaptiveY),
                                end = Offset(canvasWidth, adaptiveY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        if (points.isNotEmpty()) {
                            val strokePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val p1 = points[i - 1]
                                    val p2 = points[i]
                                    val cp1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                                    val cp2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(strokePath)
                                lineTo(points.last().x, canvasHeight)
                                lineTo(points.first().x, canvasHeight)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.02f)),
                                    startY = 0f,
                                    endY = canvasHeight
                                )
                            )

                            drawPath(
                                path = strokePath,
                                color = lineColor,
                                style = Stroke(width = 3.dp.toPx())
                            )

                            points.forEach { pt ->
                                drawCircle(color = lineColor, radius = 5.dp.toPx(), center = pt)
                                drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // X-Axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayedIntervals.forEach { pair ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${pair.second}d",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PeriodRosePrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 1: Duração da Menstruação (Barras)
                1 -> {
                    val barColor = PeriodRosePrimary

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height - 24.dp.toPx()
                        val maxPeriod = 10f
                        val barWidth = 24.dp.toPx()
                        val count = displayedPeriodLengths.size
                        val stepX = canvasWidth / count.coerceAtLeast(1)

                        displayedPeriodLengths.forEachIndexed { i, pair ->
                            val barHeight = (pair.second / maxPeriod) * canvasHeight
                            val x = i * stepX + (stepX - barWidth) / 2f
                            val y = canvasHeight - barHeight

                            drawRoundRect(
                                color = barColor.copy(alpha = 0.85f),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayedPeriodLengths.forEach { pair ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = pair.first,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${pair.second} dias",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PeriodRosePrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // 2: Sintomas mais frequentes
                2 -> {
                    if (symptomFrequency.isEmpty()) {
                        Text(
                            text = "Nenhum sintoma registrado nos ciclos até o momento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        val maxCount = symptomFrequency.maxOf { it.value }.toFloat().coerceAtLeast(1f)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            symptomFrequency.forEach { (symptom, count) ->
                                val fraction = count / maxCount
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = symptom,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "$count ${if (count == 1) "ciclo" else "ciclos"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .height(10.dp)
                                                .clip(RoundedCornerShape(5.dp))
                                                .background(PeriodRosePrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Temporal Navigation Controls (slide/navigate left and right)
            if (allIntervals.size > maxDisplayItems && selectedChartType != 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (pageOffset < maxPages - 1) pageOffset++ },
                        enabled = pageOffset < maxPages - 1
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Período anterior")
                    }

                    Text(
                        text = if (pageOffset == 0) "Período mais recente" else "Período anterior",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { if (pageOffset > 0) pageOffset-- },
                        enabled = pageOffset > 0
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Período posterior")
                    }
                }
            }

            // Legend
            if (selectedChartType == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PeriodRosePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dados Reais",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OvulationPurplePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Previsão Adaptativa",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
