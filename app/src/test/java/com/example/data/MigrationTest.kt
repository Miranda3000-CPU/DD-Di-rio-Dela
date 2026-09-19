package com.example.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class MigrationTest {

    private val TEST_DB = "migration-test-db"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun testMigration1To2PreservesAllData() {
        // 1. Create database in schema v1
        var db = helper.createDatabase(TEST_DB, 1)

        // 2. Insert representative legacy records in v1
        db.execSQL(
            """
            INSERT INTO cycle_records (id, startDateEpochDay, periodLengthDays, flowIntensity, symptoms, notes)
            VALUES (1, 19700, 5, 'MÉDIO', 'Cólica, Dor de cabeça', 'Dia com cólicas leves no início')
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO cycle_records (id, startDateEpochDay, periodLengthDays, flowIntensity, symptoms, notes)
            VALUES (2, 19728, 4, 'LEVE', 'Inchaço', 'Tudo normal')
            """.trimIndent()
        )
        db.close()

        // 3. Run migration 1 -> 2 and validate schema
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AppDatabase.MIGRATION_1_2)

        // 4. Validate that all legacy records are preserved exactly
        val cursor = db.query("SELECT id, uuid, startDateEpochDay, periodLengthDays, flowIntensity, symptoms, notes, createdAtEpochMilli, updatedAtEpochMilli FROM cycle_records ORDER BY id ASC")
        assertEquals(2, cursor.count)

        // Row 1
        assertTrue(cursor.moveToNext())
        assertEquals(1L, cursor.getLong(0))
        val uuid1 = cursor.getString(1)
        assertNotNull(uuid1)
        assertTrue(uuid1.isNotBlank())
        assertEquals(36, uuid1.length) // Standard UUID length
        assertEquals(19700L, cursor.getLong(2))
        assertEquals(5, cursor.getInt(3))
        assertEquals("MÉDIO", cursor.getString(4))
        assertEquals("Cólica, Dor de cabeça", cursor.getString(5))
        assertEquals("Dia com cólicas leves no início", cursor.getString(6))
        assertTrue(cursor.getLong(7) > 0)
        assertTrue(cursor.getLong(8) > 0)

        // Row 2
        assertTrue(cursor.moveToNext())
        assertEquals(2L, cursor.getLong(0))
        val uuid2 = cursor.getString(1)
        assertNotNull(uuid2)
        assertTrue(uuid2.isNotBlank())
        assertEquals(36, uuid2.length)
        assertEquals(19728L, cursor.getLong(2))
        assertEquals(4, cursor.getInt(3))
        assertEquals("LEVE", cursor.getString(4))
        assertEquals("Inchaço", cursor.getString(5))
        assertEquals("Tudo normal", cursor.getString(6))
        assertTrue(cursor.getLong(7) > 0)
        assertTrue(cursor.getLong(8) > 0)

        cursor.close()

        // 5. Validate that daily_logs table was populated with the symptoms & notes from the start of the cycles
        val dailyCursor = db.query("SELECT dateEpochDay, flowIntensity, symptoms, notes FROM daily_logs ORDER BY dateEpochDay ASC")
        assertEquals(2, dailyCursor.count)

        assertTrue(dailyCursor.moveToNext())
        assertEquals(19700L, dailyCursor.getLong(0))
        assertEquals("MÉDIO", dailyCursor.getString(1))
        assertEquals("Cólica, Dor de cabeça", dailyCursor.getString(2))
        assertEquals("Dia com cólicas leves no início", dailyCursor.getString(3))

        assertTrue(dailyCursor.moveToNext())
        assertEquals(19728L, dailyCursor.getLong(0))
        assertEquals("LEVE", dailyCursor.getString(1))
        assertEquals("Inchaço", dailyCursor.getString(2))
        assertEquals("Tudo normal", dailyCursor.getString(3))

        dailyCursor.close()
        db.close()
    }
}
