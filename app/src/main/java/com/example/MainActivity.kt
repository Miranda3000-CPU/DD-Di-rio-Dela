package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.NotificationHelper
import com.example.ui.CycleViewModel
import com.example.ui.components.CycleHistoryList
import com.example.ui.components.HeroPhaseCard
import com.example.ui.components.LogPeriodDialog
import com.example.ui.components.PredictionCards
import com.example.ui.components.VisualCalendar
import com.example.ui.components.PrivacyNoticeBanner
import com.example.ui.components.UserNameHeader
import com.example.ui.components.SettingsScreen
import com.example.ui.theme.MeuCicloTheme
import com.example.ui.theme.PeriodRosePrimary
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val viewModel: CycleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel on app start
        NotificationHelper.createNotificationChannel(applicationContext)

        setContent {
            MeuCicloTheme {
                MeuCicloApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeuCicloApp(viewModel: CycleViewModel) {
    val context = LocalContext.current

    val cycles by viewModel.cycles.collectAsStateWithLifecycle()
    val prediction by viewModel.prediction.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val selectedCalendarDate by viewModel.selectedCalendarDate.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val privacyNoticeDismissed by viewModel.privacyNoticeDismissed.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Início, 1: Calendário, 2: Histórico, 3: Configurações
    var showLogDialog by remember { mutableStateOf(false) }
    var logDialogInitialDate by remember { mutableStateOf(LocalDate.now()) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PeriodRosePrimary,
                        indicatorColor = PeriodRosePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendário") },
                    label = { Text("Calendário") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PeriodRosePrimary,
                        indicatorColor = PeriodRosePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_calendar")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Histórico") },
                    label = { Text("Histórico") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PeriodRosePrimary,
                        indicatorColor = PeriodRosePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_history")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configurações") },
                    label = { Text("Ajustes") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PeriodRosePrimary,
                        indicatorColor = PeriodRosePrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = {
                        logDialogInitialDate = LocalDate.now()
                        showLogDialog = true
                    },
                    containerColor = PeriodRosePrimary,
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    text = { Text("Registrar", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("fab_register")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    UserNameHeader(
                        userName = userName,
                        onUpdateUserName = { newName ->
                            viewModel.setUserName(newName)
                        }
                    )
                }

                if (!privacyNoticeDismissed && selectedTab == 0) {
                    item {
                        PrivacyNoticeBanner(
                            onDismiss = { viewModel.dismissPrivacyNotice() }
                        )
                    }
                }

                when (selectedTab) {
                    // TAB 0: Início - Somente o que deve ser registrado & status rápido
                    0 -> {
                        item {
                            HeroPhaseCard(
                                prediction = prediction,
                                onLogTodayClick = {
                                    logDialogInitialDate = LocalDate.now()
                                    showLogDialog = true
                                },
                                onLogCustomClick = {
                                    logDialogInitialDate = LocalDate.now()
                                    showLogDialog = true
                                }
                            )
                        }
                    }

                    // TAB 1: Calendário - Informações visuais completas sobre o ciclo
                    1 -> {
                        item {
                            VisualCalendar(
                                cycles = cycles,
                                prediction = prediction,
                                selectedDate = selectedCalendarDate,
                                onDateSelected = { date ->
                                    viewModel.setSelectedCalendarDate(date)
                                },
                                onLogDateClick = { date ->
                                    logDialogInitialDate = date
                                    showLogDialog = true
                                }
                            )
                        }

                        item {
                            PredictionCards(prediction = prediction)
                        }
                    }

                    // TAB 2: Histórico - Informações armazenadas locais com gráfico linear
                    2 -> {
                        item {
                            CycleHistoryList(
                                cycles = cycles,
                                averageCycleDays = prediction.averageCycleDays,
                                mlPredictedCycleDays = prediction.mlPredictedCycleDays,
                                mlConfidencePercent = prediction.mlConfidencePercent,
                                onDeleteCycle = { id ->
                                    viewModel.deleteCycle(id)
                                }
                            )
                        }
                    }

                    // TAB 3: Configurações - Personalização e ajustes do app
                    3 -> {
                        item {
                            SettingsScreen(
                                userName = userName,
                                onUpdateUserName = { newName ->
                                    viewModel.setUserName(newName)
                                },
                                notificationsEnabled = notificationsEnabled,
                                onToggleNotifications = { enabled ->
                                    viewModel.toggleNotifications(enabled, context)
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showLogDialog) {
        LogPeriodDialog(
            initialDate = logDialogInitialDate,
            onDismiss = { showLogDialog = false },
            onSave = { startDate, periodLengthDays, flowIntensity, symptoms, notes ->
                viewModel.registerPeriodOnDate(
                    startDate = startDate,
                    periodLengthDays = periodLengthDays,
                    flowIntensity = flowIntensity,
                    symptoms = symptoms,
                    notes = notes
                )
                showLogDialog = false
            }
        )
    }
}
