package com.elendheim.codex.ui.dossier

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.codex.model.Weakness
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.ClassChip
import com.elendheim.codex.ui.components.SectionLabel
import com.elendheim.codex.ui.components.ThreatPips

// The reading view. Renders one dossier like a classified file: header, description,
// powers, how to beat it, containment, notes, tags and links to related entries.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DossierScreen(
    vm: CodexViewModel,
    entityId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenRelated: (String) -> Unit
) {
    // Read the dossier straight from the live map so any edit shows at once.
    val byId by vm.entitiesById.collectAsState()
    val classes by vm.classes.collectAsState()
    val entity = byId[entityId]

    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = entity?.designation ?: "",
                        fontFamily = FontFamily.Monospace
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (entity != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            if (entity.status == "active") {
                                DropdownMenuItem(
                                    text = { Text("Move to archive") },
                                    onClick = { menuOpen = false; vm.archive(entity.id); onBack() }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Restore") },
                                    onClick = { menuOpen = false; vm.restore(entity.id) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete for good") },
                                    onClick = { menuOpen = false; confirmDelete = true }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (entity == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This entry is not available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Header: name, class and threat.
            Text(
                text = entity.name.ifBlank { "Unnamed" },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ClassChip(classKey = entity.classification, classes = classes)
                ThreatPips(threat = entity.threat)
                Text(
                    text = "Threat ${entity.threat} of 5",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (entity.summary.isNotBlank()) {
                Text(
                    text = entity.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            TextSection(label = "Description", body = entity.description)

            if (entity.abilities.isNotEmpty()) {
                SectionLabel(text = "Abilities", modifier = Modifier.padding(top = 20.dp))
                entity.abilities.forEach { AbilityBlock(it) }
            }

            if (entity.weaknesses.isNotEmpty()) {
                SectionLabel(text = "How to beat it", modifier = Modifier.padding(top = 20.dp))
                entity.weaknesses.forEach { WeaknessBlock(it) }
            }

            TextSection(label = "Containment", body = entity.containment)
            TextSection(label = "Notes", body = entity.notes)

            if (entity.tags.isNotEmpty()) {
                SectionLabel(text = "Tags", modifier = Modifier.padding(top = 20.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    entity.tags.forEach { TagPill(it) }
                }
            }

            if (entity.related.isNotEmpty()) {
                SectionLabel(text = "Related", modifier = Modifier.padding(top = 20.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    entity.related.forEach { relId ->
                        val rel = byId[relId]
                        if (rel != null) {
                            RelatedPill(rel) { onOpenRelated(relId) }
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(bottom = 32.dp))
        }
    }

    if (confirmDelete && entity != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete for good") },
            text = { Text("This removes ${entity.designation} permanently. Export first if you might want it back.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.deleteForever(entity.id); onBack() }) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

// A labelled block of freeform text, skipped entirely when the field is empty.
@Composable
private fun TextSection(label: String, body: String) {
    if (body.isBlank()) return
    Column(modifier = Modifier.padding(top = 20.dp)) {
        SectionLabel(text = label)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// One power rendered as a titled block: name, how it works, limits.
@Composable
private fun AbilityBlock(ability: Ability) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Text(
            text = ability.name.ifBlank { "Unnamed ability" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        if (ability.mechanism.isNotBlank()) {
            LabeledLine(label = "How it works", value = ability.mechanism)
        }
        if (ability.limits.isNotBlank()) {
            LabeledLine(label = "Limits", value = ability.limits)
        }
    }
}

// One counter rendered as a titled block: name, severity chip, how to use it.
@Composable
private fun WeaknessBlock(weakness: Weakness) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = weakness.name.ifBlank { "Unnamed weakness" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            SeverityChip(weakness.severity)
        }
        if (weakness.exploit.isNotBlank()) {
            LabeledLine(label = "Exploit", value = weakness.exploit)
        }
    }
}

// A small label above a value, reused inside the ability and weakness blocks.
@Composable
private fun LabeledLine(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SeverityChip(severity: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = severity,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TagPill(tag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = tag, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RelatedPill(entity: Entity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = entity.designation, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        Text(text = entity.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
    }
}
