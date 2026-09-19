package com.example.backup

import android.content.Context
import com.example.data.CycleEntity
import com.example.data.CycleRepository
import com.example.data.DailyLogEntity
import com.example.util.UserPreferencesManager

object BackupManager {

    const val BACKUP_FILE_EXTENSION = "ddbackup"
    const val DEFAULT_BACKUP_FILENAME = "diario-dela.ddbackup"
    const val CURRENT_FORMAT_VERSION = 1

    suspend fun createBackup(
        context: Context,
        repository: CycleRepository,
        password: String? = null
    ): ByteArray {
        val cycles = repository.getAllCyclesSync()
        val dailyLogs = repository.getAllDailyLogsSync()
        val prefs = UserPreferencesManager.getAllPreferencesMap(context)
        val userName = UserPreferencesManager.getUserName(context)

        val cycleDtos = cycles.map {
            CycleBackupDto(
                uuid = it.uuid,
                startDateEpochDay = it.startDateEpochDay,
                periodLengthDays = it.periodLengthDays,
                flowIntensity = it.flowIntensity,
                symptoms = it.symptoms,
                notes = it.notes,
                createdAtEpochMilli = it.createdAtEpochMilli,
                updatedAtEpochMilli = it.updatedAtEpochMilli
            )
        }

        val logDtos = dailyLogs.map {
            DailyLogBackupDto(
                uuid = it.uuid,
                dateEpochDay = it.dateEpochDay,
                flowIntensity = it.flowIntensity,
                painLevel = it.painLevel,
                symptoms = it.symptoms,
                mood = it.mood,
                energyLevel = it.energyLevel,
                sleepQuality = it.sleepQuality,
                cervicalMucus = it.cervicalMucus,
                notes = it.notes,
                createdAtEpochMilli = it.createdAtEpochMilli,
                updatedAtEpochMilli = it.updatedAtEpochMilli
            )
        }

        val payload = BackupPayload(
            formatVersion = CURRENT_FORMAT_VERSION,
            backupTag = "DD_BACKUP_V1",
            applicationId = context.packageName,
            appVersionCode = 5,
            appVersionName = "5.0",
            exportedAtEpochMilli = System.currentTimeMillis(),
            userName = userName,
            cycles = cycleDtos,
            dailyLogs = logDtos,
            preferences = prefs
        )

        val jsonString = payload.toJsonString()

        return if (!password.isNullOrBlank()) {
            BackupCrypto.encrypt(jsonString, password)
        } else {
            BackupCrypto.packPlain(jsonString)
        }
    }

    fun inspectBackup(bytes: ByteArray, password: String? = null): BackupPreview {
        val isEncrypted = BackupCrypto.isEncrypted(bytes)
        val jsonString = if (isEncrypted) {
            if (password.isNullOrBlank()) {
                throw PasswordRequiredException("Este arquivo de backup está protegido por senha.")
            }
            try {
                BackupCrypto.decrypt(bytes, password)
            } catch (e: Exception) {
                throw InvalidPasswordException("Senha incorreta ou arquivo corrompido.")
            }
        } else {
            BackupCrypto.unpackPlain(bytes)
        }

        val payload = BackupPayload.fromJsonString(jsonString)
        return BackupPreview(
            formatVersion = payload.formatVersion,
            applicationId = payload.applicationId,
            appVersionCode = payload.appVersionCode,
            appVersionName = payload.appVersionName,
            exportedAtEpochMilli = payload.exportedAtEpochMilli,
            userName = payload.userName,
            totalCycles = payload.cycles.size,
            totalDailyLogs = payload.dailyLogs.size,
            isEncrypted = isEncrypted
        )
    }

