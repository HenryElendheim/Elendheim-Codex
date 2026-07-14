package com.elendheim.codex

import com.elendheim.codex.codex.io.CodexFiles
import com.elendheim.codex.codex.io.MarkdownExporter
import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.CodexExport
import com.elendheim.codex.codex.model.Defaults
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.Weakness
import com.elendheim.codex.codex.model.effectiveImages
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// The export and import promise from the build plan: a full trip through JSON must
// reproduce the archive exactly. If this ever fails, the data format has drifted.
class ExportRoundTripTest {

    private fun sampleArchive(): CodexExport = CodexExport(
        schemaVersion = 1,
        exportedAt = 1_700_000_000_000L,
        classes = Defaults.classes,
        entities = listOf(
            Entity(
                id = "id-1",
                designation = "ELD-001",
                name = "The Hollow Chorister",
                classification = "unbound",
                threat = 4,
                summary = "A choir with no source.",
                description = "Heard before it is seen.",
                abilities = listOf(Ability("Chorus Pull", "Draws listeners in.", "Only within earshot.")),
                weaknesses = listOf(Weakness("True Silence", "Cut all sound.", "hard counter")),
                containment = "Sound isolation.",
                notes = "First entry.",
                tags = listOf("sound-based", "lure"),
                related = listOf("id-2"),
                image = "QUJD",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_700_000_100_000L,
                status = "active"
            ),
            Entity(
                id = "id-2",
                designation = "ELD-002",
                name = "Pale Cartographer",
                classification = "stirring",
                threat = 2,
                summary = "Edits geography.",
                description = "A quiet presence.",
                abilities = emptyList(),
                weaknesses = emptyList(),
                containment = "Photograph the room.",
                notes = "",
                tags = listOf("spatial"),
                related = emptyList(),
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_700_000_050_000L,
                status = "archived"
            )
        )
    )

    // Encode to text, decode back, and the object must match field for field.
    @Test
    fun jsonRoundTripIsExact() {
        val original = sampleArchive()
        val text = CodexFiles.encode(original)
        val restored = CodexFiles.decode(text)
        assertEquals(original, restored)
    }

    // A file written by a future version, with an unknown field, must still import.
    @Test
    fun unknownFieldsAreIgnoredOnImport() {
        val withExtra = """
            {
              "schemaVersion": 1,
              "exportedAt": 0,
              "somethingFromTheFuture": "ignore me",
              "classes": [],
              "entities": [
                { "id": "x1", "name": "Test", "futureField": 42 }
              ]
            }
        """.trimIndent()
        val restored = CodexFiles.decode(withExtra)
        assertEquals(1, restored.entities.size)
        assertEquals("Test", restored.entities.first().name)
        // Fields the file left out fall back to their defaults.
        assertEquals(1, restored.entities.first().threat)
    }

    // The Markdown copy should render every entry with its powers and counters.
    @Test
    fun markdownContainsEveryEntry() {
        val md = MarkdownExporter.render(sampleArchive())
        assertTrue(md.contains("The Hollow Chorister"))
        assertTrue(md.contains("Pale Cartographer"))
        assertTrue(md.contains("Chorus Pull"))
        assertTrue(md.contains("True Silence"))
        assertTrue(md.contains("How to beat it"))
    }

    // The history log survives a full round trip, and the default prefix is ELND.
    @Test
    fun storyRoundTripsAndDefaultPrefix() {
        val e = Entity(
            id = "s1",
            designation = "ELND-001",
            name = "Flicker",
            story = listOf(com.elendheim.codex.codex.model.StoryEntry("First noticed", "Day one", "Near the %%old room%%."))
        )
        val back = CodexFiles.decode(CodexFiles.encode(CodexExport(entities = listOf(e))))
        assertEquals(e, back.entities.first())
        assertEquals("ELND", Defaults.designationPrefix)
    }

    // An old entry with only the single image field still shows that image through the
    // gallery fallback, and a new entry's gallery takes priority over the legacy field.
    @Test
    fun imageFallbackAndGalleryPriority() {
        val legacy = Entity(id = "a", image = "AAA")
        assertEquals(listOf("AAA"), legacy.effectiveImages())

        val gallery = Entity(id = "b", image = "AAA", images = listOf("X", "Y"))
        assertEquals(listOf("X", "Y"), gallery.effectiveImages())

        val none = Entity(id = "c")
        assertTrue(none.effectiveImages().isEmpty())
    }

    // Sharing a single entry renders just that dossier, with its class label resolved.
    @Test
    fun renderOneContainsOnlyThatEntry() {
        val archive = sampleArchive()
        val one = archive.entities.first()
        val md = MarkdownExporter.renderOne(one, archive.classes)
        assertTrue(md.contains("The Hollow Chorister"))
        assertTrue(md.contains("Unbound"))
        // The other entry must not leak into a single share.
        assertTrue(!md.contains("Pale Cartographer"))
    }
}
