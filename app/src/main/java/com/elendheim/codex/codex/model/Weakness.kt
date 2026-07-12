package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// A single counter, the how to beat it side of a dossier. Also structured so the
// reading view can list counters cleanly and severity can be filtered later.
@Serializable
data class Weakness(
    val name: String = "",       // for example True Silence
    val exploit: String = "",    // how to use it against the entity
    val severity: String = "situational" // hard counter, mitigation or situational
)
