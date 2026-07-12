package com.elendheim.codex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.ui.theme.CodexTextFaint

// Turn a stored colour string like #C4383A into a Compose Color. Falls back to a
// neutral grey if the text is ever malformed, so a bad value never crashes a screen.
fun parseHex(hex: String): Color = runCatching {
    val clean = hex.removePrefix("#")
    val value = clean.toLong(16)
    when (clean.length) {
        6 -> Color(0xFF000000 or value)
        8 -> Color(value)
        else -> Color(0xFF9A9AA2)
    }
}.getOrDefault(Color(0xFF9A9AA2))

// A small coloured pill showing an entity's class. The colour comes from the class
// definition, so the user's own scheme drives the look.
@Composable
fun ClassChip(classKey: String, classes: List<EntityClass>, modifier: Modifier = Modifier) {
    val cls = classes.firstOrNull { it.key == classKey }
    val label = cls?.label ?: classKey.ifBlank { "Unclassed" }
    val color = cls?.let { parseHex(it.colorHex) } ?: Color(0xFF9A9AA2)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}

// Five dots showing the threat rating, filled up to the entity's level. A quick read
// that keeps the danger axis separate from the class axis.
@Composable
fun ThreatPips(threat: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (level in 1..5) {
            val filled = level <= threat
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else CodexTextFaint.copy(alpha = 0.35f)
                    )
            )
        }
    }
}

// A small uppercase label used to head a section in the reading and editing screens.
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 1.sp,
        modifier = modifier.padding(bottom = 6.dp)
    )
}
