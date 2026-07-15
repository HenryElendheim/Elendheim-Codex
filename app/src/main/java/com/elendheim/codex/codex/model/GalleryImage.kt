package com.elendheim.codex.codex.model

import kotlinx.serialization.Serializable

// One picture in an entity's gallery. The data is a base64 JPEG or PNG, exactly like
// before. The redacted flag lets a single image be hidden behind a censored panel
// when redaction mode is on, the same idea as %% on text. The real image is always
// kept in storage, only the display is blocked.
@Serializable
data class GalleryImage(
    val data: String = "",
    val redacted: Boolean = false
)
