package com.elendheim.codex.ui

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.elendheim.codex.codex.io.CodexFiles
import com.elendheim.codex.codex.io.MarkdownExporter
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.codex.repo.CodexRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// How the index list can be ordered.
enum class SortOrder { DesignationAsc, UpdatedDesc, ThreatDesc }

// Everything the index screen needs to decide what to show.
data class IndexFilters(
    val query: String = "",
    val classFilter: String? = null,   // class key, null means all
    val threatFilter: Int? = null,     // 1 to 5, null means all
    val tagFilter: String? = null,     // single tag, null means all
    val sort: SortOrder = SortOrder.DesignationAsc,
    val showArchived: Boolean = false  // the archive lives behind this toggle
)

// The shared brain of the app. It exposes the live data the screens read and the
// actions they call. All heavy list work, search, filter and sort, happens here in
// memory, which is instant at the size this archive is built for.
class CodexViewModel(private val repo: CodexRepository) : ViewModel() {

    // Raw streams from the repository.
    val classes: StateFlow<List<EntityClass>> =
        repo.classes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val designationPrefix: StateFlow<String> =
        repo.designationPrefix.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ELD")

    // Whether redaction mode is on. Drives how freeform text renders in the dossier.
    // On by default, so the initial value matches until the stored value loads.
    val redactionMode: StateFlow<Boolean> =
        repo.redactionMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setRedactionMode(on: Boolean) { viewModelScope.launch { repo.setRedactionMode(on) } }

    // Milliseconds since the last export, used to decide when to nudge for a backup.
    val lastExportAt: StateFlow<Long> =
        repo.lastExportAt.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // The archive name shown in the app, editable so anyone can rebrand it.
    val archiveName: StateFlow<String> =
        repo.archiveName.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Elendheim Codex")

    fun setArchiveName(name: String) { viewModelScope.launch { repo.setArchiveName(name); message.value = "Saved" } }

