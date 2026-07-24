package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CyclePhase
import com.example.model.CyclePrediction
import com.example.ui.theme.FertileBluePrimary
import com.example.ui.theme.FollicularGreen
import com.example.ui.theme.LutealAmber
import com.example.ui.theme.OvulationPurplePrimary
import com.example.ui.theme.PeriodRosePrimary
import java.time.format.DateTimeFormatter

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun HeroPhaseCard(
    prediction: CyclePrediction,
    onLogTodayClick: () -> Unit,
    onLogCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val phaseColor = when (prediction.currentPhase) {
        CyclePhase.MENSTRUAL -> PeriodRosePrimary
        CyclePhase.FOLLICULAR -> FollicularGreen
        CyclePhase.FERTILE -> FertileBluePrimary
        CyclePhase.OVULATION -> OvulationPurplePrimary
        CyclePhase.LUTEAL -> PeriodRosePrimary
        null -> PeriodRosePrimary
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color.White,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_phase_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGradient)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Phase badge
                prediction.currentPhase?.let { phase ->
                    Surface(
                        color = phaseColor.copy(alpha = 0.12f),
                        shape = CircleShape,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(phaseColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = phase.displayName.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = phaseColor,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // Countdown / Status main display with Circular Ring
                if (prediction.totalLoggedCycles == 0) {
                    Text(
                        text = "Bem-vinda ao DD",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Registre sua última menstruação para calcular suas previsões com alta precisão.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val days = prediction.daysUntilNextPeriod ?: 0
                    val avgDays = prediction.mlPredictedCycleDays.toFloat().coerceAtLeast(1f)
                    val progressFraction = if (days > 0) {
                        ((avgDays - days) / avgDays).coerceIn(0.1f, 1f)
                    } else 1f

                    // Radial Circular Progress Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(170.dp)
                            .padding(8.dp)
                    ) {
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 12.dp.toPx()
                            // Background circle track
                            drawCircle(
                                color = trackColor,
                                style = Stroke(width = strokeWidth)
                            )
                            // Progress arc
                            drawArc(
                                color = phaseColor,
                                startAngle = -90f,
                                sweepAngle = 360f * progressFraction,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (days > 0) {
                                Text(
                                    text = "$days",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (days == 1) "dia resta" else "dias restam",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (days == 0) {
                                Text(
                                    text = "Hoje",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PeriodRosePrimary
                                )
                                Text(
                                    text = "Início previsto",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    text = "+${Math.abs(days)}d",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PeriodRosePrimary
                                )
                                Text(
                                    text = "Atraso",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PeriodRosePrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Next period target date
                    prediction.nextPeriodStart?.let { nextDate ->
                        Text(
                            text = "Sua próxima menstruação deve começar em ${nextDate.format(DateTimeFormatter.ofPattern("dd 'de' MMMM"))}.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onLogTodayClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("log_today_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeriodRosePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registrar menstruação hoje",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onLogCustomClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("log_custom_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registrar outra data",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
