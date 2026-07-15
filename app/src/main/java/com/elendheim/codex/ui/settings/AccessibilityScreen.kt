package com.elendheim.codex.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.SectionLabel
import kotlin.math.abs

// Accessibility settings. Text size, contrast and motion, all applied live so the
// reader sees the effect while they choose. These make the app usable for a much
// wider range of people, not just its author.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(vm: CodexViewModel, onBack: () -> Unit) {
    val textScale by vm.textScale.collectAsState()
    val highContrast by vm.highContrast.collectAsState()
    val reduceMotion by vm.reduceMotion.collectAsState()
    val spinSeconds by vm.spinSeconds.collectAsState()

    // The text size choices, each a friendly label and its multiplier.
    val sizes = listOf(
        "Small" to 0.85f,
        "Normal" to 1.0f,
        "Large" to 1.15f,
        "Larger" to 1.3f,
        "Largest" to 1.5f
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accessibility") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Text size. The whole app grows with this, including this screen, so the
            // choice previews itself as you tap.
            SectionLabel(text = "Text size", modifier = Modifier.padding(top = 16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sizes.forEach { (label, value) ->
                    // Match on a small tolerance since these are floats.
                    val selected = abs(textScale - value) < 0.001f
                    ChoiceRow(label = label, selected = selected) { vm.setTextScale(value) }
                }
            }

            // High contrast.
            SectionLabel(text = "Contrast", modifier = Modifier.padding(top = 28.dp))
            ToggleRow(
                title = "High contrast",
                subtitle = "Brighter text and stronger lines against the dark background.",
                checked = highContrast,
                onCheckedChange = { vm.setHighContrast(it) }
            )

            // Reduce motion.
            SectionLabel(text = "Motion", modifier = Modifier.padding(top = 28.dp))
            ToggleRow(
                title = "Reduce motion",
                subtitle = "Skips the fade on the opening screen and the randomize spin.",
                checked = reduceMotion,
                onCheckedChange = { vm.setReduceMotion(it) }
            )

            // How long the randomize spin runs, one to ten seconds.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Text("Randomize spin length", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "About ${"%.1f".format(spinSeconds)} seconds before it lands.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = spinSeconds,
                    onValueChange = { vm.setSpinSeconds(it) },
                    valueRange = 1f..10f,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                text = "The quick brown fox jumps over the lazy dog.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(top = 28.dp, bottom = 48.dp)
            )
        }
    }
}

// A single selectable option in a list, with a clear selected outline.
@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (selected) {
            Text(text = "Selected", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// A titled switch row reused for the contrast and motion toggles.
@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
