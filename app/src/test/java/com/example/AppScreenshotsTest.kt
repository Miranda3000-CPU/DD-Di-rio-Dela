package com.example

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import com.example.data.CycleEntity
import com.example.data.DailyLogEntity
import com.example.ui.CycleViewModel
import com.example.ui.components.CycleChart
import com.example.ui.components.CycleHistoryList
import com.example.ui.components.HeroPhaseCard
import com.example.ui.components.PredictionCards
import com.example.ui.components.UserNameHeader
import com.example.ui.components.VisualCalendar
import com.example.ui.theme.MeuCicloTheme
import com.example.util.CycleCalculator
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.UUID

/**
 * Screenshot tests generating clean, high-fidelity visual documentation.
 * ATTENTION: NEVER uses real user data. Only simulated, fictitious regular cycle data.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class AppScreenshotsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testToday: LocalDate = LocalDate.of(2026, 9, 19)

    // Fictitious simulated regular cycles for UI demonstration
    private val testCycles = listOf(
        CycleEntity(
            id = 4L,
            startDateEpochDay = LocalDate.of(2026, 9, 17).toEpochDay(),
            periodLengthDays = 5,
            flowIntensity = "MÉDIO",
            symptoms = "Cólicas leves",
            notes = "Início tranquilo",
            uuid = UUID.randomUUID().toString()
        ),
        CycleEntity(
            id = 3L,
            startDateEpochDay = LocalDate.of(2026, 8, 20).toEpochDay(),
            periodLengthDays = 5,
            flowIntensity = "INTENSO",
            symptoms = "Sensibilidade mamária",
            notes = "",
            uuid = UUID.randomUUID().toString()
        ),
        CycleEntity(
            id = 2L,
            startDateEpochDay = LocalDate.of(2026, 7, 23).toEpochDay(),
            periodLengthDays = 5,
            flowIntensity = "MÉDIO",
            symptoms = "",
            notes = "",
            uuid = UUID.randomUUID().toString()
        ),
        CycleEntity(
            id = 1L,
            startDateEpochDay = LocalDate.of(2026, 6, 25).toEpochDay(),
            periodLengthDays = 5,
            flowIntensity = "LEVE",
            symptoms = "Inchaço leve",
            notes = "",
            uuid = UUID.randomUUID().toString()
        )
    )

    private val testDailyLogs = listOf(
        DailyLogEntity(
            dateEpochDay = testToday.toEpochDay(),
            flowIntensity = "MÉDIO",
            painLevel = "Leve",
            symptoms = "Cólicas leves",
            mood = "Tranquila",
            energyLevel = "Boa",
            notes = "Dia calmo, tomando chá quente."
        ),
        DailyLogEntity(
            dateEpochDay = testToday.minusDays(1).toEpochDay(),
            flowIntensity = "INTENSO",
            painLevel = "Moderada",
            symptoms = "Cólicas moderadas",
            mood = "Sensível",
            energyLevel = "Baixa"
        )
    )

    private val prediction = CycleCalculator.calculatePrediction(testCycles, testToday)

    @Test
    fun screenshot_01_inicio_hero() {
        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UserNameHeader(
                            userName = "Giovanna",
                            onUpdateUserName = {}
                        )

                        HeroPhaseCard(
                            prediction = prediction,
                            onLogTodayClick = {},
                            onLogCustomClick = {}
                        )

                        PredictionCards(
                            prediction = prediction
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/01_inicio_hero.png")
    }

    @Test
    fun screenshot_02_calendario() {
        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        VisualCalendar(
                            cycles = testCycles,
                            dailyLogs = testDailyLogs,
                            prediction = prediction,
                            selectedDate = testToday,
                            selectedDateLog = testDailyLogs.firstOrNull(),
                            onDateSelected = {},
                            onLogDateClick = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/02_calendario.png")
    }

    @Test
    fun screenshot_03_historico_graficos() {
        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CycleChart(
                            cycles = testCycles,
                            averageCycleDays = prediction.averageCycleDays,
                            mlPredictedCycleDays = prediction.adaptiveCycleDays
                        )

                        CycleHistoryList(
                            cycles = testCycles,
                            averageCycleDays = prediction.averageCycleDays,
                            mlPredictedCycleDays = prediction.adaptiveCycleDays,
                            mlConfidencePercent = prediction.mlConfidencePercent,
                            onDeleteCycle = {}
                        )
                    }
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/03_historico_graficos.png")
    }

    @Test
    fun screenshot_04_ajustes_backup() {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = CycleViewModel(app)

        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    com.example.ui.components.SettingsScreen(
                        viewModel = viewModel,
                        userName = "Giovanna",
                        onUpdateUserName = {},
                        notificationsEnabled = true,
                        onToggleNotifications = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/04_ajustes_backup.png")
    }

    @Test
    fun screenshot_05_registrar_dia() {
        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    com.example.ui.components.LogPeriodSheetContent(
                        initialDate = testToday,
                        existingLog = testDailyLogs.firstOrNull(),
                        onDismiss = {},
                        onSave = { _, _, _, _, _, _, _, _, _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/05_registrar_dia.png")
    }

    @Test
    fun screenshot_05b_registrar_dia_detalhes() {
        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    com.example.ui.components.LogPeriodSheetContent(
                        initialDate = testToday,
                        existingLog = testDailyLogs.firstOrNull(),
                        onDismiss = {},
                        onSave = { _, _, _, _, _, _, _, _, _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNode(androidx.compose.ui.test.hasText("Sintomas Físicos"))
            .performScrollTo()

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/05b_registrar_dia_dor_sintomas.png")
    }

    @Test
    fun screenshot_05c_registrar_dia_bem_estar_notas() {
        composeTestRule.setContent {
            MeuCicloTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    com.example.ui.components.LogPeriodSheetContent(
                        initialDate = testToday,
                        existingLog = testDailyLogs.firstOrNull(),
                        onDismiss = {},
                        onSave = { _, _, _, _, _, _, _, _, _, _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNode(androidx.compose.ui.test.hasText("Observações & Notas"))
            .performScrollTo()

        composeTestRule.onRoot().captureRoboImage(filePath = "docs/screenshots/05c_registrar_dia_bem_estar_notas.png")
    }
}
