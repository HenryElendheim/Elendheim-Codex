package com.elendheim.codex.codex.io

import com.elendheim.codex.codex.model.CodexExport
import kotlinx.serialization.json.Json

// One shared JSON configuration used everywhere data is written or read.
// - prettyPrint keeps the export file readable if you open it in a text editor
// - ignoreUnknownKeys means a file made by a future version, with fields this
//   version does not know yet, still imports instead of crashing
// - encodeDefaults writes default values out so the file is complete on its own
object CodexJson {
    val format: Json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // The whole archive turned into the JSON text that gets saved to a file.
    fun encode(export: CodexExport): String =
        format.encodeToString(CodexExport.serializer(), export)

    // Chosen JSON text read back into an export object. Lenient parsing means a file
    // from a newer app version still loads.
    fun decode(text: String): CodexExport =
        format.decodeFromString(CodexExport.serializer(), text)
}
