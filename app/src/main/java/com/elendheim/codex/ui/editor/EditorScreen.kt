package com.elendheim.codex.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.effectiveImages
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.SectionLabel

// Create or edit a dossier. Everything is editable here, including the structured
// ability and weakness rows. Drafts autosave, so an idea is never lost mid thought.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    vm: CodexViewModel,
    entityId: String,
    onDone: () -> Unit
) {
    val classes by vm.classes.collectAsState()
    val allById by vm.entitiesById.collectAsState()

    // The draft under edit. Null until we have loaded or built it.
    var draft by remember { mutableStateOf<Entity?>(null) }

    // Load an existing dossier, or build a fresh one with the next designation.
    LaunchedEffect(entityId) {
        draft = if (entityId == "new") vm.newDraft() else vm.loadEntity(entityId) ?: vm.newDraft()
    }

    // Autosave. Whenever the draft changes and has some content, wait a moment then
    // save. The short wait avoids writing on every single keystroke.
    LaunchedEffect(draft) {
        val current = draft ?: return@LaunchedEffect
        if (hasContent(current)) {
            kotlinx.coroutines.delay(700)
            vm.save(current)
        }
    }

    val current = draft
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entityId == "new") "New entry" else "Edit entry") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Save on the way out so nothing is dropped, then leave.
                        current?.let { if (hasContent(it)) vm.save(it) }
                        onDone()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        current?.let { if (hasContent(it)) vm.save(it) }
                        onDone()
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        if (current == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Identity fields.
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                EditorField(
                    label = "Designation",
                    value = current.designation,
                    onValue = { draft = current.copy(designation = it) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            EditorField(
                label = "Name",
                value = current.name,
                onValue = { draft = current.copy(name = it) },
                singleLine = true
            )

            // Class and threat pickers.
            SectionLabel(text = "Class", modifier = Modifier.padding(top = 12.dp))
            ClassPicker(classes = classes, selected = current.classification) {
                draft = current.copy(classification = it)
            }

            SectionLabel(text = "Threat", modifier = Modifier.padding(top = 16.dp))
            ThreatPicker(selected = current.threat) { draft = current.copy(threat = it) }

            EditorField(
                label = "Summary",
                value = current.summary,
                onValue = { draft = current.copy(summary = it) },
                singleLine = false,
                modifier = Modifier.padding(top = 8.dp)
            )
            EditorField(
                label = "Description",
                value = current.description,
                onValue = { draft = current.copy(description = it) },
                minLines = 3
            )

            // Images: a cover plus any extra visual info added lower down.
            SectionLabel(text = "Images", modifier = Modifier.padding(top = 16.dp))
            GalleryEditor(
                images = current.effectiveImages(),
                // Keep the legacy single image in step with the first gallery image so
                // older app versions and old export readers still see a picture.
                onChange = { list -> draft = current.copy(images = list, image = list.firstOrNull() ?: "") }
            )

            // Structured ability rows.
            AbilityEditor(
                abilities = current.abilities,
                onChange = { draft = current.copy(abilities = it) }
            )

            // Structured weakness rows.
            WeaknessEditor(
                weaknesses = current.weaknesses,
                onChange = { draft = current.copy(weaknesses = it) }
            )

            EditorField(
                label = "Containment",
                value = current.containment,
                onValue = { draft = current.copy(containment = it) },
                minLines = 2,
                modifier = Modifier.padding(top = 8.dp)
            )
            EditorField(
                label = "Notes",
                value = current.notes,
                onValue = { draft = current.copy(notes = it) },
                minLines = 2
            )

            // Tags.
            SectionLabel(text = "Tags", modifier = Modifier.padding(top = 16.dp))
            TagEditor(tags = current.tags, onChange = { draft = current.copy(tags = it) })

            // Related entries.
            SectionLabel(text = "Related", modifier = Modifier.padding(top = 16.dp))
            RelatedEditor(
                selfId = current.id,
                selected = current.related,
                all = allById.values.toList(),
                onChange = { draft = current.copy(related = it) }
            )

            Box(modifier = Modifier.padding(bottom = 48.dp))
        }
    }
}

// A dossier is worth saving once it has any real content. Prevents blank entries
// from being created if the editor is opened and left untouched.
private fun hasContent(e: Entity): Boolean =
    e.name.isNotBlank() || e.summary.isNotBlank() || e.description.isNotBlank() ||
        e.abilities.isNotEmpty() || e.weaknesses.isNotEmpty() ||
        e.containment.isNotBlank() || e.notes.isNotBlank() || e.tags.isNotEmpty() ||
        e.effectiveImages().isNotEmpty()
