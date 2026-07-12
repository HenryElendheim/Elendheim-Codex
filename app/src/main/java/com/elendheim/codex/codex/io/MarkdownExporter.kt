package com.elendheim.codex.codex.io

import com.elendheim.codex.codex.model.CodexExport
import com.elendheim.codex.codex.model.Entity

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

    private fun renderEntity(sb: StringBuilder, e: Entity, labelByKey: Map<String, String>) {
        val classLabel = labelByKey[e.classification] ?: e.classification
        sb.appendLine("## ${e.designation} ${e.name}".trim())
        sb.appendLine()
        sb.appendLine("- Class: $classLabel")
        sb.appendLine("- Threat: ${e.threat} of 5")
        if (e.status != "active") sb.appendLine("- Status: ${e.status}")
        if (e.summary.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("_${e.summary}_")
        }

        if (e.description.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("### Description")
            sb.appendLine(e.description)
        }

        if (e.abilities.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("### Abilities")
            e.abilities.forEach { a ->
                sb.appendLine()
                sb.appendLine("**${a.name}**")
                if (a.mechanism.isNotBlank()) sb.appendLine("- How it works: ${a.mechanism}")
                if (a.limits.isNotBlank()) sb.appendLine("- Limits: ${a.limits}")
            }
        }

        if (e.weaknesses.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("### How to beat it")
            e.weaknesses.forEach { w ->
                sb.appendLine()
                sb.appendLine("**${w.name}** (${w.severity})")
                if (w.exploit.isNotBlank()) sb.appendLine("- ${w.exploit}")
            }
        }

        if (e.containment.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("### Containment")
            sb.appendLine(e.containment)
        }

        if (e.notes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("### Notes")
            sb.appendLine(e.notes)
        }

        if (e.tags.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("Tags: ${e.tags.joinToString(", ")}")
        }
    }
}
