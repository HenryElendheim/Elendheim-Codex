package com.elendheim.codex.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import com.elendheim.codex.codex.model.Entity

// Renders a freeform field with two bits of light markup:
//   [ELD-007]   -> a tappable link to that dossier, when it exists
//   %%secret%%  -> shown as solid blocks when redaction mode is on, real text when off
// The stored text always keeps the real words, redaction is display only.
@Composable
fun RichCodexText(
    text: String,
    byDesignation: Map<String, Entity>,
    redact: Boolean,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val bodyColor = MaterialTheme.colorScheme.onSurface

    // One pass over the text, matching either a link or a redaction span.
    val token = Regex("""(\[[A-Za-z][A-Za-z0-9]*-\d+])|(%%.+?%%)""")

    val annotated = buildAnnotatedString {
        var cursor = 0
        for (match in token.findAll(text)) {
            // Plain text sitting before this match.
            if (match.range.first > cursor) append(text.substring(cursor, match.range.first))

            val raw = match.value
            if (raw.startsWith("[")) {
                // A cross link. Strip the brackets to get the designation.
                val designation = raw.substring(1, raw.length - 1)
                val target = byDesignation[designation]
                if (target != null) {
                    withLink(
                        LinkAnnotation.Clickable(
                            tag = target.id,
                            styles = TextLinkStyles(SpanStyle(color = linkColor, fontWeight = FontWeight.Medium)),
                            linkInteractionListener = { onOpen(target.id) }
                        )
                    ) { append(raw) }
                } else {
                    // No such entry, leave the text as written.
                    append(raw)
                }
            } else {
                // A redaction span. Inner text is between the %% markers.
                val inner = raw.substring(2, raw.length - 2)
                if (redact) {
                    // Blocks roughly the width of the hidden phrase, capped so a long
                    // passage does not fill the screen.
                    append("█".repeat(inner.length.coerceIn(2, 24)))
                } else {
                    append(inner)
                }
            }
            cursor = match.range.last + 1
        }
        // Whatever trails after the last match.
        if (cursor < text.length) append(text.substring(cursor))
    }

    Text(text = annotated, modifier = modifier, style = style, color = bodyColor)
}
