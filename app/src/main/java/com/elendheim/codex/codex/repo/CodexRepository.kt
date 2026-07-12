package com.elendheim.codex.codex.repo

import com.elendheim.codex.codex.db.CodexDao
import com.elendheim.codex.codex.db.SettingRecord
import com.elendheim.codex.codex.db.toDomain
import com.elendheim.codex.codex.db.toRecord
import com.elendheim.codex.codex.model.CodexExport
import com.elendheim.codex.codex.model.Defaults
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.EntityClass
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// The one door to the data layer. Screens never touch Room or JSON directly, they
// ask the repository. This keeps the data layer with no knowledge of the UI.
class CodexRepository(private val dao: CodexDao) {

    companion object {
        private const val KEY_PREFIX = "designation_prefix"
    }

    // Live streams the screens observe. Room pushes a new list whenever data changes.
    val entities: Flow<List<Entity>> =
        dao.observeEntities().map { rows -> rows.map { it.toDomain() } }

    val classes: Flow<List<EntityClass>> =
        dao.observeClasses().map { rows -> rows.map { it.toDomain() } }

    // The designation prefix is watched too so a change in settings shows up at once.
    val designationPrefix: Flow<String> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_PREFIX }?.value ?: Defaults.designationPrefix
        }

    suspend fun getEntity(id: String): Entity? = dao.getEntity(id)?.toDomain()

    // Create or update. We always stamp updatedAt so import can pick the newer copy.
    suspend fun saveEntity(entity: Entity) {
        dao.upsertEntity(entity.copy(updatedAt = now()).toRecord())
    }

    // Soft delete. The dossier is not removed, it is moved to the archive so no idea
    // is ever truly lost. The archive screen can bring it back or delete for good.
    suspend fun archiveEntity(id: String) {
        val current = dao.getEntity(id)?.toDomain() ?: return
        dao.upsertEntity(current.copy(status = "archived", updatedAt = now()).toRecord())
    }

    suspend fun restoreEntity(id: String) {
        val current = dao.getEntity(id)?.toDomain() ?: return
        dao.upsertEntity(current.copy(status = "active", updatedAt = now()).toRecord())
    }

    // Hard delete, only reached from the archive behind a confirm.
    suspend fun deleteForever(id: String) = dao.deleteEntity(id)

    suspend fun saveClasses(list: List<EntityClass>) {
        dao.clearClasses()
        dao.upsertClasses(list.mapIndexed { index, c -> c.toRecord(index) })
    }

    suspend fun setDesignationPrefix(prefix: String) {
        dao.putSetting(SettingRecord(KEY_PREFIX, prefix.trim().ifBlank { Defaults.designationPrefix }))
    }

    // Work out the next free designation, for example ELD-014. We look at every
    // existing designation with the current prefix, take the highest number and add
    // one. The result is only a suggestion, the editor still lets it be changed.
    suspend fun nextDesignation(): String {
        val prefix = dao.getSetting(KEY_PREFIX) ?: Defaults.designationPrefix
        val rows = dao.getAllEntities()
        val highest = rows
            .mapNotNull { record ->
                val match = Regex("^${Regex.escape(prefix)}-(\\d+)$").find(record.designation.trim())
                match?.groupValues?.get(1)?.toIntOrNull()
            }
            .maxOrNull() ?: 0
        val next = highest + 1
        // Pad to at least three digits so sorting by text stays tidy, ELD-001 style.
        return "%s-%03d".format(prefix, next)
    }

    // Build a fresh empty dossier ready for the editor, with a stable id and the
    // next designation already filled in.
    suspend fun newDraft(): Entity {
        val stamp = now()
        return Entity(
            id = UUID.randomUUID().toString(),
            designation = nextDesignation(),
            createdAt = stamp,
            updatedAt = stamp
        )
    }

    // Gather the whole archive into one export object, including archived dossiers.
    suspend fun buildExport(): CodexExport {
        val allEntities = dao.getAllEntities().map { it.toDomain() }
        val allClasses = dao.getClasses().map { it.toDomain() }
        return CodexExport(
            schemaVersion = 1,
            exportedAt = now(),
            classes = allClasses,
            entities = allEntities
        )
    }

    // Merge safe import. For each incoming dossier we match by id: a new id is added,
    // an existing id keeps whichever copy has the newer updatedAt. Nothing is blindly
    // overwritten and nothing is duplicated. Classes are added if their key is new.
    suspend fun importMerge(data: CodexExport): ImportResult {
        var added = 0
        var updated = 0
        var skipped = 0

        val existingById = dao.getAllEntities().associateBy { it.id }
        val toWrite = mutableListOf<Entity>()
        for (incoming in data.entities) {
            val current = existingById[incoming.id]
            when {
                current == null -> { toWrite += incoming; added++ }
                incoming.updatedAt > current.updatedAt -> { toWrite += incoming; updated++ }
                else -> skipped++
            }
        }
        if (toWrite.isNotEmpty()) dao.upsertEntities(toWrite.map { it.toRecord() })

        // Merge classes by key, keeping any the user already has.
        val currentClasses = dao.getClasses().map { it.toDomain() }
        val byKey = LinkedHashMap<String, EntityClass>()
        currentClasses.forEach { byKey[it.key] = it }
        data.classes.forEach { byKey.putIfAbsent(it.key, it) }
        saveClasses(byKey.values.toList())

        return ImportResult(added, updated, skipped)
    }

    // Full restore. Wipes everything then writes the file exactly as it is. This is
    // the path the round trip test uses: export, replace, and the archive matches.
    suspend fun importReplace(data: CodexExport) {
        dao.clearEntities()
        dao.upsertEntities(data.entities.map { it.toRecord() })
        saveClasses(data.classes)
    }

    // First run setup. If the database is empty we lay down the default classes and a
    // few example dossiers so the app is never a blank wall on first open.
    suspend fun seedIfEmpty() {
        val hasClasses = dao.getClasses().isNotEmpty()
        if (!hasClasses) saveClasses(Defaults.classes)

        val hasEntities = dao.getAllEntities().isNotEmpty()
        if (!hasEntities) {
            SeedData.entities().forEach { dao.upsertEntity(it.toRecord()) }
        }
    }

    private fun now(): Long = System.currentTimeMillis()

    data class ImportResult(val added: Int, val updated: Int, val skipped: Int)
}
