package com.elendheim.codex.codex.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// The Room database. It is only a fast local cache of what the JSON export already
// describes, so if the two ever disagree in design, the export format wins.
@Database(
    entities = [EntityRecord::class, ClassRecord::class, SettingRecord::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CodexDatabase : RoomDatabase() {

    abstract fun codexDao(): CodexDao

    companion object {
        @Volatile private var instance: CodexDatabase? = null

        // Version 2 added the optional image column. This migration keeps any data
        // written by version 1 intact, it simply adds the new empty column.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entities ADD COLUMN image TEXT NOT NULL DEFAULT ''")
            }
        }

        // Version 3 added the images gallery column. The list is stored as JSON text,
        // and an empty default reads back as an empty list, so old rows are untouched
        // and still show their single image through the fallback in the model.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entities ADD COLUMN images TEXT NOT NULL DEFAULT ''")
            }
        }

        // Version 4 added the story log column, stored as JSON text like the other
        // lists. Empty default reads back as an empty log, so old rows are untouched.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entities ADD COLUMN story TEXT NOT NULL DEFAULT ''")
            }
        }

        // Version 5 added the pictures gallery column, where each image can be redacted.
        // Stored as JSON text, empty default reads back as an empty gallery, and the
        // fallback in the model keeps older single image and images fields working.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE entities ADD COLUMN pictures TEXT NOT NULL DEFAULT ''")
            }
        }

        // Standard single instance pattern so the whole app shares one connection.
        fun get(context: Context): CodexDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodexDatabase::class.java,
                    "elendheim-codex.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { instance = it }
            }
    }
}
