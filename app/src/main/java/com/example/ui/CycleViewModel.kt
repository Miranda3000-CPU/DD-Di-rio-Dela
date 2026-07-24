package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CycleEntity
import com.example.data.CycleRepository
import com.example.model.CyclePrediction
import com.example.notification.NotificationHelper
import com.example.util.CycleCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.example.util.UserPreferencesManager

class CycleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CycleRepository

    val cycles: StateFlow<List<CycleEntity>>

    val prediction: StateFlow<CyclePrediction>

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    private val _userName = MutableStateFlow(UserPreferencesManager.getUserName(application))
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _privacyNoticeDismissed = MutableStateFlow(UserPreferencesManager.isPrivacyNoticeDismissed(application))
    val privacyNoticeDismissed: StateFlow<Boolean> = _privacyNoticeDismissed.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).cycleDao()
        repository = CycleRepository(dao)

        cycles = repository.allCycles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        prediction = repository.allCycles.map { cycleList ->
            val pred = CycleCalculator.calculatePrediction(cycleList)
            if (_notificationsEnabled.value) {
                NotificationHelper.scheduleNextPeriodReminder(
                    getApplication(),
                    pred.nextPeriodStart
                )
            }
            pred
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CyclePrediction()
        )
    }

    fun setUserName(name: String) {
        _userName.value = name
        UserPreferencesManager.setUserName(getApplication(), name)
    }

    fun dismissPrivacyNotice() {
        _privacyNoticeDismissed.value = true
        UserPreferencesManager.setPrivacyNoticeDismissed(getApplication(), true)
    }

    fun setSelectedCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    fun registerPeriodToday(
        periodLengthDays: Int = 5,
        flowIntensity: String = "MÉDIO",
        symptoms: String = "",
        notes: String = ""
    ) {
        registerPeriodOnDate(
            startDate = LocalDate.now(),
            periodLengthDays = periodLengthDays,
            flowIntensity = flowIntensity,
            symptoms = symptoms,
            notes = notes
        )
    }

    fun registerPeriodOnDate(
        startDate: LocalDate,
        periodLengthDays: Int = 5,
        flowIntensity: String = "MÉDIO",
        symptoms: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val entity = CycleEntity(
                startDateEpochDay = startDate.toEpochDay(),
                periodLengthDays = periodLengthDays,
                flowIntensity = flowIntensity,
                symptoms = symptoms,
                notes = notes
            )
            repository.insertCycle(entity)
        }
    }

    fun deleteCycle(id: Long) {
        viewModelScope.launch {
            repository.deleteCycle(id)
        }
    }

    fun toggleNotifications(enabled: Boolean, context: Context) {
        _notificationsEnabled.value = enabled
        if (enabled) {
            NotificationHelper.scheduleNextPeriodReminder(
                context,
                prediction.value.nextPeriodStart
            )
        }
    }
}
