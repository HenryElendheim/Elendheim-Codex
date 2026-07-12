package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// A classification tier that the user defines themselves. It is data, not code, so
// the scheme is yours to invent and it travels inside the export file.
@Serializable
data class EntityClass(
    val key: String,              // stable id stored on entities, for example "unbound"
    val label: String,            // shown name, for example Unbound
    val colorHex: String,         // chip colour, for example #C4383A
    val meaning: String = ""      // short description of what the tier means
)
