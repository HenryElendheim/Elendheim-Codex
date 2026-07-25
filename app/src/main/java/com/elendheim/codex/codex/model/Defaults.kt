package com.elendheim.codex.codex.model

// The starting classification scheme. It is only a starting point, the user can
// rename, recolor, add or remove tiers in settings. Threat 1 to 5 is a separate
// axis from class on purpose: how dangerous is not the same as how hard to contain.
object Defaults {
    val classes: List<EntityClass> = listOf(
        EntityClass("clam", "Clam", "#5B8C7B", "Easy to contain. Not always safe, just well understood and reliably held."),
        EntityClass("rekna", "Rekna", "#C9A227", "Showing activity. Watch closely, effects are spreading."),
        EntityClass("rinner", "Rinner", "#C4383A", "Loose and acting on its own. Direct threat."),
        EntityClass("heimer", "Heimer", "#8A2BE2", "Beyond current means to contain. Handle with extreme care."),
        EntityClass("neutralized", "Neutralized", "#9A9AA2", "No longer exists. Dead and or gone for good.")
    )

    // The prefix used when the app auto numbers a new dossier, for example ELND-014.
    const val designationPrefix: String = "ELND"

    // The name shown at the top of the archive and on the about screen. This is only a
    // starting point, anyone can rename it in settings to make the app their own.
    const val archiveName: String = "Elendheim Codex"
}