    suspend fun restoreBackup(
        context: Context,
        repository: CycleRepository,
        bytes: ByteArray,
        password: String? = null
    ): RestoreResult {
        val isEncrypted = BackupCrypto.isEncrypted(bytes)
        val jsonString = if (isEncrypted) {
            if (password.isNullOrBlank()) {
                throw PasswordRequiredException("Este arquivo de backup está protegido por senha.")
            }
            try {
                BackupCrypto.decrypt(bytes, password)
            } catch (e: Exception) {
                throw InvalidPasswordException("Senha incorreta ou arquivo corrompido.")
            }
        } else {
            BackupCrypto.unpackPlain(bytes)
        }

        val payload = BackupPayload.fromJsonString(jsonString)
        if (payload.formatVersion > CURRENT_FORMAT_VERSION) {
            throw IncompatibleVersionException("A versão deste backup (${payload.formatVersion}) é mais recente que a versão suportada pelo app ($CURRENT_FORMAT_VERSION).")
        }

        // 1. Fetch current database state for safe merge
        val existingCycles = repository.getAllCyclesSync()
        val existingCyclesByUuid = existingCycles.associateBy { it.uuid }
        val existingCyclesByDate = existingCycles.associateBy { it.startDateEpochDay }

        var addedCycles = 0
        var updatedCycles = 0
        var preservedCycles = 0

        // 2. Safe merge of cycles (NEVER DELETE LOCAL DATA)
        for (incoming in payload.cycles) {
            val matchByUuid = existingCyclesByUuid[incoming.uuid]
            val matchByDate = existingCyclesByDate[incoming.startDateEpochDay]
            val existing = matchByUuid ?: matchByDate

            if (existing != null) {
                if (incoming.updatedAtEpochMilli > existing.updatedAtEpochMilli) {
                    val updated = existing.copy(
                        startDateEpochDay = incoming.startDateEpochDay,
                        periodLengthDays = incoming.periodLengthDays,
                        flowIntensity = incoming.flowIntensity,
                        symptoms = incoming.symptoms,
                        notes = incoming.notes,
                        updatedAtEpochMilli = incoming.updatedAtEpochMilli
                    )
                    repository.updateCycle(updated)
                    updatedCycles++
                } else {
                    preservedCycles++
                }
            } else {
                val newEntity = CycleEntity(
                    uuid = incoming.uuid,
                    startDateEpochDay = incoming.startDateEpochDay,
                    periodLengthDays = incoming.periodLengthDays,
                    flowIntensity = incoming.flowIntensity,
                    symptoms = incoming.symptoms,
                    notes = incoming.notes,
                    createdAtEpochMilli = incoming.createdAtEpochMilli,
                    updatedAtEpochMilli = incoming.updatedAtEpochMilli
                )
                repository.insertCycle(newEntity)
                addedCycles++
            }
        }

        // 3. Safe merge of daily logs
        val existingLogs = repository.getAllDailyLogsSync()
        val existingLogsByUuid = existingLogs.associateBy { it.uuid }
        val existingLogsByDate = existingLogs.associateBy { it.dateEpochDay }

        var addedLogs = 0
        var updatedLogs = 0
        var preservedLogs = 0

        for (incoming in payload.dailyLogs) {
            val matchByUuid = existingLogsByUuid[incoming.uuid]
            val matchByDate = existingLogsByDate[incoming.dateEpochDay]
            val existing = matchByUuid ?: matchByDate

            if (existing != null) {
                if (incoming.updatedAtEpochMilli > existing.updatedAtEpochMilli) {
                    val updated = existing.copy(
                        flowIntensity = incoming.flowIntensity,
                        painLevel = incoming.painLevel,
                        symptoms = incoming.symptoms,
                        mood = incoming.mood,
                        energyLevel = incoming.energyLevel,
                        sleepQuality = incoming.sleepQuality,
                        cervicalMucus = incoming.cervicalMucus,
                        notes = incoming.notes,
                        updatedAtEpochMilli = incoming.updatedAtEpochMilli
                    )
                    repository.updateDailyLog(updated)
                    updatedLogs++
                } else {
                    preservedLogs++
                }
            } else {
                val newEntity = DailyLogEntity(
                    uuid = incoming.uuid,
                    dateEpochDay = incoming.dateEpochDay,
                    flowIntensity = incoming.flowIntensity,
                    painLevel = incoming.painLevel,
                    symptoms = incoming.symptoms,
                    mood = incoming.mood,
                    energyLevel = incoming.energyLevel,
                    sleepQuality = incoming.sleepQuality,
                    cervicalMucus = incoming.cervicalMucus,
                    notes = incoming.notes,
                    createdAtEpochMilli = incoming.createdAtEpochMilli,
                    updatedAtEpochMilli = incoming.updatedAtEpochMilli
                )
                repository.insertDailyLog(newEntity)
                addedLogs++
            }
        }

        // 4. Restore preferences safely
        if (payload.preferences.isNotEmpty()) {
            UserPreferencesManager.restorePreferencesMap(context, payload.preferences)
        }
        if (payload.userName.isNotBlank()) {
            UserPreferencesManager.setUserName(context, payload.userName)
        }

        return RestoreResult(
            success = true,
            message = "Restauração mesclada com sucesso!",
            addedCycles = addedCycles,
            updatedCycles = updatedCycles,
            preservedCycles = preservedCycles,
            addedDailyLogs = addedLogs,
            updatedDailyLogs = updatedLogs,
            preservedDailyLogs = preservedLogs
        )
    }
}

class PasswordRequiredException(message: String) : Exception(message)
class InvalidPasswordException(message: String) : Exception(message)
class IncompatibleVersionException(message: String) : Exception(message)
