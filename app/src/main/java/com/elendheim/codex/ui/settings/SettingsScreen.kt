package com.elendheim.codex.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.elendheim.codex.BuildConfig
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.SectionLabel
import java.time.LocalDate

// The settings hub. Export and import live here as first class actions, next to the
// class scheme, the designation prefix, the archive and the about screen.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: CodexViewModel,
    onBack: () -> Unit,
    onManageClasses: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenAccessibility: () -> Unit
) {
    val context = LocalContext.current
    val prefix by vm.designationPrefix.collectAsState()
    val archivedCount by vm.archivedCount.collectAsState()
    val redaction by vm.redactionMode.collectAsState()
    val archiveName by vm.archiveName.collectAsState()

    var prefixInput by remember(prefix) { mutableStateOf(prefix) }
    var nameInput by remember(archiveName) { mutableStateOf(archiveName) }
    var confirmReplaceUri by remember { mutableStateOf<android.net.Uri?>(null) }

    // A snackbar so pressing Save clearly shows the change went through.
    val message by vm.message.collectAsState()
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    // File pickers. Export creates a new file, import opens an existing one.
    val exportJson = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> if (uri != null) vm.exportJson(context.contentResolver, uri) }

    val exportMd = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/markdown")
    ) { uri -> if (uri != null) vm.exportMarkdown(context.contentResolver, uri) }

    val importMerge = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) vm.importJson(context.contentResolver, uri, replace = false) }

    // Replace asks for a confirm before wiping, so we hold the chosen file first.
    val importReplace = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) confirmReplaceUri = uri }

    fun today(): Triple<Int, Int, Int> {
        val d = LocalDate.now()
        return Triple(d.year, d.monthValue, d.dayOfMonth)
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            // Backup section.
            SectionLabel(text = "Backup", modifier = Modifier.padding(top = 16.dp))
            Text(
                text = "Your archive is yours. Export the whole thing to a single file, and import it back on any phone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val (y, m, d) = today()
                        exportJson.launch("elendheim-codex-%04d-%02d-%02d.json".format(y, m, d))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Export to file (JSON)") }

                OutlinedButton(
                    onClick = {
                        val (y, m, d) = today()
                        exportMd.launch("elendheim-codex-%04d-%02d-%02d.md".format(y, m, d))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Export a readable copy (Markdown)") }

                OutlinedButton(
                    onClick = { importMerge.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Import from file (merge)") }

                OutlinedButton(
                    onClick = { importReplace.launch(arrayOf("application/json", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Import and replace everything") }
            }

            // Make it your own. The archive name is what shows at the top of the app,
            // so anyone can turn this into their own sorting system.
            SectionLabel(text = "Archive name", modifier = Modifier.padding(top = 28.dp))
            Text(
                text = "Shown at the top of the app. Rename it to make this your own.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("Name") }
                )
                OutlinedButton(onClick = { vm.setArchiveName(nameInput) }) { Text("Save") }
            }

            // Designation prefix.
            SectionLabel(text = "Designation prefix", modifier = Modifier.padding(top = 28.dp))
            Text(
                text = "New entries are numbered with this prefix, for example ${prefixInput.ifBlank { "ELND" }}-014.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = prefixInput,
                    onValueChange = { prefixInput = it.uppercase() },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    label = { Text("Prefix") }
                )
                OutlinedButton(onClick = { vm.setPrefix(prefixInput) }) { Text("Save") }
            }

            // Reading and writing helpers.
            SectionLabel(text = "Reading", modifier = Modifier.padding(top = 28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Redaction mode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "Hides phrases you wrap in %% %% behind solid blocks. The real words are always kept.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                androidx.compose.material3.Switch(checked = redaction, onCheckedChange = { vm.setRedactionMode(it) })
            }
            Text(
                text = "Tip: write [${prefixInput.ifBlank { "ELND" }}-007] in any field to make a tap link to that entry.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Navigation rows.
            SectionLabel(text = "Archive setup", modifier = Modifier.padding(top = 28.dp))
            SettingsRow(title = "Classification scheme", subtitle = "Name, colour and meaning of your tiers", onClick = onManageClasses)
            SettingsRow(title = "Accessibility", subtitle = "Text size, contrast and motion", onClick = onOpenAccessibility)
            SettingsRow(
                title = "Archive",
                subtitle = if (archivedCount == 0) "No archived entries" else "$archivedCount archived",
                onClick = onOpenArchive
            )

            // About.
            SectionLabel(text = "About", modifier = Modifier.padding(top = 28.dp))
            AboutCard(archiveName = archiveName)

            Row(modifier = Modifier.padding(bottom = 48.dp)) {}
        }
    }

    // Confirm before a full replace, since it wipes the current archive first.
    val replaceUri = confirmReplaceUri
    if (replaceUri != null) {
        AlertDialog(
            onDismissRequest = { confirmReplaceUri = null },
            title = { Text("Replace everything") },
            text = { Text("This wipes the current archive and loads the chosen file in its place. Export first if you are not sure.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.importJson(context.contentResolver, replaceUri, replace = true)
                    confirmReplaceUri = null
                }) { Text("Replace") }
            },
            dismissButton = { TextButton(onClick = { confirmReplaceUri = null }) { Text("Cancel") } }
        )
    }
}

// A tappable row that leads to another settings screen.
@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// The short, plain about card. What the app is, in one breath.
@Composable
private fun AboutCard(archiveName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // The reader's own name for their archive, big, with the app version under it.
        Text(
            text = archiveName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Elendheim Codex v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
        Text(
            text = "A quiet place for the things you make up. Write down what each one is, what it can do, and how it goes down, then find it again in seconds when you need it.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "It never leaves this phone unless you send it somewhere, and when you do, the whole archive travels as one file that stays yours. Built on the Elendheim suite, open source under the MIT license.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
