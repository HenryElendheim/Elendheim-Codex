package com.elendheim.codex.ui.editor

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.codex.model.Weakness
import com.elendheim.codex.ui.components.ClassChip

// A labelled text field used throughout the editor. Kept in one place so every field
// looks the same.
@Composable
fun EditorField(
    label: String,
    value: String,
    onValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

// Pick a classification tier from the user defined list.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassPicker(classes: List<EntityClass>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        classes.forEach { c ->
            val isSelected = c.key == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelect(c.key) }
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                ClassChip(classKey = c.key, classes = classes)
            }
        }
    }
}

// Pick a threat level from one to five as a simple segmented row.
@Composable
fun ThreatPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { level ->
            val isSelected = level == selected
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .clickable { onSelect(level) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$level",
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// Add, edit, remove and reorder the structured ability rows.
@Composable
fun AbilityEditor(abilities: List<Ability>, onChange: (List<Ability>) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ABILITIES",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = { onChange(abilities + Ability()) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add")
            }
        }
        abilities.forEachIndexed { index, ability ->
            RowCard(
                index = index,
                count = abilities.size,
                onRemove = { onChange(abilities.toMutableList().apply { removeAt(index) }) },
                onMoveUp = { onChange(abilities.swap(index, index - 1)) },
                onMoveDown = { onChange(abilities.swap(index, index + 1)) }
            ) {
                EditorField("Name", ability.name, { v -> onChange(abilities.replaceAt(index, ability.copy(name = v))) }, singleLine = true)
                EditorField("How it works", ability.mechanism, { v -> onChange(abilities.replaceAt(index, ability.copy(mechanism = v))) }, minLines = 2)
                EditorField("Limits", ability.limits, { v -> onChange(abilities.replaceAt(index, ability.copy(limits = v))) }, minLines = 1)
            }
        }
    }
}

// Add, edit, remove and reorder the structured weakness rows.
@Composable
fun WeaknessEditor(weaknesses: List<Weakness>, onChange: (List<Weakness>) -> Unit) {
    val severities = listOf("hard counter", "mitigation", "situational")
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "HOW TO BEAT IT",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = { onChange(weaknesses + Weakness()) }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("Add")
            }
        }
        weaknesses.forEachIndexed { index, weakness ->
            RowCard(
                index = index,
                count = weaknesses.size,
                onRemove = { onChange(weaknesses.toMutableList().apply { removeAt(index) }) },
                onMoveUp = { onChange(weaknesses.swap(index, index - 1)) },
                onMoveDown = { onChange(weaknesses.swap(index, index + 1)) }
            ) {
                EditorField("Name", weakness.name, { v -> onChange(weaknesses.replaceAt(index, weakness.copy(name = v))) }, singleLine = true)
                EditorField("Exploit", weakness.exploit, { v -> onChange(weaknesses.replaceAt(index, weakness.copy(exploit = v))) }, minLines = 2)
                DropdownField(
                    label = "Severity",
                    value = weakness.severity,
                    options = severities,
                    onSelect = { v -> onChange(weaknesses.replaceAt(index, weakness.copy(severity = v))) }
                )
            }
        }
    }
}

// A card wrapping one structured row with remove and reorder controls.
@Composable
private fun RowCard(
    index: Int,
    count: Int,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#${index + 1}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )
            Row {
                // Reorder controls, disabled at the ends where they would do nothing.
                IconButton(onClick = onMoveUp, enabled = index > 0) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                }
                IconButton(onClick = onMoveDown, enabled = index < count - 1) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove")
                }
            }
        }
        content()
    }
}

// A dropdown styled like a text field, used for severity.
@Composable
fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                .clickable { open = true }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = value, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); open = false })
                }
            }
        }
    }
}

// Add and remove freeform tags. Type a tag, press add, tap a chip to remove it.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditor(tags: List<String>, onChange: (List<String>) -> Unit) {
    var input by remember { mutableStateOf("") }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Add a tag") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(onClick = {
                val clean = input.trim()
                // Only add non blank tags that are not already present.
                if (clean.isNotBlank() && tags.none { it.equals(clean, ignoreCase = true) }) {
                    onChange(tags + clean)
                }
                input = ""
            }) { Text("Add") }
        }
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onChange(tags - tag) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = tag, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.Filled.Close, contentDescription = "Remove tag", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// Pick which other dossiers this one is related to. Tapping a name toggles the link.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RelatedEditor(selfId: String, selected: List<String>, all: List<Entity>, onChange: (List<String>) -> Unit) {
    // Never offer to link an entry to itself.
    val others = all.filter { it.id != selfId }.sortedBy { it.designation }
    if (others.isEmpty()) {
        Text(
            text = "No other entries to link yet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        others.forEach { entity ->
            val isSelected = selected.contains(entity.id)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        onChange(if (isSelected) selected - entity.id else selected + entity.id)
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = entity.designation, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                Text(text = entity.name.ifBlank { "Unnamed" }, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Small list helpers that return a new list, keeping the editor state immutable.
private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    toMutableList().also { it[index] = value }

private fun <T> List<T>.swap(a: Int, b: Int): List<T> {
    if (a < 0 || b < 0 || a >= size || b >= size) return this
    return toMutableList().also { val tmp = it[a]; it[a] = it[b]; it[b] = tmp }
}
