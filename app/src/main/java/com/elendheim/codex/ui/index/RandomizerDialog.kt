package com.elendheim.codex.ui.index

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elendheim.codex.codex.model.Entity
import kotlinx.coroutines.delay

// The randomize animation. Designations flick past fast, then slow down and settle on
// the one you land on. You do not see the result until it stops, and the landing gets a
// small pop so it feels final. Then it opens that entry.
@Composable
fun RandomizerDialog(
    active: List<Entity>,
    reduceMotion: Boolean,
    onLand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (active.isEmpty()) return

    // The winner is chosen up front, the spin is just for show.
    val target = remember { active.random() }
    var display by remember { mutableStateOf(active.random().designation) }
    var settled by remember { mutableStateOf(false) }

    // A gentle pop on the landing.
    val scale by animateFloatAsState(
        targetValue = if (settled) 1f else 0.9f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 260),
        label = "landScale"
    )

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            // No spin when motion is reduced, just show the result and go.
            display = target.designation
            settled = true
            delay(500)
            onLand(target.id)
            return@LaunchedEffect
        }
        // Flick through random designations, slowing down as it goes.
        var wait = 45L
        repeat(26) { step ->
            display = active.random().designation
            delay(wait)
            wait += (step * 5).toLong()
        }
        // Land on the winner, hold a beat, then open it.
        display = target.designation
        settled = true
        delay(750)
        onLand(target.id)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 32.dp, vertical = 36.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (settled) "OPENING" else "SELECTING",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp
            )
            Text(
                text = display,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = if (settled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .scale(scale)
                    .alpha(if (settled) 1f else 0.85f)
            )
        }
    }
}