    // Accessibility settings. These drive the theme and a couple of animations.
    val textScale: StateFlow<Float> =
        repo.textScale.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1.0f)

    fun setTextScale(scale: Float) { viewModelScope.launch { repo.setTextScale(scale) } }

    val highContrast: StateFlow<Boolean> =
        repo.highContrast.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setHighContrast(on: Boolean) { viewModelScope.launch { repo.setHighContrast(on) } }

    val reduceMotion: StateFlow<Boolean> =
        repo.reduceMotion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setReduceMotion(on: Boolean) { viewModelScope.launch { repo.setReduceMotion(on) } }

    // The current filter and sort choices, changed by the index controls.
    val filters = MutableStateFlow(IndexFilters())

    // A short lived message for the snackbar, for example after an export.
    val message = MutableStateFlow<String?>(null)
    fun clearMessage() { message.value = null }

    private val allEntities: StateFlow<List<Entity>> =
        repo.entities.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // The list the index actually renders: the raw entities run through the filters.
    val visibleEntities: StateFlow<List<Entity>> =
        combine(allEntities, filters) { list, f -> applyFilters(list, f) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Every dossier keyed by id, used to show related entries as tappable links and
    // to offer the related picker in the editor.
    val entitiesById: StateFlow<Map<String, Entity>> =
        allEntities
            .map { list -> list.associateBy { it.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Count of archived dossiers, shown as a badge in settings.
    val archivedCount: StateFlow<Int> =
        combine(allEntities, filters) { list, _ -> list.count { it.status == "archived" } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // First run setup: default classes and a couple of example dossiers.
        viewModelScope.launch { repo.seedIfEmpty() }
    }

    // Turn the full list into what the current filters allow, then sort it.
    private fun applyFilters(list: List<Entity>, f: IndexFilters): List<Entity> {
        val q = f.query.trim().lowercase()
        val filtered = list.asSequence()
            .filter { if (f.showArchived) it.status == "archived" else it.status == "active" }
            .filter { f.classFilter == null || it.classification == f.classFilter }
            .filter { f.threatFilter == null || it.threat == f.threatFilter }
            .filter { f.tagFilter == null || it.tags.any { t -> t.equals(f.tagFilter, ignoreCase = true) } }
            .filter { q.isBlank() || matchesQuery(it, q) }
            .toList()

        return when (f.sort) {
            SortOrder.DesignationAsc -> filtered.sortedBy { it.designation }
            SortOrder.UpdatedDesc -> filtered.sortedByDescending { it.updatedAt }
            SortOrder.ThreatDesc -> filtered.sortedByDescending { it.threat }
        }
    }

    // A dossier matches a search if the text appears in any of its fields, including
    // its powers, counters and tags. Broad on purpose so nothing hides.
    private fun matchesQuery(e: Entity, q: String): Boolean {
        if (e.name.lowercase().contains(q)) return true
        if (e.designation.lowercase().contains(q)) return true
        if (e.summary.lowercase().contains(q)) return true
        if (e.description.lowercase().contains(q)) return true
        if (e.containment.lowercase().contains(q)) return true
        if (e.notes.lowercase().contains(q)) return true
        if (e.tags.any { it.lowercase().contains(q) }) return true
        if (e.abilities.any { it.name.lowercase().contains(q) || it.mechanism.lowercase().contains(q) }) return true
        if (e.weaknesses.any { it.name.lowercase().contains(q) || it.exploit.lowercase().contains(q) }) return true
        return false
    }

    // Every tag in use, for the tag filter menu.
    val allTags: StateFlow<List<String>> =
        allEntities
            .map { list -> list.flatMap { it.tags }.distinct().sorted() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter control setters.
    fun setQuery(value: String) { filters.value = filters.value.copy(query = value) }
    fun setClassFilter(key: String?) { filters.value = filters.value.copy(classFilter = key) }
    fun setThreatFilter(value: Int?) { filters.value = filters.value.copy(threatFilter = value) }
    fun setTagFilter(value: String?) { filters.value = filters.value.copy(tagFilter = value) }
    fun setSort(order: SortOrder) { filters.value = filters.value.copy(sort = order) }
    fun setShowArchived(value: Boolean) { filters.value = filters.value.copy(showArchived = value) }
    fun clearFilters() {
        val keepArchived = filters.value.showArchived
        filters.value = IndexFilters(showArchived = keepArchived)
    }

    // Pick a random active dossier, good for rediscovering old ideas. Returns null
    // when the archive has no active entries yet.
    fun randomEntityId(): String? =
        allEntities.value.filter { it.status == "active" }.randomOrNull()?.id

    // Dossier actions.
    suspend fun loadEntity(id: String): Entity? = repo.getEntity(id)
    suspend fun newDraft(): Entity = repo.newDraft()
    fun save(entity: Entity) { viewModelScope.launch { repo.saveEntity(entity) } }
    fun archive(id: String) { viewModelScope.launch { repo.archiveEntity(id); message.value = "Moved to archive" } }
    fun restore(id: String) { viewModelScope.launch { repo.restoreEntity(id); message.value = "Restored" } }
    fun deleteForever(id: String) { viewModelScope.launch { repo.deleteForever(id); message.value = "Deleted for good" } }

    // Make a copy of a dossier and return the new id so the caller can open it.
    suspend fun duplicate(id: String): String? {
        val newId = repo.duplicate(id)
        message.value = if (newId != null) "Copied" else "Could not copy"
        return newId
    }

    // Build the readable text for a single dossier, used by the share action.
    fun shareTextFor(entity: Entity): String =
        MarkdownExporter.renderOne(entity, classes.value)

    // Settings actions. confirm shows a short "Saved" note when the reader pressed a
    // save control, so it is obvious the change went through.
    fun saveClasses(list: List<EntityClass>, confirm: Boolean = false) {
        viewModelScope.launch { repo.saveClasses(list); if (confirm) message.value = "Saved" }
    }
    fun setPrefix(value: String) { viewModelScope.launch { repo.setDesignationPrefix(value); message.value = "Saved" } }

    // Export the whole archive as JSON to the location the user picked.
    fun exportJson(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val export = repo.buildExport()
                CodexFiles.writeText(resolver, uri, CodexFiles.encode(export))
                repo.recordExport()
                export.entities.size
            }.onSuccess { count -> message.value = "Exported $count entries" }
                .onFailure { message.value = "Export failed: ${it.message}" }
        }
    }

    // Export the readable Markdown copy.
    fun exportMarkdown(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch {
            runCatching {
                val export = repo.buildExport()
                CodexFiles.writeText(resolver, uri, MarkdownExporter.render(export))
            }.onSuccess { message.value = "Saved Markdown copy" }
                .onFailure { message.value = "Export failed: ${it.message}" }
        }
    }

    // Import a chosen JSON file. Merge keeps the newer copy of any clash, replace
    // wipes first for a full restore. Both are safe, replace is behind a confirm.
    fun importJson(resolver: ContentResolver, uri: Uri, replace: Boolean) {
        viewModelScope.launch {
            runCatching {
                val text = CodexFiles.readText(resolver, uri)
                val data = CodexFiles.decode(text)
                if (replace) {
                    repo.importReplace(data)
                    "Replaced with ${data.entities.size} entries"
                } else {
                    val r = repo.importMerge(data)
                    "Imported: ${r.added} added, ${r.updated} updated, ${r.skipped} unchanged"
                }
            }.onSuccess { message.value = it }
                .onFailure { message.value = "Import failed: ${it.message}" }
        }
    }

    // Factory so Compose can build the ViewModel with the repository from the app.
    class Factory(private val repo: CodexRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CodexViewModel(repo) as T
    }
}
