package com.elendheim.codex.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.elendheim.codex.ui.CodexViewModel
import com.elendheim.codex.ui.archive.ArchiveScreen
import com.elendheim.codex.ui.dossier.DossierScreen
import com.elendheim.codex.ui.editor.EditorScreen
import com.elendheim.codex.ui.index.IndexScreen
import com.elendheim.codex.ui.overview.OverviewScreen
import com.elendheim.codex.ui.settings.ClassesScreen
import com.elendheim.codex.ui.settings.SettingsScreen

// Route names kept in one place so typos cannot drift between screens.
object Routes {
    const val INDEX = "index"
    const val DOSSIER = "dossier/{id}"
    const val EDITOR = "editor/{id}"
    const val SETTINGS = "settings"
    const val CLASSES = "classes"
    const val ARCHIVE = "archive"
    const val OVERVIEW = "overview"

    fun dossier(id: String) = "dossier/$id"
    fun editor(id: String) = "editor/$id"   // pass "new" to create a fresh dossier
}

// Wires every screen together. One shared ViewModel is passed down so all screens
// read and write the same live archive.
@Composable
fun CodexNavHost(vm: CodexViewModel) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.INDEX) {

        composable(Routes.INDEX) {
            IndexScreen(
                vm = vm,
                onOpen = { id -> nav.navigate(Routes.dossier(id)) },
                onCreate = { nav.navigate(Routes.editor("new")) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onOverview = { nav.navigate(Routes.OVERVIEW) }
            )
        }

        composable(
            route = Routes.DOSSIER,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            DossierScreen(
                vm = vm,
                entityId = id,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Routes.editor(id)) },
                onOpenRelated = { relatedId -> nav.navigate(Routes.dossier(relatedId)) }
            )
        }

        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            EditorScreen(
                vm = vm,
                entityId = id,
                onDone = { nav.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onManageClasses = { nav.navigate(Routes.CLASSES) },
                onOpenArchive = { nav.navigate(Routes.ARCHIVE) }
            )
        }

        composable(Routes.CLASSES) {
            ClassesScreen(vm = vm, onBack = { nav.popBackStack() })
        }

        composable(Routes.ARCHIVE) {
            ArchiveScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onOpen = { id -> nav.navigate(Routes.dossier(id)) }
            )
        }

        composable(Routes.OVERVIEW) {
            OverviewScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}
