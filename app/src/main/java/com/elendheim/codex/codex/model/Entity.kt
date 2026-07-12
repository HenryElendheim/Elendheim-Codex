package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// One entity is one dossier. Structured where structure pays off, freeform where
// it does not. Every field has a default so old export files still load into new
// versions of the app without breaking.
@Serializable
data class Entity(
    val id: String,                       // UUID, stable forever, links depend on it
    val designation: String = "",         // for example ELD-013, auto assigned but editable
    val name: String = "",                // for example The Hollow Chorister
    val classification: String = "",      // key into the user defined class list
    val threat: Int = 1,                  // threat rating from 1 to 5
    val summary: String = "",             // one line description shown in the index

    // The core sections. Freeform text with light markdown style writing.
    val description: String = "",         // what it is, how it looks, how it behaves
    val abilities: List<Ability> = emptyList(),   // structured powers, see below
    val weaknesses: List<Weakness> = emptyList(), // structured counters, see below
    val containment: String = "",         // how to hold, neutralise or survive it
    val notes: String = "",               // lore, incidents, open questions

    val tags: List<String> = emptyList(), // freeform labels, for example sound-based
    val related: List<String> = emptyList(), // ids of linked entities
    val image: String = "",               // one optional sketch, JPEG stored as base64
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val status: String = "active"         // active or archived, archived is a soft delete
)
