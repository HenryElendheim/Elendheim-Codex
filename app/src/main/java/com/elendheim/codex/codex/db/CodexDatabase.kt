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
    version = 2,
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

        // Standard single instance pattern so the whole app shares one connection.
        fun get(context: Context): CodexDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodexDatabase::class.java,
                    "elendheim-codex.db"
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
