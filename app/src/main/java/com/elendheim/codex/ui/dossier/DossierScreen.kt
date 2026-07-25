package com.elendheim.codex.ui.dossier

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elendheim.codex.codex.io.ImageCodec
import com.elendheim.codex.codex.model.Ability
import com.elendheim.codex.codex.model.Entity
import com.elendheim.codex.codex.model.EntityClass
import com.elendheim.codex.codex.model.GalleryImage
import com.elendheim.codex.codex.model.StoryEntry
import com.elendheim.codex.codex.model.Weakness
import com.elendheim.codex.codex.model.effectiveGallery
import com.elendheim.codex.codex.model.matchesQuery
import com.elendheim.codex.codex.model.orderedByDate
import com.elendheim.codex.codex.model.requiredClearance
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.ClassChip
import com.elendheim.codex.ui.components.RichCodexText
import com.elendheim.codex.ui.components.SectionLabel
import com.elendheim.codex.ui.components.ThreatPips
import kotlinx.coroutines.launch

// The reading view. Renders one dossier like a classified file: header, description,
// powers, how to beat it, containment, notes, tags and links to related entries.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DossierScreen(
    vm: CodexViewModel,
    entityId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenRelated: (String) -> Unit,
    onOpenStory: (String, Int) -> Unit
) {
    // Read the dossier straight from the live map so any edit shows at once.
    val byId by vm.entitiesById.collectAsState()
    val classes by vm.classes.collectAsState()
    val redact by vm.redactionMode.collectAsState()
    val clearance by vm.clearance.collectAsState()
    val entity = byId[entityId]

    // Clearance gate. A file needs a clearance to be revealed, and the more it hides the
    // higher that is. When your clearance is short, the redactions stay sealed and the
    // reveal toggle is locked.
    val required = entity?.requiredClearance() ?: 1
    val locked = clearance < required
    // What the body actually shows: redacted if redaction mode is on, or if locked.
    val showRedacted = redact || locked

    // Lookup from designation to entity so [ELD-007] style links can resolve.
    val byDesignation = byId.values.associateBy { it.designation }

    // Needed for the share sheet and for opening the duplicate after it is made.
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // A snackbar for the clearance note and other short messages.
    val message by vm.message.collectAsState()
    val snackbar = remember { androidx.compose.material3.SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }

    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbar) },
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
                        // Redaction toggle. When the file is locked by clearance, this
                        // shows a lock and cannot reveal, only explains what is needed.
                        IconButton(onClick = {
                            if (locked) {
                                vm.message.value = "Clearance $required needed to reveal this file"
                            } else {
                                vm.setRedactionMode(!redact)
                            }
                        }) {
                            Icon(
                                imageVector = when {
                                    locked -> Icons.Filled.Lock
                                    redact -> Icons.Filled.VisibilityOff
                                    else -> Icons.Filled.Visibility
                                },
                                contentDescription = when {
                                    locked -> "Locked, clearance $required needed"
                                    redact -> "Show redacted text"
                                    else -> "Redact marked text"
                                }
                            )
                        }
                        // Editing is blocked while the file is locked, since you cannot
                        // even see what you would be changing.
                        IconButton(onClick = {
                            if (locked) vm.message.value = "Clearance $required needed to edit this file"
                            else onEdit()
                        }) {
                            Icon(
                                imageVector = if (locked) Icons.Filled.EditOff else Icons.Filled.Edit,
                                contentDescription = if (locked) "Editing locked" else "Edit"
                            )
                        }
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            // Share this one dossier as readable text through the system
                            // share sheet.
                            DropdownMenuItem(
                                text = { Text("Share as text") },
                                onClick = {
                                    menuOpen = false
                                    if (locked) {
                                        // Sharing would spell out the hidden text, so it
                                        // needs the same clearance as reading does.
                                        vm.message.value = "Clearance $required needed to share this file"
                                    } else {
                                        val send = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TITLE, "${entity.designation} ${entity.name}".trim())
                                            putExtra(Intent.EXTRA_TEXT, vm.shareTextFor(entity))
                                        }
                                        context.startActivity(Intent.createChooser(send, "Share entry"))
                                    }
                                }
                            )
                            // Make a copy as a new entry, then open the copy.
                            DropdownMenuItem(
                                text = { Text("Duplicate") },
                                onClick = {
                                    menuOpen = false
                                    scope.launch {
                                        val newId = vm.duplicate(entity.id)
                                        if (newId != null) onOpenRelated(newId)
                                    }
                                }
                            )
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
            // A clearance line, shown only when the file actually hides something.
            if (required > 1) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (locked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                        contentDescription = null,
                        tint = if (locked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (locked) "Clearance $required needed" else "Clearance $required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (locked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (entity.summary.isNotBlank()) {
                RichCodexText(
                    text = entity.summary,
                    byDesignation = byDesignation,
                    redact = showRedacted,
                    onOpen = onOpenRelated,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            // The images: the cover shows first, then swipe sideways through the rest.
            // A small counter shows which image you are on. Redacted images stay hidden
            // while redaction mode is on or the file is locked by clearance.
            GalleryPager(gallery = entity.effectiveGallery(), redact = showRedacted, entityName = entity.name)

            TextSection("Description", entity.description, byDesignation, showRedacted, onOpenRelated)

            if (entity.abilities.isNotEmpty()) {
                SectionLabel(text = "Abilities", modifier = Modifier.padding(top = 20.dp))
                entity.abilities.forEach { AbilityBlock(it, byDesignation, showRedacted, onOpenRelated) }
            }

            if (entity.weaknesses.isNotEmpty()) {
                SectionLabel(text = "How to beat it", modifier = Modifier.padding(top = 20.dp))
                entity.weaknesses.forEach { WeaknessBlock(it, byDesignation, showRedacted, onOpenRelated) }
            }

            TextSection("Containment", entity.containment, byDesignation, showRedacted, onOpenRelated)
            TextSection("Notes", entity.notes, byDesignation, showRedacted, onOpenRelated)

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

            // Stories sit on their own at the very bottom, oldest date first. Each is a
            // tappable row that opens a full reading view. A search box filters them by
            // date or title once there is more than one.
            if (entity.story.isNotEmpty()) {
                SectionLabel(text = "Stories", modifier = Modifier.padding(top = 28.dp))
                var storyQuery by remember(entity.id) { mutableStateOf("") }
                if (entity.story.size > 1) {
                    OutlinedTextField(
                        value = storyQuery,
                        onValueChange = { storyQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search stories by date or title") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true
                    )
                }
                val orderedStories = entity.story.orderedByDate().filter { (_, s) -> s.matchesQuery(storyQuery) }
                if (orderedStories.isEmpty()) {
                    Text(
                        text = "No stories match.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    orderedStories.forEach { (originalIndex, s) ->
                        StoryRow(entry = s, onClick = { onOpenStory(entity.id, originalIndex) })
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

// A labeled block of freeform text, skipped entirely when the field is empty.
@Composable
private fun TextSection(
    label: String,
    body: String,
    byDesignation: Map<String, Entity>,
    redact: Boolean,
    onOpen: (String) -> Unit
) {
    if (body.isBlank()) return
    Column(modifier = Modifier.padding(top = 20.dp)) {
        SectionLabel(text = label)
        RichCodexText(
            text = body,
            byDesignation = byDesignation,
            redact = redact,
            onOpen = onOpen,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// One power rendered as a titled block: name, how it works, limits.
@Composable
private fun AbilityBlock(ability: Ability, byDesignation: Map<String, Entity>, redact: Boolean, onOpen: (String) -> Unit) {
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
            LabeledLine("How it works", ability.mechanism, byDesignation, redact, onOpen)
        }
        if (ability.limits.isNotBlank()) {
            LabeledLine("Limits", ability.limits, byDesignation, redact, onOpen)
        }
    }
}

// One story shown as a tappable row: title, optional time label, and a chevron. The
// full text lives on its own reading screen, opened by tapping this.
@Composable
private fun StoryRow(entry: StoryEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title.ifBlank { "Untitled event" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            if (entry.period.isNotBlank()) {
                Text(
                    text = entry.period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// The swipeable image viewer. The first image is the intro, and any others are a
// swipe away. A small counter shows which one you are on. A redacted image, while
// redaction mode is on, shows a censored panel instead of the picture.
@Composable
private fun GalleryPager(gallery: List<GalleryImage>, redact: Boolean, entityName: String) {
    if (gallery.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { gallery.size })

    Column(modifier = Modifier.padding(top = 16.dp)) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            // A fixed height keeps the frame steady while swiping between images.
            modifier = Modifier.fillMaxWidth().height(300.dp)
        ) { page ->
            val pic = gallery[page]
            if (redact && pic.redacted) {
                RedactedImageBox()
            } else {
                val bitmap = remember(pic.data) { ImageCodec.toBitmap(pic.data) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Image ${page + 1} for $entityName",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    RedactedImageBox()
                }
            }
        }

        // Counter and dots, only worth showing once there is more than one image.
        if (gallery.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${gallery.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (i in gallery.indices) {
                        val on = i == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (on) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                        )
                    }
                }
            }
        }
    }
}

// The censored panel shown in place of a redacted image.
@Composable
private fun RedactedImageBox() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.VisibilityOff,
                contentDescription = "Redacted image",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "REDACTED",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

// One counter rendered as a titled block: name, severity chip, how to use it.
@Composable
private fun WeaknessBlock(weakness: Weakness, byDesignation: Map<String, Entity>, redact: Boolean, onOpen: (String) -> Unit) {
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
            LabeledLine("Exploit", weakness.exploit, byDesignation, redact, onOpen)
        }
    }
}

// A small label above a value, reused inside the ability and weakness blocks. The
// value is rich text so links and redaction work there too.
@Composable
private fun LabeledLine(
    label: String,
    value: String,
    byDesignation: Map<String, Entity>,
    redact: Boolean,
    onOpen: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace
        )
        RichCodexText(
            text = value,
            byDesignation = byDesignation,
            redact = redact,
            onOpen = onOpen,
            style = MaterialTheme.typography.bodyLarge
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
