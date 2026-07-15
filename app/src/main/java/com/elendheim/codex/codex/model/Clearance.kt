package com.elendheim.codex.codex.model

// The security clearance idea. The more an entry hides, the higher the clearance a
// reader needs to reveal it. This is flavor, a way to get into the mood, but it is
// driven by real counting so it stays consistent.

// The same redaction pattern the reader sees, so counting matches the display.
private val REDACTION = Regex("%%.+?%%")

private fun String.redactionSpans(): Int = REDACTION.findAll(this).count()

// Count every redacted thing on an entry: the %% spans across all text fields, plus
// any image marked redacted.
fun Entity.redactionCount(): Int {
    var count = 0
    count += summary.redactionSpans()
    count += description.redactionSpans()
    count += containment.redactionSpans()
    count += notes.redactionSpans()
    abilities.forEach { count += it.name.redactionSpans() + it.mechanism.redactionSpans() + it.limits.redactionSpans() }
    weaknesses.forEach { count += it.name.redactionSpans() + it.exploit.redactionSpans() }
    story.forEach { count += it.title.redactionSpans() + it.period.redactionSpans() + it.body.redactionSpans() }
    count += effectiveGallery().count { it.redacted }
    return count
}

// Map a redaction count to the clearance a reader needs to reveal the file. More than
// seven redactions needs the top clearance, level 5.
fun clearanceForRedactions(count: Int): Int = when {
    count == 0 -> 1
    count <= 2 -> 2
    count <= 4 -> 3
    count <= 7 -> 4
    else -> 5
}

// The clearance this entry needs before its redactions can be revealed.
fun Entity.requiredClearance(): Int = clearanceForRedactions(redactionCount())
