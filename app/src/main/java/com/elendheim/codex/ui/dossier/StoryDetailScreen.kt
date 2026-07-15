package com.elendheim.codex.ui.dossier

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.components.RichCodexText

// A calm, full screen reading view for a single story. Opened from the Stories list on
// a dossier. The body reads with the same redaction and tap links as everywhere else.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryDetailScreen(
    vm: CodexViewModel,
    entityId: String,
    index: Int,
    onBack: () -> Unit,
    onOpenRelated: (String) -> Unit
) {
    val byId by vm.entitiesById.collectAsState()
    val redact by vm.redactionMode.collectAsState()

    val entity = byId[entityId]
    val story = entity?.story?.getOrNull(index)
    val byDesignation = byId.values.associateBy { it.designation }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(story?.title?.ifBlank { "Story" } ?: "Story") },
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
        if (story == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("This story is not available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            // Which entity this story belongs to, plus its time label.
            entity?.let {
                Text(
                    text = "${it.designation} ${it.name}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            if (story.period.isNotBlank()) {
                Text(
                    text = story.period,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (story.body.isNotBlank()) {
                RichCodexText(
                    text = story.body,
                    byDesignation = byDesignation,
                    redact = redact,
                    onOpen = onOpenRelated,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Text(
                    text = "This story has no text yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Box(modifier = Modifier.padding(bottom = 48.dp))
        }
    }
}
