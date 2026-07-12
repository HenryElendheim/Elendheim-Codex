package com.elendheim.codex.ui.index

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

// The row of filter and sort controls under the search box. Each control is a small
// pill that opens a menu. A clear pill appears when any filter is active.
@Composable
fun IndexControls(
    vm: CodexViewModel,
    classes: List<EntityClass>,
    tags: List<String>,
    filters: IndexFilters
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

        // Threat filter.
        FilterPill(label = filters.threatFilter?.let { "Threat $it" } ?: "Threat", active = filters.threatFilter != null) { dismiss ->
            DropdownMenuItem(text = { Text("Any threat") }, onClick = { vm.setThreatFilter(null); dismiss() })
            (1..5).forEach { level ->
                DropdownMenuItem(text = { Text("Threat $level") }, onClick = { vm.setThreatFilter(level); dismiss() })
            }
        }

        // Tag filter, only useful once tags exist.
        if (tags.isNotEmpty()) {
            FilterPill(label = filters.tagFilter ?: "Tag", active = filters.tagFilter != null) { dismiss ->
                DropdownMenuItem(text = { Text("Any tag") }, onClick = { vm.setTagFilter(null); dismiss() })
                tags.forEach { tag ->
                    DropdownMenuItem(text = { Text(tag) }, onClick = { vm.setTagFilter(tag); dismiss() })
                }
            }
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

// A pill with no menu, used for the clear action.
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
