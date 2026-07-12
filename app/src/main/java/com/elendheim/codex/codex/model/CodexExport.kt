package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// The whole archive in one object. Export writes this to a single JSON file and
// import reads it back. The schema version lets future versions add fields while
// old files still load, that is the own your data promise of the suite.
@Serializable
data class CodexExport(
    val schemaVersion: Int = 1,
    val exportedAt: Long = 0L,
    val classes: List<EntityClass> = emptyList(),
    val entities: List<Entity> = emptyList()
)
