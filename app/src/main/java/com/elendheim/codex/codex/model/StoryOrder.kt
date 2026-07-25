package com.elendheim.codex.codex.model

// Story dates are written as dd.mm.yyyy. These helpers parse that shape, order stories
// by it, and match a search against the date or the title. Kept here as pure logic so
// it is easy to test.

private val DATE = Regex("""^(\d{2})\.(\d{2})\.(\d{4})$""")

// A sortable key from a dd.mm.yyyy date, or null if the text is not a full date. The
// key packs year, month and day so plain number order is date order.
fun storyDateKey(period: String): Long? {
    val m = DATE.find(period.trim()) ?: return null
    val (d, mo, y) = m.destructured
    return y.toLong() * 10000L + mo.toLong() * 100L + d.toLong()
}

// Shape whatever was typed into the dd.mm.yyyy pattern: keep only digits, cap at eight,
// and drop the dots in at the right spots. This is what stops a date being written any
// other way, the field simply cannot hold anything else.
fun shapeDateInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.take(8)
    val sb = StringBuilder()
    for (i in digits.indices) {
        if (i == 2 || i == 4) sb.append('.')
        sb.append(digits[i])
    }
    return sb.toString()
}

// True if the query appears in the story's date or title, ignoring case. An empty
// query matches everything. Searching just a year, like 2003, still finds the story.
fun StoryEntry.matchesQuery(query: String): Boolean {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return true
    return period.lowercase().contains(q) || title.lowercase().contains(q)
}

// Stories in date order, oldest first. Anything without a real date sits at the end,
// keeping its original order. Returns pairs of the original index and the story, so the
// caller can still open the right one after sorting.
fun List<StoryEntry>.orderedByDate(): List<Pair<Int, StoryEntry>> =
    withIndex()
        .sortedWith(compareBy { storyDateKey(it.value.period) ?: Long.MAX_VALUE })
        .map { it.index to it.value }
