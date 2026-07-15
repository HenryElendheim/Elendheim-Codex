package com.elendheim.codex.ui.index

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.elendheim.codex.codex.model.Entity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// The randomize animation. Designations flick past, and each new one gives a quick
// glow so you can feel every change. It slows down as it goes, then settles on the one
// you land on with a firm pop, the little reward at the end. Then it opens that entry.
@Composable
fun RandomizerDialog(
    active: List<Entity>,
    reduceMotion: Boolean,
    spinSeconds: Float,
    onLand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (active.isEmpty()) return

    // The winner is chosen up front, the spin is just for show.
    val target = remember { active.random() }
    var display by remember { mutableStateOf(active.random().designation) }
    var settled by remember { mutableStateOf(false) }

    // Glow pulses to full on each change, then fades. It also flashes bright on landing.
    val glow = remember { Animatable(0f) }

    // A firm pop on the landing.
    val landScale by animateFloatAsState(
        targetValue = if (settled) 1f else 0.92f,
        animationSpec = tween(durationMillis = if (reduceMotion) 0 else 300),
        label = "landScale"
    )

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            // No spin when motion is reduced, just show the result and go.
            display = target.designation
            settled = true
            glow.snapTo(1f); glow.animateTo(0f, tween(200))
            delay(500)
            onLand(target.id)
            return@LaunchedEffect
        }

        // Run for about the chosen length, slowing down as it goes so it feels like it
        // is coming to rest rather than just stopping. delay paces the flicks, and each
        // glow fades on its own coroutine so it never holds up the timing.
        val totalMs = (spinSeconds.coerceIn(1f, 10f) * 1000f).toLong()
        var elapsed = 0L
        var step = 45L
        while (elapsed < totalMs) {
            display = active.random().designation
            // A short glow on each flick so every change is felt.
            glow.snapTo(0.75f)
            launch { glow.animateTo(0f, tween(140)) }
            delay(step)
            elapsed += step
            // Ease out: each flick waits a little longer than the last, up to a cap.
            step = (step * 1.14f).toLong().coerceAtMost(360L)
        }

        // Land on the winner with a strong flash, hold a beat, then open it.
        display = target.designation
        settled = true
        glow.snapTo(1f)
        glow.animateTo(0f, tween(650))
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
            // The designation sits in a panel that lights up on each change and flashes
            // on the landing.
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f + glow.value * 0.22f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f + glow.value * 0.6f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 22.dp, vertical = 14.dp)
            ) {
                Text(
                    text = display,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = if (settled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .scale(landScale)
                        // A tiny extra swell on each glow so the change reads at a glance.
                        .graphicsLayer(scaleX = 1f + glow.value * 0.04f, scaleY = 1f + glow.value * 0.04f)
                )
            }
        }
    }
}
