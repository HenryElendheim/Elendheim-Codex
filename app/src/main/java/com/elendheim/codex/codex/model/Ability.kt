package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// A single power. It is a structured row, not a paragraph, so the reading view can
// render powers as clean scannable blocks and future search can match on them.
@Serializable
data class Ability(
    val name: String = "",        // for example Chorus Pull
    val mechanism: String = "",   // how it works, the part that matters
    val limits: String = ""       // range, cost, conditions, cooldown
)
