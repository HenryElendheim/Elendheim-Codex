package com.elendheim.codex.codex.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// The Room database. It is only a fast local cache of what the JSON export already
// describes, so if the two ever disagree in design, the export format wins.
@Database(
    entities = [EntityRecord::class, ClassRecord::class, SettingRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CodexDatabase : RoomDatabase() {

    abstract fun codexDao(): CodexDao

    companion object {
        @Volatile private var instance: CodexDatabase? = null

        // Standard single instance pattern so the whole app shares one connection.
        fun get(context: Context): CodexDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CodexDatabase::class.java,
                    "elendheim-codex.db"
                ).build().also { instance = it }
            }
    }
}
