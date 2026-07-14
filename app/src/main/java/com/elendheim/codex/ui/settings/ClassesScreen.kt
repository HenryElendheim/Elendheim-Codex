package com.elendheim.codex.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.parseHex
import java.util.UUID

// Manage the classification scheme. It is entirely user defined, so the tiers, their
// colours and their meanings are all editable here. Edits are saved when you leave
// this screen, so typing a label or colour is never interrupted.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(vm: CodexViewModel, onBack: () -> Unit) {
    val stored by vm.classes.collectAsState()

    // Edit a local copy so typing is smooth. We seed it once when the classes first
    // arrive, then never overwrite it, so a save can never echo back and steal focus
    // mid word. The copy is written to storage only when leaving the screen.
    var working by remember { mutableStateOf<List<EntityClass>?>(null) }
    LaunchedEffect(stored) {
        if (working == null && stored.isNotEmpty()) working = stored
    }

    // Save on the way out, so edits are kept but never applied while still typing.
    val latest by rememberUpdatedState(working)
    DisposableEffect(Unit) {
        onDispose { latest?.let { vm.saveClasses(it) } }
    }

    // Until the local copy is seeded, show what is stored. All edits go to the copy.
    val list = working ?: stored
    fun update(newList: List<EntityClass>) { working = newList }

    // A snackbar so the Save button clearly confirms the change went through.
    val message by vm.message.collectAsState()
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Classification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Explicit save with a confirmation. Edits also save on the way out,
                    // this button just makes it obvious the change was kept.
                    IconButton(onClick = { vm.saveClasses(list, confirm = true) }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                    IconButton(onClick = {
                        // A new tier gets a unique key so entities can reference it safely.
                        val fresh = EntityClass(
                            key = "cls-" + UUID.randomUUID().toString().take(8),
                            label = "New tier",
                            colorHex = "#9A9AA2",
                            meaning = ""
                        )
                        update(list + fresh)
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add tier")
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Threat is a separate axis from class. Class is how you sort them, threat one to five is how dangerous they are.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )

            list.forEachIndexed { index, cls ->
                ClassCard(
                    cls = cls,
                    onChange = { updated -> update(list.toMutableList().also { it[index] = updated }) },
                    onRemove = { update(list.toMutableList().also { it.removeAt(index) }) }
                )
            }

            Box(modifier = Modifier.padding(bottom = 48.dp))
        }
    }
}

// One editable tier: colour swatch, label, hex and meaning.
@Composable
private fun ClassCard(cls: EntityClass, onChange: (EntityClass) -> Unit, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // A live swatch of the current colour.
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(parseHex(cls.colorHex))
            )
            OutlinedTextField(
                value = cls.label,
                onValueChange = { onChange(cls.copy(label = it)) },
                label = { Text("Label") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove tier")
            }
        }
        OutlinedTextField(
            value = cls.colorHex,
            onValueChange = { onChange(cls.copy(colorHex = it)) },
            label = { Text("Colour, for example #C4383A") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = cls.meaning,
            onValueChange = { onChange(cls.copy(meaning = it)) },
            label = { Text("Meaning") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
    }
}
