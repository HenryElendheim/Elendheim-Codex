package com.elendheim.codex.codex.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// All database reads and writes go through this one interface. Reads return Flow so
// the screens update on their own whenever the data changes.
@Dao
interface CodexDao {

    // Dossiers. We load every row and do search, filter and sort in memory, which is
    // fast and simple at the hundreds of entries this archive is built for.
    @Query("SELECT * FROM entities")
    fun observeEntities(): Flow<List<EntityRecord>>

    // One shot read of every row, used for export, import matching and seeding.
    @Query("SELECT * FROM entities")
    suspend fun getAllEntities(): List<EntityRecord>

    @Query("SELECT * FROM entities WHERE id = :id LIMIT 1")
    suspend fun getEntity(id: String): EntityRecord?

    @Upsert
    suspend fun upsertEntity(record: EntityRecord)

    @Upsert
    suspend fun upsertEntities(records: List<EntityRecord>)

    @Query("DELETE FROM entities WHERE id = :id")
    suspend fun deleteEntity(id: String)

    @Query("DELETE FROM entities")
    suspend fun clearEntities()

    // Classification tiers, kept in the order the user set.
    @Query("SELECT * FROM classes ORDER BY sortIndex ASC")
    fun observeClasses(): Flow<List<ClassRecord>>

    @Query("SELECT * FROM classes ORDER BY sortIndex ASC")
    suspend fun getClasses(): List<ClassRecord>

    @Upsert
    suspend fun upsertClasses(records: List<ClassRecord>)

    @Query("DELETE FROM classes")
    suspend fun clearClasses()

    // Small settings values.
    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT * FROM settings")
    fun observeSettings(): Flow<List<SettingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putSetting(record: SettingRecord)
}
