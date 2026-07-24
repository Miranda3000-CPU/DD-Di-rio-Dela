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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CycleEntity
import com.example.ui.theme.OvulationPurplePrimary
import com.example.ui.theme.PeriodRosePrimary
import java.time.LocalDate

@Composable
fun CycleChart(
    cycles: List<CycleEntity>,
    averageCycleDays: Int,
    mlPredictedCycleDays: Int,
    modifier: Modifier = Modifier
) {
    if (cycles.size < 2) return

    val sortedCycles = cycles.sortedBy { it.startDateEpochDay }
    val intervalPairs = (1 until sortedCycles.size).map { i ->
        val days = (sortedCycles[i].startDateEpochDay - sortedCycles[i - 1].startDateEpochDay).toInt()
        val date = LocalDate.ofEpochDay(sortedCycles[i].startDateEpochDay)
        val monthLabel = when (date.monthValue) {
            1 -> "Jan"; 2 -> "Fev"; 3 -> "Mar"; 4 -> "Abr"; 5 -> "Mai"; 6 -> "Jun"
            7 -> "Jul"; 8 -> "Ago"; 9 -> "Set"; 10 -> "Out"; 11 -> "Nov"; else -> "Dez"
        }
        Pair(monthLabel, days.coerceIn(15, 45))
    }.takeLast(6) // Take up to last 6 cycles for clear readable spacing

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gráfico Linear de Ciclos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Evolução do tempo de duração (dias)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = OvulationPurplePrimary.copy(alpha = 0.12f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "ML: ${mlPredictedCycleDays}d",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OvulationPurplePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val lineColor = PeriodRosePrimary
            val mlLineColor = OvulationPurplePrimary
            val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height - 30.dp.toPx()

                val maxDays = (intervalPairs.maxOfOrNull { it.second } ?: 35).coerceAtLeast(35) + 3
                val minDays = (intervalPairs.minOfOrNull { it.second } ?: 21).coerceAtMost(21) - 3

                val range = (maxDays - minDays).toFloat().coerceAtLeast(1f)

                // Calculate points
                val stepX = if (intervalPairs.size > 1) canvasWidth / (intervalPairs.size - 1) else canvasWidth
                val points = intervalPairs.mapIndexed { index, pair ->
                    val x = index * stepX
                    val y = canvasHeight - ((pair.second - minDays) / range) * canvasHeight
                    Offset(x, y)
                }

                // Draw Horizontal Grid Lines
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val gridY = canvasHeight * (i.toFloat() / gridSteps)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, gridY),
                        end = Offset(canvasWidth, gridY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw ML Prediction Line (Dashed)
                val mlY = canvasHeight - ((mlPredictedCycleDays - minDays) / range) * canvasHeight
                if (mlY in 0f..canvasHeight) {
                    drawLine(
                        color = mlLineColor.copy(alpha = 0.6f),
                        start = Offset(0f, mlY),
                        end = Offset(canvasWidth, mlY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                }

                if (points.isNotEmpty()) {
                    // Build path for smooth/linear line
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val p1 = points[i - 1]
                            val p2 = points[i]
                            val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                            val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                        }
                    }

                    // Build filled area path
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(points.last().x, canvasHeight)
                        lineTo(points.first().x, canvasHeight)
                        close()
                    }

                    // Draw gradient under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                lineColor.copy(alpha = 0.25f),
                                lineColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = canvasHeight
                        )
                    )

                    // Draw smooth curve stroke
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw node circles & text
                    points.forEachIndexed { index, point ->
                        // Outer glowing circle
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = point
                        )
                        // Inner white core
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                intervalPairs.forEach { pair ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = pair.first,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${pair.second}d",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PeriodRosePrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chart Legend
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
                    text = "Ciclos Registrados",
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
                    text = "Previsão Modelo ML",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
