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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CyclePrediction
import com.example.ui.theme.FertileBlueBackground
import com.example.ui.theme.FertileBluePrimary
import com.example.ui.theme.OvulationPurpleContainer
import com.example.ui.theme.OvulationPurplePrimary
import com.example.ui.theme.PeriodRoseBackground
import com.example.ui.theme.PeriodRosePrimary
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PredictionCards(
    prediction: CyclePrediction,
    modifier: Modifier = Modifier
) {
    val portugueseLocale = Locale.forLanguageTag("pt-BR")
    val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMM, yyyy", portugueseLocale)
    val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prediction_cards_column"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Suas Previsões do Ciclo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Next Period Primary Estimate
        val windowText = if (prediction.nextPeriodWindowStart != null && prediction.nextPeriodWindowEnd != null) {
            "Faixa estimada: ${prediction.nextPeriodWindowStart.format(shortDateFormatter)} a ${prediction.nextPeriodWindowEnd.format(shortDateFormatter)} (±${prediction.uncertaintyDays} dias)"
        } else {
            "Intervalo padrão de 28 dias"
        }

        PredictionItemCard(
            title = "Próxima Menstruação Estimada",
            dateRange = prediction.nextPeriodStart?.format(dateFormatter) ?: "Aguardando registros",
            subtitle = "Modelo estatístico adaptativo: ${prediction.adaptiveCycleDays} dias • $windowText",
            icon = Icons.Default.WaterDrop,
            accentColor = PeriodRosePrimary,
            backgroundColor = PeriodRoseBackground
        )

        // 2. Projeções para 90 dias
        if (prediction.projectedCycles90Days.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Projeções para os Próximos 90 Dias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            color = PeriodRosePrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Estimativas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PeriodRosePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Projeção dos próximos ciclos dentro de 3 meses. Note que a incerteza aumenta para datas mais distantes no futuro:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    prediction.projectedCycles90Days.forEach { projected ->
                        val cycleLabel = if (projected.cycleIndex == 1) "1º Próximo ciclo" else "${projected.cycleIndex}º Ciclo futuro"
                        val formattedStart = projected.estimatedStartDate.format(dateFormatter)
                        val window = "${projected.windowStart.format(shortDateFormatter)} a ${projected.windowEnd.format(shortDateFormatter)}"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cycleLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PeriodRosePrimary
                                )
                                Text(
                                    text = formattedStart,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Faixa: $window",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "±${projected.uncertaintyDays} dias",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Ovulação Estimada
        PredictionItemCard(
            title = "Ovulação Estimada",
            dateRange = prediction.ovulationDate?.format(dateFormatter) ?: "Aguardando registros",
            subtitle = "Estimativa retrospectiva aproximada: 14 dias antes do ciclo seguinte",
            icon = Icons.Default.Egg,
            accentColor = OvulationPurplePrimary,
            backgroundColor = OvulationPurpleContainer
        )

        // 4. Janela Fértil Estimada
        val fertileText = if (prediction.fertileStart != null && prediction.fertileEnd != null) {
            "${prediction.fertileStart.format(shortDateFormatter)} até ${prediction.fertileEnd.format(shortDateFormatter)}"
        } else {
            "Aguardando registros"
        }

        PredictionItemCard(
            title = "Janela Fértil Estimada",
            dateRange = fertileText,
            subtitle = "5 dias antes até 1 dia após a ovulação estimada",
            icon = Icons.Default.Opacity,
            accentColor = FertileBluePrimary,
            backgroundColor = FertileBlueBackground
        )

        // 5. Scientific Medical Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Aviso: As datas são estimativas baseadas nos registros do seu histórico pessoal e não confirmam ovulação. Não devem ser usadas isoladamente para contracepção ou planejamento médico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PredictionItemCard(
    title: String,
    dateRange: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = accentColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
