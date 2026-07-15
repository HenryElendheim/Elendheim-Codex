package com.elendheim.codex.ui.index

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.IndexFilters
import com.elendheim.codex.ui.SortOrder
import kotlin.math.roundToInt

// The row of filter and sort controls under the search box. Sort and class still use a
// small dropdown. Threat uses a slider in a little dialog. Tags open their own page,
// which matters once the archive has hundreds of them.
@Composable
fun IndexControls(
    vm: CodexViewModel,
    classes: List<EntityClass>,
    tags: List<String>,
    filters: IndexFilters,
    onOpenTagFilter: () -> Unit
) {
    val anyActive = filters.classFilter != null || filters.threatFilter != null ||
        filters.tagFilter != null || filters.query.isNotBlank()

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sort order.
        val sortLabel = when (filters.sort) {
            SortOrder.DesignationAsc -> "Designation"
            SortOrder.UpdatedDesc -> "Recent"
            SortOrder.ThreatDesc -> "Threat"
        }
        FilterPill(label = "Sort: $sortLabel", active = false) { dismiss ->
            DropdownMenuItem(text = { Text("Designation") }, onClick = { vm.setSort(SortOrder.DesignationAsc); dismiss() })
            DropdownMenuItem(text = { Text("Recently updated") }, onClick = { vm.setSort(SortOrder.UpdatedDesc); dismiss() })
            DropdownMenuItem(text = { Text("Threat, high first") }, onClick = { vm.setSort(SortOrder.ThreatDesc); dismiss() })
        }

        // Class filter.
        val classLabel = filters.classFilter?.let { key -> classes.firstOrNull { it.key == key }?.label ?: key } ?: "Class"
        FilterPill(label = classLabel, active = filters.classFilter != null) { dismiss ->
            DropdownMenuItem(text = { Text("All classes") }, onClick = { vm.setClassFilter(null); dismiss() })
            classes.forEach { c ->
                DropdownMenuItem(text = { Text(c.label) }, onClick = { vm.setClassFilter(c.key); dismiss() })
            }
        }

        // Threat filter, chosen with a slider from 1 to 5. Any clears it.
        ThreatPill(
            current = filters.threatFilter,
            onAny = { vm.setThreatFilter(null) },
            onPick = { vm.setThreatFilter(it) }
        )

        // Tag filter opens its own searchable page, only worth offering once tags exist.
        if (tags.isNotEmpty()) {
            ActionPill(
                label = filters.tagFilter ?: "Tag",
                active = filters.tagFilter != null,
                onClick = onOpenTagFilter
            )
        }

        // Clear everything.
        if (anyActive) {
            PlainPill(label = "Clear") { vm.clearFilters() }
        }
    }
}

// A pill that opens a dropdown menu of options. The menu content is supplied by the
// caller and given a dismiss callback to close after a choice.
@Composable
private fun FilterPill(
    label: String,
    active: Boolean,
    menuContent: @Composable (dismiss: () -> Unit) -> Unit
) {
    var open by remember { mutableStateOf(false) }
    val border = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { open = true }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textColor, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = textColor)
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            menuContent { open = false }
        }
    }
}

// A pill that just runs an action on tap, used for the tag page and, with a dialog, the
// threat slider.
@Composable
private fun ActionPill(label: String, active: Boolean, onClick: () -> Unit) {
    val border = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = textColor, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
    }
}

// The threat pill opens a small dialog with a slider from 1 to 5, plus an Any button to
// clear it. A dialog keeps the slider out of the horizontally scrolling row, so
// sliding never fights the scroll.
@Composable
private fun ThreatPill(current: Int?, onAny: () -> Unit, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ActionPill(label = current?.let { "Threat $it" } ?: "Threat", active = current != null) { open = true }

    if (open) {
        var value by remember { mutableStateOf((current ?: 3).toFloat()) }
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Filter by threat") },
            text = {
                Column {
                    Text(
                        text = "Threat ${value.roundToInt()} of 5",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = value,
                        onValueChange = { value = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onPick(value.roundToInt()); open = false }) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { onAny(); open = false }) { Text("Any") }
            }
        )
    }
}

// A pill with no border or menu, used for the clear action.
@Composable
private fun PlainPill(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium)
    }
}
