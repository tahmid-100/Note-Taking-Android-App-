package com.example.noteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.noteapp.data.local.NoteDatabase
import com.example.noteapp.data.local.ThemePreferences
import com.example.noteapp.data.repository.NoteRepository
import com.example.noteapp.ui.navigation.NavGraph
import com.example.noteapp.ui.theme.NoteAppTheme
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.ui.viewmodel.NoteViewModelFactory

/**
 * MainActivity is the entry point of the app.
 * Sets up the database, repository, ViewModel, and theme preferences.
 * Observes theme settings and applies the correct theme.
 * Hosts the navigation graph for the app's screens.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge layout for immersive UI

        // Initialize database, repository, ViewModel factory, and theme preferences
        val database = NoteDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao())
        val viewModelFactory = NoteViewModelFactory(repository)
        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            // Observe theme preferences from DataStore
            val useSystemTheme by themePreferences.useSystemTheme.collectAsState(initial = true)
            val savedDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
            val systemDarkMode = isSystemInDarkTheme() // Detect system dark mode

            // Choose theme: system or user preference
            val darkTheme = if (useSystemTheme) systemDarkMode else savedDarkMode

            // Apply app theme
            NoteAppTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up navigation controller and ViewModel
                    val navController = rememberNavController()
                    val viewModel: NoteViewModel = viewModel(factory = viewModelFactory)

                    // Host the navigation graph, passing theme preferences
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        themePreferences = themePreferences // Pass to NavGraph for theme switching
                    )
                }
            }
        }
    }
}