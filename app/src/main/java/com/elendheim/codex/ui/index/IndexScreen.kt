package com.elendheim.codex.ui.index

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.SortOrder
import com.elendheim.codex.ui.components.ClassChip
import com.elendheim.codex.ui.components.ThreatPips
import java.time.LocalDate

// The home screen. The archive as a dense list with search, filters and sort, plus
// a one tap JSON export shortcut and the button that adds a new dossier.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
    vm: CodexViewModel,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onSettings: () -> Unit,
    onOverview: () -> Unit,
    onOpenTagFilter: () -> Unit
) {
    val entities by vm.visibleEntities.collectAsState()
    val classes by vm.classes.collectAsState()
    val tags by vm.allTags.collectAsState()
    val filters by vm.filters.collectAsState()
    val message by vm.message.collectAsState()
    val byId by vm.entitiesById.collectAsState()
    val lastExport by vm.lastExportAt.collectAsState()
    val archiveName by vm.archiveName.collectAsState()
    val reduceMotion by vm.reduceMotion.collectAsState()

    // Drives the randomize spin overlay. When set, the dialog animates then opens.
    var spinning by remember { mutableStateOf(false) }
    val activeEntities = byId.values.filter { it.status == "active" }

    // The backup reminder can be waved away for the current session.
    var nudgeDismissed by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    // The quick export shortcut. It asks the system where to save, then writes JSON.
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) vm.exportJson(context.contentResolver, uri) }

    // Surface any message from the ViewModel as a snackbar, then clear it.
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(archiveName) },
                actions = {
                    // Open a random entry with a little spin. Only if there is one to land on.
                    IconButton(onClick = { if (activeEntities.isNotEmpty()) spinning = true }) {
                        Icon(Icons.Filled.Shuffle, contentDescription = "Open a random entry")
                    }
                    IconButton(onClick = {
                        val today = LocalDate.now()
                        exportLauncher.launch(
                            "elendheim-codex-%04d-%02d-%02d.json".format(
                                today.year, today.monthValue, today.dayOfMonth
                            )
                        )
                    }) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = "Export archive")
                    }
                    // A quick overview of the whole archive: counts and simple bars.
                    IconButton(onClick = onOverview) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Overview")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Search across every field of every dossier.
            OutlinedTextField(
                value = filters.query,
                onValueChange = vm::setQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                placeholder = { Text("Search the archive") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filters.query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions()
            )

            // The filter and sort controls.
            IndexControls(
                vm = vm,
                classes = classes,
                tags = tags,
                filters = filters,
                onOpenTagFilter = onOpenTagFilter
            )

            // A gentle backup reminder. Shows when there is data and it has either
            // never been exported or not been exported in a month.
            val thirtyDays = 30L * 24 * 60 * 60 * 1000
            val stale = lastExport == 0L || (System.currentTimeMillis() - lastExport) > thirtyDays
            if (!nudgeDismissed && byId.isNotEmpty() && stale) {
                ExportNudge(
                    onExport = {
                        val today = LocalDate.now()
                        exportLauncher.launch(
                            "elendheim-codex-%04d-%02d-%02d.json".format(today.year, today.monthValue, today.dayOfMonth)
                        )
                        nudgeDismissed = true
                    },
                    onDismiss = { nudgeDismissed = true }
                )
            }

            if (entities.isEmpty()) {
                EmptyState(hasFilters = filters.query.isNotBlank() ||
                    filters.classFilter != null || filters.threatFilter != null || filters.tagFilter != null)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 12.dp, end = 12.dp, top = 4.dp, bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entities, key = { it.id }) { entity ->
                        EntityRow(entity = entity, classes = classes, onClick = { onOpen(entity.id) })
                    }
                }
            }
        }
    }

    // The randomize spin overlay. It picks a winner, animates, then opens it.
    if (spinning) {
        RandomizerDialog(
            active = activeEntities,
            reduceMotion = reduceMotion,
            onLand = { id -> spinning = false; onOpen(id) },
            onDismiss = { spinning = false }
        )
    }
}

// One dense row in the list: designation, name, class chip, threat pips and summary.
@Composable
private fun EntityRow(entity: Entity, classes: List<com.elendheim.codex.codex.model.EntityClass>, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = entity.designation,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Box(modifier = Modifier.padding(start = 8.dp)) {
                ClassChip(classKey = entity.classification, classes = classes)
            }
            Box(modifier = Modifier.weight(1f))
            ThreatPips(threat = entity.threat)
        }
        Text(
            text = entity.name.ifBlank { "Unnamed" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (entity.summary.isNotBlank()) {
            Text(
                text = entity.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// The backup reminder card. Plain and easy to wave away, never in the way.
@Composable
private fun ExportNudge(onExport: () -> Unit, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Back up your archive. One file, keeps everything safe.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        androidx.compose.material3.TextButton(onClick = onExport) { Text("Export") }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Filled.Clear, contentDescription = "Dismiss")
        }
    }
}

// Shown when the list is empty, with different wording for a fresh archive versus a
// filter that simply matched nothing.
@Composable
private fun EmptyState(hasFilters: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = if (hasFilters) "Nothing matches those filters." else "The archive is empty. Add your first entry.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(32.dp)
        )
    }
}
