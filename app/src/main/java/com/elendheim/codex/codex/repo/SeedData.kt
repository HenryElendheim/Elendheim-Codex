package com.elendheim.codex.codex.repo

import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.GalleryImage
import com.elendheim.codex.codex.model.StoryEntry
import com.elendheim.codex.codex.model.Weakness
import java.util.UUID

// Three plain example entries written on first run. They are deliberately simple and
// obvious, each showing the format with a single ability, a single weakness, a few
// short stories and a small gallery. One image is marked redacted so that feature is
// visible, and a few phrases use %% so text redaction shows too. Measurements are in
// meters and Celsius with feet and Fahrenheit in parentheses. Every entry says clearly
// that it is an example, so it is easy to rename, edit or delete and start your own.
object SeedData {

    // The four placeholder images. redactLast marks the last one redacted to show how a
    // hidden image looks while redaction mode is on.
    private fun gallery(redactLast: Boolean): List<GalleryImage> =
        SeedImages.defaultGallery.mapIndexed { i, data ->
            GalleryImage(data = data, redacted = redactLast && i == SeedImages.defaultGallery.lastIndex)
        }

    fun entities(): List<Entity> {
        // Fixed base time so the seeds have a sensible order without needing a clock.
        val base = 1_700_000_000_000L
        val note = "This is an example entry to show the format. Rename it, edit it, or delete it whenever you like."

        return listOf(
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELND-001",
                name = "Example: The Flicker",
                classification = "clam",
                threat = 1,
                summary = "Lights dim and flicker whenever it is near. Harmless so far.",
                description = "A simple starter example. Nearby lights flicker when it is close, which " +
                    "makes it easy to notice. It was first seen in the %%old archive room%% and has " +
                    "stayed calm since.",
                abilities = listOf(
                    Ability(
                        name = "Dim the lights",
                        mechanism = "Weakens nearby electric light while it is within a room.",
                        limits = "Only affects lights in the same room. Range is about %%4 meters (13 feet)%%."
                    )
                ),
                weaknesses = listOf(
                    Weakness(
                        name = "Daylight",
                        exploit = "It has no effect in natural daylight, so open a window or step outside.",
                        severity = "hard counter"
                    )
                ),
                containment = "Keep it in a room lit by daylight or battery lamps it cannot reach.",
                notes = note,
                story = listOf(
                    StoryEntry(
                        title = "First noticed",
                        period = "03.01.2024",
                        body = "The hallway lights kept flickering near the %%old archive room%%. Nothing " +
                            "else seemed out of place. See [ELND-002]."
                    ),
                    StoryEntry(
                        title = "A quiet week",
                        period = "10.01.2024",
                        body = "Seven days with no change. It sat in its room and dimmed the lamp now and then."
                    )
                ),
                tags = listOf("example", "lights"),
                related = emptyList(),
                image = SeedImages.image1,
                images = SeedImages.defaultGallery,
                pictures = gallery(redactLast = true),
                createdAt = base,
                updatedAt = base,
                status = "active"
            ),
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELND-002",
                name = "Example: The Hollow Door",
                classification = "rekna",
                threat = 2,
                summary = "A door that appears on a wall where there was none.",
                description = "Another simple example. A plain door shows up on a blank wall. It opens, " +
                    "but there is only a shallow empty space behind it, about %%0.5 meters (20 inches)%% deep.",
                abilities = listOf(
                    Ability(
                        name = "Appear",
                        mechanism = "Places itself on any flat wall when no one is looking at that wall.",
                        limits = "Cannot appear on a wall someone is watching."
                    )
                ),
                weaknesses = listOf(
                    Weakness(
                        name = "Keep watching",
                        exploit = "Watch the wall and it never appears. A camera on the wall works too.",
                        severity = "mitigation"
                    )
                ),
                containment = "Point a camera at the walls of its room and check the footage daily.",
                notes = note,
                story = listOf(
                    StoryEntry(
                        title = "Opened once",
                        period = "18.01.2024",
                        body = "A staff member opened it and found only %%bare concrete%% behind. It was gone " +
                            "an hour later."
                    ),
                    StoryEntry(
                        title = "Moved rooms",
                        period = "01.02.2024",
                        body = "It stopped appearing in the store room and started showing up in the %%stairwell%% instead."
                    )
                ),
                tags = listOf("example", "spatial"),
                related = emptyList(),
                image = SeedImages.image1,
                images = SeedImages.defaultGallery,
                pictures = gallery(redactLast = false),
                createdAt = base + 1000,
                updatedAt = base + 1000,
                status = "active"
            ),
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELND-003",
                name = "Example: The Echo",
                classification = "clam",
                threat = 1,
                summary = "Repeats the last thing it hears, a few seconds later.",
                description = "The last simple example. It waits, then repeats the last sentence it heard " +
                    "in a flat copy of the speaker's voice. The delay is usually about six seconds.",
                abilities = listOf(
                    Ability(
                        name = "Repeat",
                        mechanism = "Plays back the last sentence it heard after a short delay.",
                        limits = "Only the most recent sentence. Forgets it once it repeats it."
                    )
                ),
                weaknesses = listOf(
                    Weakness(
                        name = "Silence",
                        exploit = "Say nothing near it and there is nothing for it to repeat.",
                        severity = "situational"
                    )
                ),
                containment = "A quiet room kept cool, below about 20 C (68 F), is enough. No special measures needed.",
                notes = note,
                story = listOf(
                    StoryEntry(
                        title = "Logged for the record",
                        period = "25.01.2024",
                        body = "Kept as a calm example next to [ELND-001] and [ELND-002]."
                    )
                ),
                tags = listOf("example", "sound"),
                related = emptyList(),
                image = SeedImages.image1,
                images = SeedImages.defaultGallery,
                pictures = gallery(redactLast = false),
                createdAt = base + 2000,
                updatedAt = base + 2000,
                status = "active"
            )
        )
    }
}
