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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PredictionCards(
    prediction: CyclePrediction,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMM, yyyy")
    val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prediction_cards_column"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Previsões do Ciclo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Next Period
        PredictionItemCard(
            title = "Próxima Menstruação",
            dateRange = prediction.nextPeriodStart?.format(dateFormatter) ?: "Aguardando registros",
            subtitle = "Modelo ML Adaptativo: ${prediction.mlPredictedCycleDays} dias • Média histórica: ${prediction.averageCycleDays} dias",
            icon = Icons.Default.WaterDrop,
            accentColor = PeriodRosePrimary,
            backgroundColor = PeriodRoseBackground
        )

        // ML Model Confidence Pill
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Modelo Predictivo Adaptativo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OvulationPurplePrimary
                    )
                    Text(
                        text = "Confiança da Previsão: ${prediction.mlConfidencePercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    color = OvulationPurplePrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "ML Ativo",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OvulationPurplePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. Ovulation
        PredictionItemCard(
            title = "Ovulação Estimada",
            dateRange = prediction.ovulationDate?.format(dateFormatter) ?: "Aguardando registros",
            subtitle = "Aproximadamente 14 dias antes do próximo ciclo",
            icon = Icons.Default.Egg,
            accentColor = OvulationPurplePrimary,
            backgroundColor = OvulationPurpleContainer
        )

        // 3. Fertile Window
        val fertileText = if (prediction.fertileStart != null && prediction.fertileEnd != null) {
            "${prediction.fertileStart.format(shortDateFormatter)} até ${prediction.fertileEnd.format(shortDateFormatter)}"
        } else {
            "Aguardando registros"
        }

        PredictionItemCard(
            title = "Período Fértil",
            dateRange = fertileText,
            subtitle = "5 dias antes até 1 dia após a ovulação",
            icon = Icons.Default.Opacity,
            accentColor = FertileBluePrimary,
            backgroundColor = FertileBlueBackground
        )

        // Medical Disclaimer Card
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
                    text = "Aviso: Todas as datas e cálculos são estimativas baseadas em médias matemáticas para uso pessoal e não substituem o acompanhamento clínico ou contracepção médica.",
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
