package com.elendheim.codex.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.SectionLabel
import com.elendheim.codex.ui.components.parseHex

// A small dashboard for the whole archive. Counts at a glance, then simple bars for
// how entries spread across classes and threat levels. All worked out here in the
// screen from the live data, no extra storage needed.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OverviewScreen(vm: CodexViewModel, onBack: () -> Unit) {
    val byId by vm.entitiesById.collectAsState()
    val classes by vm.classes.collectAsState()

    val all = byId.values.toList()
    val active = all.filter { it.status == "active" }
    val archived = all.filter { it.status == "archived" }
    val tagCount = all.flatMap { it.tags }.distinct().size

    // Two ways to read the same numbers: bars or a wall of blocks. The choice makes it
    // easier to read for different eyes.
    var blocks by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overview") },
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
            // Top line counts.
            Row(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(label = "Active", value = active.size.toString(), modifier = Modifier.weight(1f))
                StatTile(label = "Archived", value = archived.size.toString(), modifier = Modifier.weight(1f))
                StatTile(label = "Tags", value = tagCount.toString(), modifier = Modifier.weight(1f))
            }

            // View switch: bars or blocks.
            Row(
                modifier = Modifier.padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewToggle(label = "Bars", selected = !blocks) { blocks = false }
                ViewToggle(label = "Blocks", selected = blocks) { blocks = true }
            }

            // Spread across classes, active entries only.
            SectionLabel(text = "By class", modifier = Modifier.padding(top = 28.dp))
            if (active.isEmpty()) {
                EmptyNote()
            } else {
                val maxByClass = classes.maxOfOrNull { c -> active.count { it.classification == c.key } } ?: 0
                if (blocks) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        classes.forEach { c ->
                            BlockTile(
                                label = c.label,
                                count = active.count { it.classification == c.key },
                                color = parseHex(c.colorHex)
                            )
                        }
                    }
                } else {
                    classes.forEach { c ->
                        val count = active.count { it.classification == c.key }
                        BarRow(
                            label = c.label,
                            count = count,
                            fraction = if (maxByClass == 0) 0f else count.toFloat() / maxByClass,
                            color = parseHex(c.colorHex)
                        )
                    }
                }
            }

            // Spread across threat levels.
            SectionLabel(text = "By threat", modifier = Modifier.padding(top = 28.dp))
            if (active.isEmpty()) {
                EmptyNote()
            } else {
                val maxByThreat = (1..5).maxOf { level -> active.count { it.threat == level } }
                if (blocks) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        (1..5).forEach { level ->
                            BlockTile(
                                label = "Threat $level",
                                count = active.count { it.threat == level },
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    (1..5).forEach { level ->
                        val count = active.count { it.threat == level }
                        BarRow(
                            label = "Threat $level",
                            count = count,
                            fraction = if (maxByThreat == 0) 0f else count.toFloat() / maxByThreat,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Box(modifier = Modifier.padding(bottom = 48.dp))
        }
    }
}

// A small selectable pill for switching between bars and blocks.
@Composable
private fun ViewToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace
        )
    }
}

// One block in the blocks view: a colored panel with a big count and a label under it.
@Composable
private fun BlockTile(label: String, count: Int, color: Color) {
    Column(
        modifier = Modifier
            .width(104.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// One big number with a small label under it.
@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// A labeled bar. The track is faint, the fill shows the share for this row.
@Composable
private fun BarRow(label: String, count: Int, fraction: Float, color: Color) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            // The fill width is this row's share of the largest count. A zero count
            // draws no fill and just leaves the faint track.
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun EmptyNote() {
    Text(
        text = "No active entries yet.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp)
    )
}
