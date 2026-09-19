package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.UUID

@Database(
    entities = [CycleEntity::class, DailyLogEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        const val DATABASE_NAME = "meu_ciclo_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Add uuid, createdAtEpochMilli, updatedAtEpochMilli to cycle_records
                db.execSQL("ALTER TABLE cycle_records ADD COLUMN uuid TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cycle_records ADD COLUMN createdAtEpochMilli INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cycle_records ADD COLUMN updatedAtEpochMilli INTEGER NOT NULL DEFAULT 0")

                // 2. Generate UUIDs and timestamps for existing records
                val cursor = db.query("SELECT id FROM cycle_records WHERE uuid = '' OR uuid IS NULL")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val generatedUuid = UUID.randomUUID().toString()
                    val now = System.currentTimeMillis()
                    db.execSQL(
                        "UPDATE cycle_records SET uuid = ?, createdAtEpochMilli = ?, updatedAtEpochMilli = ? WHERE id = ?",
                        arrayOf(generatedUuid, now, now, id)
                    )
                }
                cursor.close()

                // 3. Create indices on cycle_records
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_cycle_records_uuid ON cycle_records(uuid)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_cycle_records_startDateEpochDay ON cycle_records(startDateEpochDay)")

                // 4. Create daily_logs table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        uuid TEXT NOT NULL,
                        dateEpochDay INTEGER NOT NULL,
                        flowIntensity TEXT,
                        painLevel TEXT,
                        symptoms TEXT NOT NULL DEFAULT '',
                        mood TEXT,
                        energyLevel TEXT,
                        sleepQuality TEXT,
                        cervicalMucus TEXT,
                        notes TEXT NOT NULL DEFAULT '',
                        createdAtEpochMilli INTEGER NOT NULL,
                        updatedAtEpochMilli INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_logs_uuid ON daily_logs(uuid)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_daily_logs_dateEpochDay ON daily_logs(dateEpochDay)")

                // 5. Migrate symptoms/notes from cycle_records to daily_logs on the cycle's start date without deleting anything
                val cycleCursor = db.query("SELECT startDateEpochDay, flowIntensity, symptoms, notes, createdAtEpochMilli FROM cycle_records")
                while (cycleCursor.moveToNext()) {
                    val startDateEpochDay = cycleCursor.getLong(0)
                    val flow = cycleCursor.getString(1)
                    val symptoms = cycleCursor.getString(2)
                    val notes = cycleCursor.getString(3)
                    val createdAt = cycleCursor.getLong(4)

                    if (symptoms.isNotBlank() || notes.isNotBlank() || flow.isNotBlank()) {
                        val logUuid = UUID.randomUUID().toString()
                        val timestamp = if (createdAt > 0) createdAt else System.currentTimeMillis()
                        db.execSQL(
                            """
                            INSERT OR IGNORE INTO daily_logs 
                            (uuid, dateEpochDay, flowIntensity, symptoms, notes, createdAtEpochMilli, updatedAtEpochMilli)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf(logUuid, startDateEpochDay, flow, symptoms, notes, timestamp, timestamp)
                        )
                    }
                }
                cycleCursor.close()
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
