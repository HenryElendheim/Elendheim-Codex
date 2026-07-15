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
        private const val KEY_REDACTION = "redaction_mode"
        private const val KEY_LAST_EXPORT = "last_export_at"
        private const val KEY_ARCHIVE_NAME = "archive_name"
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_HIGH_CONTRAST = "high_contrast"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_CLEARANCE = "clearance"
        private const val KEY_NUDGE_DISMISSED = "nudge_dismissed_at"
        private const val KEY_SPIN_SECONDS = "spin_seconds"
        private const val KEY_OVERVIEW_MODE = "overview_mode"
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

    // Redaction mode is a display only toggle. Text marked with %% %% shows as solid
    // blocks when this is on, and the real words are always kept in storage. It is on
    // by default, so only an explicit "false" that the reader chose turns it off.
    val redactionMode: Flow<Boolean> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_REDACTION }?.value != "false"
        }

    suspend fun setRedactionMode(on: Boolean) {
        dao.putSetting(SettingRecord(KEY_REDACTION, if (on) "true" else "false"))
    }

    // When the archive was last exported, used for the gentle backup reminder.
    val lastExportAt: Flow<Long> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_LAST_EXPORT }?.value?.toLongOrNull() ?: 0L
        }

    // Called by the ViewModel after a successful export so the reminder resets.
    suspend fun recordExport() {
        dao.putSetting(SettingRecord(KEY_LAST_EXPORT, now().toString()))
    }

    // The archive name shown in the app. Anyone can rename it to make the app their
    // own sorting system, so it is data rather than a fixed string.
    val archiveName: Flow<String> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_ARCHIVE_NAME }?.value?.takeIf { it.isNotBlank() }
                ?: Defaults.archiveName
        }

    suspend fun setArchiveName(name: String) {
        dao.putSetting(SettingRecord(KEY_ARCHIVE_NAME, name.trim().ifBlank { Defaults.archiveName }))
    }

    // Accessibility: a text size multiplier applied to the whole app. Defaults to 1.0.
    val textScale: Flow<Float> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_TEXT_SCALE }?.value?.toFloatOrNull() ?: 1.0f
        }

    suspend fun setTextScale(scale: Float) {
        dao.putSetting(SettingRecord(KEY_TEXT_SCALE, scale.toString()))
    }

    // Accessibility: a higher contrast palette for readers who want stronger text.
    val highContrast: Flow<Boolean> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_HIGH_CONTRAST }?.value == "true"
        }

    suspend fun setHighContrast(on: Boolean) {
        dao.putSetting(SettingRecord(KEY_HIGH_CONTRAST, if (on) "true" else "false"))
    }

    // Accessibility: skip the fade animations for readers who prefer less motion.
    val reduceMotion: Flow<Boolean> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_REDUCE_MOTION }?.value == "true"
        }

    suspend fun setReduceMotion(on: Boolean) {
        dao.putSetting(SettingRecord(KEY_REDUCE_MOTION, if (on) "true" else "false"))
    }

    // The reader's own security clearance, 1 to 5. A file can only be revealed when
    // this is at least the clearance the file requires. Starts at the lowest level, so
    // more redacted files stay sealed until the reader raises it. Flavor, but it sets
    // the mood.
    val clearance: Flow<Int> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_CLEARANCE }?.value?.toIntOrNull()?.coerceIn(1, 5) ?: 1
        }

    suspend fun setClearance(level: Int) {
        dao.putSetting(SettingRecord(KEY_CLEARANCE, level.coerceIn(1, 5).toString()))
    }

    // When the backup reminder was last dismissed with the x. It stays away for a
    // while after that, so it never nags right after you wave it off.
    val nudgeDismissedAt: Flow<Long> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_NUDGE_DISMISSED }?.value?.toLongOrNull() ?: 0L
        }

    suspend fun recordNudgeDismissed() {
        dao.putSetting(SettingRecord(KEY_NUDGE_DISMISSED, now().toString()))
    }

    // How long the randomize spin runs, in seconds. Default 3, capped at 10.
    val spinSeconds: Flow<Float> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_SPIN_SECONDS }?.value?.toFloatOrNull()?.coerceIn(1f, 10f) ?: 3f
        }

    suspend fun setSpinSeconds(seconds: Float) {
        dao.putSetting(SettingRecord(KEY_SPIN_SECONDS, seconds.coerceIn(1f, 10f).toString()))
    }

    // Which chart style the overview shows. Remembered so it opens how you left it.
    val overviewMode: Flow<String> =
        dao.observeSettings().map { rows ->
            rows.firstOrNull { it.key == KEY_OVERVIEW_MODE }?.value ?: "row"
        }

    suspend fun setOverviewMode(mode: String) {
        dao.putSetting(SettingRecord(KEY_OVERVIEW_MODE, mode))
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

    // Make a copy of an existing dossier as a fresh entry. The copy gets a brand new
    // id and the next free designation, its name is marked as a copy, and it starts
    // active. Handy for building variants without retyping everything. Returns the new
    // id, or null if the source is gone.
    suspend fun duplicate(id: String): String? {
        val source = dao.getEntity(id)?.toDomain() ?: return null
        val stamp = now()
        val copy = source.copy(
            id = UUID.randomUUID().toString(),
            designation = nextDesignation(),
            name = (source.name.ifBlank { "Unnamed" } + " (copy)"),
            related = emptyList(),   // links point at the original, not carried over
            createdAt = stamp,
            updatedAt = stamp,
            status = "active"
        )
        dao.upsertEntity(copy.toRecord())
        return copy.id
    }

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
