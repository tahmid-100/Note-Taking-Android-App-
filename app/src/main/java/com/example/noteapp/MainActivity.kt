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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = NoteDatabase.getDatabase(applicationContext)
        val repository = NoteRepository(database.noteDao())
        val viewModelFactory = NoteViewModelFactory(repository)
        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            // Observe theme preferences
            val useSystemTheme by themePreferences.useSystemTheme.collectAsState(initial = true)
            val savedDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
            val systemDarkMode = isSystemInDarkTheme()

            // Determine which theme to use
            val darkTheme = if (useSystemTheme) systemDarkMode else savedDarkMode

            NoteAppTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val viewModel: NoteViewModel = viewModel(factory = viewModelFactory)

                    NavGraph(
                        navController = navController,
                        viewModel = viewModel,
                        themePreferences = themePreferences // ← Pass to NavGraph
                    )
                }
            }
        }
    }
}