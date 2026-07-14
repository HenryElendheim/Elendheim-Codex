package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// One past event in an entity's history. These build up a log of how the anomaly has
// interacted with the world over time. The body is freeform, so it supports the same
// redaction markers and cross-links as the other text fields.
@Serializable
data class StoryEntry(
    val title: String = "",   // for example First Contact, or Incident at the docks
    val period: String = "",  // an optional time label, for example Year 3, or "Winter"
    val body: String = ""     // what happened, redactions and [links] welcome
)
