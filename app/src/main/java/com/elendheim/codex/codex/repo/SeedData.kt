package com.elendheim.codex.codex.repo

import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.Weakness
import java.util.UUID

// A small set of example dossiers written on first run so the archive shows what a
// filled in entry looks like. They can be edited or archived like any other entry.
object SeedData {

    fun entities(): List<Entity> {
        // Fixed base time so the seeds have sensible ordering without needing a clock.
        val base = 1_700_000_000_000L
        return listOf(
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELD-001",
                name = "The Hollow Chorister",
                classification = "unbound",
                threat = 4,
                summary = "A sound that wears the shape of a choir and pulls listeners toward it.",
                description = "Heard before it is seen. Appears as a smear of overlapping voices " +
                    "with no source. Those who follow the sound report losing whole hours.",
                abilities = listOf(
                    Ability(
                        name = "Chorus Pull",
                        mechanism = "Layers a false memory of safety onto its song so listeners " +
                            "walk toward it without deciding to.",
                        limits = "Only works within earshot. Breaks if the listener cannot hear it."
                    )
                ),
                weaknesses = listOf(
                    Weakness(
                        name = "True Silence",
                        exploit = "Full sensory silence, ear protection plus a quiet room, cuts the " +
                            "pull instantly and leaves it powerless.",
                        severity = "hard counter"
                    )
                ),
                containment = "Keep it behind sound isolation. Never route audio out of its room.",
                notes = "First logged entry. Origin unknown. Suspected link to ELD-002.",
                tags = listOf("sound-based", "lure"),
                related = emptyList(),
                createdAt = base,
                updatedAt = base,
                status = "active"
            ),
            Entity(
                id = UUID.randomUUID().toString(),
                designation = "ELD-002",
                name = "Pale Cartographer",
                classification = "stirring",
                threat = 2,
                summary = "Redraws small parts of a place while no one is looking at them.",
                description = "A quiet presence that edits geography. A door moves a metre. A stair " +
                    "gains a step. Changes are always minor and always deniable.",
                abilities = listOf(
                    Ability(
                        name = "Quiet Revision",
                        mechanism = "Alters an unobserved detail of a room so the change is real once " +
                            "someone looks again.",
                        limits = "Cannot change anything under direct observation."
                    )
                ),
                weaknesses = listOf(
                    Weakness(
                        name = "Constant Watch",
                        exploit = "Two observers covering all exits stop any revision from taking hold.",
                        severity = "mitigation"
                    )
                ),
                containment = "Map the room, photograph it, and compare on a schedule.",
                notes = "Harmless so far. Watch for escalation in scale.",
                tags = listOf("spatial", "subtle"),
                related = emptyList(),
                createdAt = base + 1000,
                updatedAt = base + 1000,
                status = "active"
            )
        )
    }
}
