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
    val containment: String = "",         // how to hold, neutralize or survive it
    val notes: String = "",               // lore, incidents, open questions
    val story: List<StoryEntry> = emptyList(), // past events, the history log

    val tags: List<String> = emptyList(), // freeform labels, for example sound-based
    val related: List<String> = emptyList(), // ids of linked entities
    // Image fields, newest last. pictures is the current gallery and each item can be
    // redacted on its own. The two older fields are kept so files from earlier versions
    // still load, and new entries mirror their gallery into them for old readers.
    val image: String = "",               // legacy single image, base64
    val images: List<String> = emptyList(), // legacy gallery, base64 per item
    val pictures: List<GalleryImage> = emptyList(), // current gallery, first is cover
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val status: String = "active"         // active or archived, archived is a soft delete
)

// The gallery to actually show, resolved across the three image fields. New entries
// fill pictures, older ones only have images or the single image, so fall back down
// the chain. One source of truth for the UI without touching stored data.
fun Entity.effectiveGallery(): List<GalleryImage> = when {
    pictures.isNotEmpty() -> pictures
    images.isNotEmpty() -> images.map { GalleryImage(it) }
    image.isNotBlank() -> listOf(GalleryImage(image))
    else -> emptyList()
}

// Just the image data, used where redaction does not matter, for example counting
// images in the Markdown export.
fun Entity.effectiveImages(): List<String> =
    effectiveGallery().map { it.data }
