package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backup.BackupManager
import com.example.backup.BackupPreview
import com.example.backup.RestoreResult
import com.example.data.AppDatabase
import com.example.data.CycleEntity
import com.example.data.CycleRepository
import com.example.data.DailyLogEntity
import com.example.model.CyclePrediction
import com.example.notification.NotificationHelper
import com.example.util.CycleCalculator
import com.example.util.UserPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class CycleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CycleRepository

    val cycles: StateFlow<List<CycleEntity>>
    val dailyLogs: StateFlow<List<DailyLogEntity>>
    val prediction: StateFlow<CyclePrediction>

    private val _selectedCalendarDate = MutableStateFlow(LocalDate.now())
    val selectedCalendarDate: StateFlow<LocalDate> = _selectedCalendarDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedDateLog: StateFlow<DailyLogEntity?> = _selectedCalendarDate
        .flatMapLatest { date -> repository.getDailyLogForDate(date.toEpochDay()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _userName = MutableStateFlow(UserPreferencesManager.getUserName(application))
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(UserPreferencesManager.isNotificationsEnabled(application))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _privateNotifications = MutableStateFlow(UserPreferencesManager.isPrivateNotifications(application))
    val privateNotifications: StateFlow<Boolean> = _privateNotifications.asStateFlow()

    private val _privacyNoticeDismissed = MutableStateFlow(UserPreferencesManager.isPrivacyNoticeDismissed(application))
    val privacyNoticeDismissed: StateFlow<Boolean> = _privacyNoticeDismissed.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CycleRepository(db.cycleDao(), db.dailyLogDao())

        cycles = repository.allCycles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        dailyLogs = repository.allDailyLogs.stateIn(
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
        val cleanName = if (name.isBlank()) UserPreferencesManager.DEFAULT_USER_NAME else name.trim()
        _userName.value = cleanName
        UserPreferencesManager.setUserName(getApplication(), cleanName)
    }

    fun dismissPrivacyNotice() {
        _privacyNoticeDismissed.value = true
        UserPreferencesManager.setPrivacyNoticeDismissed(getApplication(), true)
    }

    fun setSelectedCalendarDate(date: LocalDate) {
        _selectedCalendarDate.value = date
    }

    fun toggleNotifications(enabled: Boolean, context: Context) {
        _notificationsEnabled.value = enabled
        UserPreferencesManager.setNotificationsEnabled(context, enabled)
        if (enabled) {
            NotificationHelper.scheduleNextPeriodReminder(
                context,
                prediction.value.nextPeriodStart
            )
        }
    }

    fun setPrivateNotifications(isPrivate: Boolean) {
        _privateNotifications.value = isPrivate
        UserPreferencesManager.setPrivateNotifications(getApplication(), isPrivate)
    }

    fun sendTestNotification(context: Context) {
        NotificationHelper.sendTestNotification(context)
    }

    fun registerPeriodOnDate(
        startDate: LocalDate,
        periodLengthDays: Int = 5,
        flowIntensity: String = "MÉDIO",
        symptoms: String = "",
        notes: String = ""
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val cycleEntity = CycleEntity(
                uuid = UUID.randomUUID().toString(),
                startDateEpochDay = startDate.toEpochDay(),
                periodLengthDays = periodLengthDays,
                flowIntensity = flowIntensity,
                symptoms = symptoms,
                notes = notes,
                createdAtEpochMilli = now,
                updatedAtEpochMilli = now
            )
            repository.insertCycle(cycleEntity)

            // Also ensure a daily log entry for that start date exists
            val existingLog = repository.getDailyLogForDateSync(startDate.toEpochDay())
            if (existingLog == null) {
                val dailyLog = DailyLogEntity(
                    uuid = UUID.randomUUID().toString(),
                    dateEpochDay = startDate.toEpochDay(),
                    flowIntensity = flowIntensity,
                    symptoms = symptoms,
                    notes = notes,
                    createdAtEpochMilli = now,
                    updatedAtEpochMilli = now
                )
                repository.insertDailyLog(dailyLog)
            } else {
                val updated = existingLog.copy(
                    flowIntensity = flowIntensity,
                    symptoms = if (symptoms.isNotBlank()) symptoms else existingLog.symptoms,
                    notes = if (notes.isNotBlank()) notes else existingLog.notes,
                    updatedAtEpochMilli = now
                )
                repository.updateDailyLog(updated)
            }
        }
    }

    fun saveDailyLog(
        date: LocalDate,
        flowIntensity: String?,
        painLevel: String?,
        symptoms: String,
        mood: String?,
        energyLevel: String?,
        sleepQuality: String?,
        cervicalMucus: String?,
        notes: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repository.getDailyLogForDateSync(date.toEpochDay())
            if (existing != null) {
                val updated = existing.copy(
                    flowIntensity = flowIntensity,
                    painLevel = painLevel,
                    symptoms = symptoms,
                    mood = mood,
                    energyLevel = energyLevel,
                    sleepQuality = sleepQuality,
                    cervicalMucus = cervicalMucus,
                    notes = notes,
                    updatedAtEpochMilli = now
                )
                repository.updateDailyLog(updated)
            } else {
                val newLog = DailyLogEntity(
                    uuid = UUID.randomUUID().toString(),
                    dateEpochDay = date.toEpochDay(),
                    flowIntensity = flowIntensity,
                    painLevel = painLevel,
                    symptoms = symptoms,
                    mood = mood,
                    energyLevel = energyLevel,
                    sleepQuality = sleepQuality,
                    cervicalMucus = cervicalMucus,
                    notes = notes,
                    createdAtEpochMilli = now,
                    updatedAtEpochMilli = now
                )
                repository.insertDailyLog(newLog)
            }
        }
    }

    fun deleteCycle(id: Long) {
        viewModelScope.launch {
            repository.deleteCycle(id)
        }
    }

    fun deleteDailyLog(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyLog(id)
        }
    }

    suspend fun createBackup(password: String? = null): ByteArray {
        return BackupManager.createBackup(getApplication(), repository, password)
    }

    fun inspectBackup(bytes: ByteArray, password: String? = null): BackupPreview {
        return BackupManager.inspectBackup(bytes, password)
    }

    suspend fun restoreBackup(bytes: ByteArray, password: String? = null): RestoreResult {
        val result = BackupManager.restoreBackup(getApplication(), repository, bytes, password)
        _userName.value = UserPreferencesManager.getUserName(getApplication())
        _notificationsEnabled.value = UserPreferencesManager.isNotificationsEnabled(getApplication())
        _privateNotifications.value = UserPreferencesManager.isPrivateNotifications(getApplication())
        return result
    }
}
