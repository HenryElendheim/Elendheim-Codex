package com.elendheim.codex.codex.repo

import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.StoryEntry
import com.elendheim.codex.codex.model.Weakness
import java.util.UUID

// Three plain example entries written on first run. They are deliberately simple and
// obvious, each one showing the format with a single ability, a single weakness and a
// short history note. Every one says clearly that it is an example, so it is easy to
// rename, edit or delete and start your own archive.
object SeedData {

    fun entities(): List<Entity> {
        // Fixed base time so the seeds have a sensible order without needing a clock.
        val base = 1_700_000_000_000L
        val note = "This is an example entry to show the format. Rename it, edit it, or delete it whenever you like."

        return listOf(
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELND-001",
                name = "Example: The Flicker",
                classification = "dormant",
                threat = 1,
                summary = "Lights dim and flicker whenever it is near. Harmless so far.",
                description = "A simple starter example. Nearby lights flicker when it is close, " +
                    "which makes it easy to notice and easy to track.",
                abilities = listOf(
                    Ability(
                        name = "Dim the lights",
                        mechanism = "Weakens nearby electric light while it is within a room.",
                        limits = "Only affects lights in the same room. Stops the moment it leaves."
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
                        period = "Day one",
                        body = "Found because the hallway lights kept flickering near the %%old archive room%%. " +
                            "Nothing else seemed out of place. See [ELND-002]."
                    )
                ),
                tags = listOf("example", "lights"),
                related = emptyList(),
                createdAt = base,
                updatedAt = base,
                status = "active"
            ),
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELND-002",
                name = "Example: The Hollow Door",
                classification = "stirring",
                threat = 2,
                summary = "A door that appears on a wall where there was none.",
                description = "Another simple example. A plain door shows up on a blank wall. It opens, " +
                    "but there is only a shallow empty space behind it.",
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
                        period = "Week two",
                        body = "A staff member opened it and found only %%bare concrete%% behind. It was gone " +
                            "an hour later."
                    )
                ),
                tags = listOf("example", "spatial"),
                related = emptyList(),
                createdAt = base + 1000,
                updatedAt = base + 1000,
                status = "active"
            ),
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELND-003",
                name = "Example: The Echo",
                classification = "dormant",
                threat = 1,
                summary = "Repeats the last thing it hears, a few seconds later.",
                description = "The last simple example. It waits, then repeats the last sentence it heard " +
                    "in a flat copy of the speaker's voice.",
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
                containment = "A quiet room is enough. No special measures needed.",
                notes = note,
                story = listOf(
                    StoryEntry(
                        title = "Logged for the record",
                        period = "Week three",
                        body = "Kept as a calm example next to [ELND-001] and [ELND-002]."
                    )
                ),
                tags = listOf("example", "sound"),
                related = emptyList(),
                createdAt = base + 2000,
                updatedAt = base + 2000,
                status = "active"
            )
        )
    }
}
