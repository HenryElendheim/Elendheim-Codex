package com.elendheim.codex.codex.io

import android.content.ContentResolver
import android.net.Uri
import com.elendheim.codex.codex.model.CodexExport

// Reads and writes the export file at the exact location the user picked through the
// system file picker. We only ever touch the one Uri handed to us, nothing else.
object CodexFiles {

    // Encoding and decoding live in CodexJson so they carry no Android dependency and
    // can be tested on their own. These stay here as the file facing entry points.
    fun encode(export: CodexExport): String = CodexJson.encode(export)

    fun decode(text: String): CodexExport = CodexJson.decode(text)

    // Write text to the picked location.
    fun writeText(resolver: ContentResolver, uri: Uri, text: String) {
        resolver.openOutputStream(uri, "wt")?.use { stream ->
            stream.write(text.toByteArray(Charsets.UTF_8))
            stream.flush()
        } ?: error("Could not open the chosen file for writing.")
    }

    // Read text from the picked location.
    fun readText(resolver: ContentResolver, uri: Uri): String =
        resolver.openInputStream(uri)?.use { stream ->
            stream.readBytes().toString(Charsets.UTF_8)
        } ?: error("Could not open the chosen file for reading.")

    // A friendly default file name with today's date baked in.
    fun suggestedJsonName(year: Int, month: Int, day: Int): String =
        "elendheim-codex-%04d-%02d-%02d.json".format(year, month, day)

    fun suggestedMarkdownName(year: Int, month: Int, day: Int): String =
        "elendheim-codex-%04d-%02d-%02d.md".format(year, month, day)
}
