package com.example.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.AppDatabase
import com.example.data.CycleEntity
import com.example.data.CycleRepository
import com.example.data.DailyLogEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class BackupManagerTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: CycleRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CycleRepository(db.cycleDao(), db.dailyLogDao())
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testExportAndImportPlainBackup() = runTest {
        // Insert sample cycles and daily logs
        val cycle = CycleEntity(
            uuid = UUID.randomUUID().toString(),
            startDateEpochDay = 19800,
            periodLengthDays = 5,
            flowIntensity = "MÉDIO",
            symptoms = "Cólica",
            notes = "Notas de teste"
        )
        repository.insertCycle(cycle)

        val log = DailyLogEntity(
            uuid = UUID.randomUUID().toString(),
            dateEpochDay = 19800,
            flowIntensity = "MÉDIO",
            symptoms = "Cólica",
            mood = "Tranquila",
            notes = "Dia calmo"
        )
        repository.insertDailyLog(log)

        // Export
        val backupBytes = BackupManager.createBackup(context, repository, password = null)
        assertNotNull(backupBytes)
        assertTrue(BackupCrypto.isPlain(backupBytes))

        // Inspect preview
        val preview = BackupManager.inspectBackup(backupBytes, password = null)
        assertEquals(1, preview.totalCycles)
        assertEquals(1, preview.totalDailyLogs)
        assertFalse(preview.isEncrypted)

        // Clear local database to simulate fresh restore
        db.clearAllTables()
        assertEquals(0, repository.getAllCyclesSync().size)
        assertEquals(0, repository.getAllDailyLogsSync().size)

        // Restore
        val result = BackupManager.restoreBackup(context, repository, backupBytes, password = null)
        assertTrue(result.success)
        assertEquals(1, result.addedCycles)
        assertEquals(1, result.addedDailyLogs)

        val restoredCycles = repository.getAllCyclesSync()
        assertEquals(1, restoredCycles.size)
        assertEquals(cycle.startDateEpochDay, restoredCycles[0].startDateEpochDay)
        assertEquals(cycle.symptoms, restoredCycles[0].symptoms)

        val restoredLogs = repository.getAllDailyLogsSync()
        assertEquals(1, restoredLogs.size)
        assertEquals(log.dateEpochDay, restoredLogs[0].dateEpochDay)
        assertEquals(log.mood, restoredLogs[0].mood)
    }

    @Test
    fun testPasswordProtectedBackupAndRestore() = runTest {
        val cycle = CycleEntity(
            uuid = UUID.randomUUID().toString(),
            startDateEpochDay = 19820,
            periodLengthDays = 4,
            flowIntensity = "LEVE",
            symptoms = "Sensibilidade",
            notes = "Com senha"
        )
        repository.insertCycle(cycle)

        val password = "SenhaForteSeguraDaGiovanna123!"

        // Export encrypted
        val backupBytes = BackupManager.createBackup(context, repository, password = password)
        assertTrue(BackupCrypto.isEncrypted(backupBytes))

        // Inspect preview requires password
        try {
            BackupManager.inspectBackup(backupBytes, password = null)
            org.junit.Assert.fail("Deveria lançar PasswordRequiredException")
        } catch (e: PasswordRequiredException) {
            // Expected
        }

        // Wrong password throws InvalidPasswordException
        try {
            BackupManager.inspectBackup(backupBytes, password = "senha_errada")
            org.junit.Assert.fail("Deveria lançar InvalidPasswordException")
        } catch (e: InvalidPasswordException) {
            // Expected
        }

        // Correct password succeeds
        val preview = BackupManager.inspectBackup(backupBytes, password = password)
        assertEquals(1, preview.totalCycles)
        assertTrue(preview.isEncrypted)

        // Clear local DB and restore
        db.clearAllTables()
        val result = BackupManager.restoreBackup(context, repository, backupBytes, password = password)
        assertTrue(result.success)
        assertEquals(1, result.addedCycles)

        val restored = repository.getAllCyclesSync()
        assertEquals(1, restored.size)
        assertEquals("Com senha", restored[0].notes)
    }

    @Test
    fun testMergeDoesNotDeleteLocalRecordsAndResolvesConflicts() = runTest {
        // Local state has an existing cycle with timestamp 1000
        val sharedUuid = UUID.randomUUID().toString()
        val localCycle = CycleEntity(
            uuid = sharedUuid,
            startDateEpochDay = 19850,
            periodLengthDays = 5,
            flowIntensity = "MÉDIO",
            notes = "Versão Local Mais Antiga",
            createdAtEpochMilli = 1000,
            updatedAtEpochMilli = 1000
        )
        repository.insertCycle(localCycle)

        // Also local has an exclusive cycle
        val exclusiveLocalCycle = CycleEntity(
            uuid = UUID.randomUUID().toString(),
            startDateEpochDay = 19880,
            periodLengthDays = 4,
            notes = "Dado exclusivo do aparelho"
        )
        repository.insertCycle(exclusiveLocalCycle)

        // Backup has an updated version of sharedUuid (timestamp 2000) AND a new incoming cycle
        val backupPayload = BackupPayload(
            formatVersion = 1,
            applicationId = context.packageName,
            appVersionCode = 5,
            appVersionName = "5.0",
            exportedAtEpochMilli = 2000,
            userName = "Giovanna",
            cycles = listOf(
                CycleBackupDto(
                    uuid = sharedUuid,
                    startDateEpochDay = 19850,
                    periodLengthDays = 5,
                    flowIntensity = "INTENSO",
                    symptoms = "Cólica forte",
                    notes = "Versão Atualizada do Backup",
                    createdAtEpochMilli = 1000,
                    updatedAtEpochMilli = 2000
                ),
                CycleBackupDto(
                    uuid = UUID.randomUUID().toString(),
                    startDateEpochDay = 19910,
                    periodLengthDays = 5,
                    flowIntensity = "LEVE",
                    symptoms = "",
                    notes = "Novo ciclo do backup",
                    createdAtEpochMilli = 2000,
                    updatedAtEpochMilli = 2000
                )
            ),
            dailyLogs = emptyList(),
            preferences = emptyMap()
        )

        val backupBytes = BackupCrypto.packPlain(backupPayload.toJsonString())

        // Perform restore (MERGE)
        val result = BackupManager.restoreBackup(context, repository, backupBytes, password = null)
        assertTrue(result.success)
        assertEquals(1, result.addedCycles) // The new 19910 cycle
        assertEquals(1, result.updatedCycles) // The updated sharedUuid cycle
        assertEquals(0, result.preservedCycles)

        // Verify total cycles in local DB: must have 3! Local exclusive MUST be kept!
        val allLocal = repository.getAllCyclesSync()
        assertEquals(3, allLocal.size)

        // Verify local exclusive is preserved
        val foundExclusive = allLocal.find { it.uuid == exclusiveLocalCycle.uuid }
        assertNotNull(foundExclusive)
        assertEquals("Dado exclusivo do aparelho", foundExclusive?.notes)

        // Verify shared cycle was safely updated because backup had newer updatedAt
        val foundUpdated = allLocal.find { it.uuid == sharedUuid }
        assertNotNull(foundUpdated)
        assertEquals("Versão Atualizada do Backup", foundUpdated?.notes)
        assertEquals("INTENSO", foundUpdated?.flowIntensity)
    }

    @Test
    fun testCorruptedBackupThrowsException() {
        val corruptedBytes = "DDBACKUP_V1_PLAIN\n{invalid_json".toByteArray(Charsets.UTF_8)
        try {
            BackupManager.inspectBackup(corruptedBytes)
            org.junit.Assert.fail("Deveria falhar ao analisar backup com JSON corrompido")
        } catch (e: Exception) {
            // Expected
        }
    }

    @Test
    fun testIncompatibleFutureVersionThrowsException() = runTest {
        val futurePayload = BackupPayload(
            formatVersion = 99, // Future unsupported version
            applicationId = context.packageName,
            appVersionCode = 99,
            appVersionName = "99.0",
            exportedAtEpochMilli = System.currentTimeMillis(),
            userName = "Giovanna",
            cycles = emptyList(),
            dailyLogs = emptyList(),
            preferences = emptyMap()
        )
        val bytes = BackupCrypto.packPlain(futurePayload.toJsonString())

        try {
            BackupManager.restoreBackup(context, repository, bytes)
            org.junit.Assert.fail("Deveria lançar IncompatibleVersionException")
        } catch (e: IncompatibleVersionException) {
            // Expected
        }
    }
}
