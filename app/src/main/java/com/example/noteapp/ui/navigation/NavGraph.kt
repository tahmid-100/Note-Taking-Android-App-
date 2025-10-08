package com.example.noteapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.noteapp.data.local.ThemePreferences
import com.example.noteapp.ui.screens.AddEditNoteScreen
import com.example.noteapp.ui.screens.HomeScreen
import com.example.noteapp.ui.viewmodel.NoteViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home") // Route for the home screen
    object AddNote : Screen("add_note") // Route for adding a new note
    object EditNote : Screen("edit_note/{noteId}") { // Route for editing a note with noteId as argument
        fun createRoute(noteId: Int) = "edit_note/$noteId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: NoteViewModel,
    themePreferences: ThemePreferences
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route // Initial screen
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                themePreferences = themePreferences, // ← Pass to HomeScreen
                onAddClick = {
                    navController.navigate(Screen.AddNote.route)
                },
                onNoteClick = { noteId ->
                    navController.navigate(Screen.EditNote.createRoute(noteId))
                }
            )
        }

        composable(route = Screen.AddNote.route) {
            AddEditNoteScreen(
                viewModel = viewModel,
                noteId = null,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.EditNote.route,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId")
            AddEditNoteScreen(
                viewModel = viewModel,
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}