package com.elendheim.codex.codex.io

import com.elendheim.codex.codex.model.CodexExport
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.codex.model.effectiveImages

// The human copy of the archive. JSON is the machine truth for import and export,
// this writes one readable Markdown document with every dossier laid out so it can
// be read, printed or pasted anywhere. Pure text work, no Android types, so it is
// easy to test.
object MarkdownExporter {

    fun render(export: CodexExport): String {
        val labelByKey = export.classes.associate { it.key to it.label }
        val sb = StringBuilder()

        sb.appendLine("# Elendheim Codex")
        sb.appendLine()
        sb.appendLine("Entries: ${export.entities.size}")
        sb.appendLine()

        // Sort by designation so the document reads in archive order.
        export.entities
            .sortedBy { it.designation }
            .forEach { entity ->
                renderEntity(sb, entity, labelByKey)
                sb.appendLine()
                sb.appendLine("---")
                sb.appendLine()
            }

        return sb.toString().trimEnd() + "\n"
    }

    // Render a single dossier on its own. Used by the share action so one entry can be
    // sent as clean, readable text.
    fun renderOne(entity: Entity, classes: List<EntityClass>): String {
        val labelByKey = classes.associate { it.key to it.label }
        val sb = StringBuilder()
        renderEntity(sb, entity, labelByKey)
        return sb.toString().trimEnd() + "\n"
    }

    private fun renderEntity(sb: StringBuilder, e: Entity, labelByKey: Map<String, String>) {
        val classLabel = labelByKey[e.classification] ?: e.classification
        sb.appendLine("## ${e.designation} ${e.name}".trim())
        sb.appendLine()
        sb.appendLine("- Class: $classLabel")
        sb.appendLine("- Threat: ${e.threat} of 5")
        if (e.status != "active") sb.appendLine("- Status: ${e.status}")
        // The readable copy just notes how many images exist, the pictures themselves
        // stay in the JSON export as base64.
        val imageCount = e.effectiveImages().size
        if (imageCount == 1) sb.appendLine("- Images: 1 attached")
        else if (imageCount > 1) sb.appendLine("- Images: $imageCount attached")
        if (e.summary.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("_${e.summary.plainText()}_")
        }

        if (e.description.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("### Description")
            sb.appendLine(e.description.plainText())
        }

        if (e.abilities.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("### Abilities")
            e.abilities.forEach { a ->
                sb.appendLine()
                sb.appendLine("**${a.name}**")
                if (a.mechanism.isNotBlank()) sb.appendLine("- How it works: ${a.mechanism.plainText()}")
                if (a.limits.isNotBlank()) sb.appendLine("- Limits: ${a.limits.plainText()}")
            }
        }

        if (e.weaknesses.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("### How to beat it")
            e.weaknesses.forEach { w ->
                sb.appendLine()
                sb.appendLine("**${w.name}** (${w.severity})")
                if (w.exploit.isNotBlank()) sb.appendLine("- ${w.exploit.plainText()}")
            }
        }

        if (e.containment.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("### Containment")
            sb.appendLine(e.containment.plainText())
        }

        if (e.notes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("### Notes")
            sb.appendLine(e.notes.plainText())
        }

        if (e.tags.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Tags: ${e.tags.joinToString(", ")}")
        }
    }

    // The readable copy drops the redaction markers so %% never leaks into the text.
    // The words stay, only the markup is removed.
    private fun String.plainText(): String = replace("%%", "")
}
