package com.elendheim.codex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.brand.ElendheimSplash
import com.elendheim.codex.ui.nav.CodexNavHost
import com.elendheim.codex.ui.theme.ElendheimCodexTheme

// The single Activity that hosts the whole Compose app. It builds the shared
// ViewModel, applies the theme with the reader's accessibility choices, shows the
// splash once, then hands off to the navigation graph.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Build the shared ViewModel with the repository from the Application.
            val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as CodexApp
            val vm: CodexViewModel = viewModel(factory = CodexViewModel.Factory(app.repository))

            // Accessibility choices drive the theme, so read them here at the root.
            val textScale by vm.textScale.collectAsState()
            val highContrast by vm.highContrast.collectAsState()

            ElendheimCodexTheme(textScale = textScale, highContrast = highContrast) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Root(vm)
                }
            }
        }
    }
}

@Composable
private fun Root(vm: CodexViewModel) {
    val reduceMotion by vm.reduceMotion.collectAsState()

    // Show the splash first, then the app. The flag lives here so a config change
    // does not replay the splash unless the whole process restarts.
    var showSplash by remember { mutableStateOf(true) }
    if (showSplash) {
        ElendheimSplash(reduceMotion = reduceMotion, onFinished = { showSplash = false })
    } else {
        CodexNavHost(vm = vm)
    }
}
