package com.elendheim.codex.ui.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.SectionLabel
import com.elendheim.codex.ui.components.parseHex

// One entry in a chart: a label, its count, and the color to draw it.
private data class ChartItem(val label: String, val count: Int, val color: Color)

// A small dashboard for the whole archive. Counts at a glance, then the same numbers
// drawn as row bars, column bars, a stacked bar or blocks. The choice is remembered
// so it opens the way you left it.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OverviewScreen(vm: CodexViewModel, onBack: () -> Unit) {
    val byId by vm.entitiesById.collectAsState()
    val classes by vm.classes.collectAsState()
    val mode by vm.overviewMode.collectAsState()

    val all = byId.values.toList()
    val active = all.filter { it.status == "active" }
    val archived = all.filter { it.status == "archived" }
    val tagCount = all.flatMap { it.tags }.distinct().size

    val accent = MaterialTheme.colorScheme.primary
    val classItems = classes.map { c -> ChartItem(c.label, active.count { it.classification == c.key }, parseHex(c.colorHex)) }
    // Threat shades get more solid as the level rises, so higher threat reads stronger.
    val threatItems = (1..5).map { level ->
        ChartItem("T$level", active.count { it.threat == level }, accent.copy(alpha = 0.40f + 0.12f * (level - 1)))
    }

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

            // View switch. Scrolls sideways so all four styles fit on any width.
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewToggle("Row", mode == "row") { vm.setOverviewMode("row") }
                ViewToggle("Column", mode == "column") { vm.setOverviewMode("column") }
                ViewToggle("Stacked", mode == "stacked") { vm.setOverviewMode("stacked") }
                ViewToggle("Blocks", mode == "blocks") { vm.setOverviewMode("blocks") }
            }

            SectionLabel(text = "By class", modifier = Modifier.padding(top = 28.dp))
            if (active.isEmpty()) EmptyNote() else ChartSection(mode, classItems)

            SectionLabel(text = "By threat", modifier = Modifier.padding(top = 28.dp))
            if (active.isEmpty()) EmptyNote() else ChartSection(mode, threatItems)

            Box(modifier = Modifier.padding(bottom = 48.dp))
        }
    }
}

// Draws one set of items in whichever style is chosen.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChartSection(mode: String, items: List<ChartItem>) {
    when (mode) {
        "column" -> ColumnChart(items)
        "stacked" -> StackedBar(items)
        "blocks" -> FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items.forEach { BlockTile(it.label, it.count, it.color) }
        }
        else -> {
            val max = (items.maxOfOrNull { it.count } ?: 0)
            items.forEach { item ->
                BarRow(item.label, item.count, if (max == 0) 0f else item.count.toFloat() / max, item.color)
            }
        }
    }
}

// Vertical bars with the count on top and the label below, like a column chart.
@Composable
private fun ColumnChart(items: List<ChartItem>) {
    val max = (items.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                // A fixed tall area so every bar shares the same baseline.
                Box(
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(0.72f).height(150.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val frac = item.count.toFloat() / max
                    val barH = (150f * frac).coerceAtLeast(if (item.count > 0) 4f else 0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barH.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(item.color)
                    )
                }
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// One horizontal bar split into colored segments, with a legend under it.
@Composable
private fun StackedBar(items: List<ChartItem>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        items.filter { it.count > 0 }.forEach { item ->
            Box(
                modifier = Modifier
                    .weight(item.count.toFloat())
                    .fillMaxHeight()
                    .background(item.color)
            )
        }
    }
    Column(modifier = Modifier.padding(top = 12.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(item.color))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp).weight(1f)
                )
                Text(
                    text = item.count.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// A small selectable pill for switching chart styles.
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
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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

// A labeled horizontal bar. The track is faint, the fill shows the share for this row.
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
