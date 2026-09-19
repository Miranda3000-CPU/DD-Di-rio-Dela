package com.example.backup

import org.json.JSONArray
import org.json.JSONObject

data class CycleBackupDto(
    val uuid: String,
    val startDateEpochDay: Long,
    val periodLengthDays: Int,
    val flowIntensity: String,
    val symptoms: String,
    val notes: String,
    val createdAtEpochMilli: Long,
    val updatedAtEpochMilli: Long
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("uuid", uuid)
            put("startDateEpochDay", startDateEpochDay)
            put("periodLengthDays", periodLengthDays)
            put("flowIntensity", flowIntensity)
            put("symptoms", symptoms)
            put("notes", notes)
            put("createdAtEpochMilli", createdAtEpochMilli)
            put("updatedAtEpochMilli", updatedAtEpochMilli)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CycleBackupDto {
            return CycleBackupDto(
                uuid = json.optString("uuid", java.util.UUID.randomUUID().toString()),
                startDateEpochDay = json.getLong("startDateEpochDay"),
                periodLengthDays = json.optInt("periodLengthDays", 5),
                flowIntensity = json.optString("flowIntensity", "MÉDIO"),
                symptoms = json.optString("symptoms", ""),
                notes = json.optString("notes", ""),
                createdAtEpochMilli = json.optLong("createdAtEpochMilli", System.currentTimeMillis()),
                updatedAtEpochMilli = json.optLong("updatedAtEpochMilli", System.currentTimeMillis())
            )
        }
    }
}

data class DailyLogBackupDto(
    val uuid: String,
    val dateEpochDay: Long,
    val flowIntensity: String?,
    val painLevel: String?,
    val symptoms: String,
    val mood: String?,
    val energyLevel: String?,
    val sleepQuality: String?,
    val cervicalMucus: String?,
    val notes: String,
    val createdAtEpochMilli: Long,
    val updatedAtEpochMilli: Long
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("uuid", uuid)
            put("dateEpochDay", dateEpochDay)
            if (flowIntensity != null) put("flowIntensity", flowIntensity)
            if (painLevel != null) put("painLevel", painLevel)
            put("symptoms", symptoms)
            if (mood != null) put("mood", mood)
            if (energyLevel != null) put("energyLevel", energyLevel)
            if (sleepQuality != null) put("sleepQuality", sleepQuality)
            if (cervicalMucus != null) put("cervicalMucus", cervicalMucus)
            put("notes", notes)
            put("createdAtEpochMilli", createdAtEpochMilli)
            put("updatedAtEpochMilli", updatedAtEpochMilli)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): DailyLogBackupDto {
            return DailyLogBackupDto(
                uuid = json.optString("uuid", java.util.UUID.randomUUID().toString()),
                dateEpochDay = json.getLong("dateEpochDay"),
                flowIntensity = if (json.has("flowIntensity")) json.optString("flowIntensity") else null,
                painLevel = if (json.has("painLevel")) json.optString("painLevel") else null,
                symptoms = json.optString("symptoms", ""),
                mood = if (json.has("mood")) json.optString("mood") else null,
                energyLevel = if (json.has("energyLevel")) json.optString("energyLevel") else null,
                sleepQuality = if (json.has("sleepQuality")) json.optString("sleepQuality") else null,
                cervicalMucus = if (json.has("cervicalMucus")) json.optString("cervicalMucus") else null,
                notes = json.optString("notes", ""),
                createdAtEpochMilli = json.optLong("createdAtEpochMilli", System.currentTimeMillis()),
                updatedAtEpochMilli = json.optLong("updatedAtEpochMilli", System.currentTimeMillis())
            )
        }
    }
}

data class BackupPayload(
    val formatVersion: Int = 1,
    val backupTag: String = "DD_BACKUP_V1",
    val applicationId: String,
    val appVersionCode: Int,
    val appVersionName: String,
    val exportedAtEpochMilli: Long,
    val userName: String,
    val cycles: List<CycleBackupDto>,
    val dailyLogs: List<DailyLogBackupDto>,
    val preferences: Map<String, String>
) {
    fun toJsonString(): String {
        val root = JSONObject().apply {
            put("formatVersion", formatVersion)
            put("backupTag", backupTag)
            put("applicationId", applicationId)
            put("appVersionCode", appVersionCode)
            put("appVersionName", appVersionName)
            put("exportedAtEpochMilli", exportedAtEpochMilli)
            put("userName", userName)

            val cyclesArray = JSONArray()
            cycles.forEach { cyclesArray.put(it.toJson()) }
            put("cycles", cyclesArray)

            val logsArray = JSONArray()
            dailyLogs.forEach { logsArray.put(it.toJson()) }
            put("dailyLogs", logsArray)

            val prefsObj = JSONObject()
            preferences.forEach { (k, v) -> prefsObj.put(k, v) }
            put("preferences", prefsObj)
        }
        return root.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String): BackupPayload {
            val root = JSONObject(jsonStr)
            val formatVersion = root.optInt("formatVersion", 1)
            val backupTag = root.optString("backupTag", "DD_BACKUP_V1")
            val applicationId = root.optString("applicationId", "")
            val appVersionCode = root.optInt("appVersionCode", 1)
            val appVersionName = root.optString("appVersionName", "1.0")
            val exportedAtEpochMilli = root.optLong("exportedAtEpochMilli", System.currentTimeMillis())
            val userName = root.optString("userName", "Giovanna")

            val cyclesList = mutableListOf<CycleBackupDto>()
            val cyclesArray = root.optJSONArray("cycles")
            if (cyclesArray != null) {
                for (i in 0 until cyclesArray.length()) {
                    cyclesList.add(CycleBackupDto.fromJson(cyclesArray.getJSONObject(i)))
                }
            }

            val logsList = mutableListOf<DailyLogBackupDto>()
            val logsArray = root.optJSONArray("dailyLogs")
            if (logsArray != null) {
                for (i in 0 until logsArray.length()) {
                    logsList.add(DailyLogBackupDto.fromJson(logsArray.getJSONObject(i)))
                }
            }

            val prefsMap = mutableMapOf<String, String>()
            val prefsObj = root.optJSONObject("preferences")
            if (prefsObj != null) {
                val keys = prefsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    prefsMap[key] = prefsObj.getString(key)
                }
            }

            return BackupPayload(
                formatVersion = formatVersion,
                backupTag = backupTag,
                applicationId = applicationId,
                appVersionCode = appVersionCode,
                appVersionName = appVersionName,
                exportedAtEpochMilli = exportedAtEpochMilli,
                userName = userName,
                cycles = cyclesList,
                dailyLogs = logsList,
                preferences = prefsMap
            )
        }
    }
}

data class BackupPreview(
    val formatVersion: Int,
    val applicationId: String,
    val appVersionCode: Int,
    val appVersionName: String,
    val exportedAtEpochMilli: Long,
    val userName: String,
    val totalCycles: Int,
    val totalDailyLogs: Int,
    val isEncrypted: Boolean
)

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val addedCycles: Int = 0,
    val updatedCycles: Int = 0,
    val preservedCycles: Int = 0,
    val addedDailyLogs: Int = 0,
    val updatedDailyLogs: Int = 0,
    val preservedDailyLogs: Int = 0
)
