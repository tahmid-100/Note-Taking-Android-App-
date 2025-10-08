// AddEditNoteScreen.kt
// Screen for adding or editing a note. Handles UI and logic for note creation and update.

package com.example.noteapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.noteapp.data.local.Note
import com.example.noteapp.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: NoteViewModel, // ViewModel for note operations
    noteId: Int?,            // If not null, edit mode; else, add mode
    onNavigateBack: () -> Unit // Callback for navigation
) {
    // State for note title
    var title by remember { mutableStateOf("") }
    // State for note description
    var description by remember { mutableStateOf("") }
    // Holds the existing note if editing
    var existingNote by remember { mutableStateOf<Note?>(null) }
    // Controls error display for empty title
    var showEmptyTitleError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope() // Coroutine scope for async operations

    // Load note data if editing
    LaunchedEffect(noteId) {
        if (noteId != null) {
            val note = viewModel.getNoteById(noteId)
            note?.let {
                title = it.title
                description = it.description
                existingNote = it
            }
        }
    }

    // Save note logic: validates title, updates or inserts note
    val saveNote: () -> Unit = {
        if (title.isBlank()) {
            showEmptyTitleError = true
        } else {
            scope.launch {
                if (existingNote != null) {
                    viewModel.updateNote(
                        existingNote!!.copy(
                            title = title.trim(),
                            description = description.trim(),
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    viewModel.insertNote(title, description)
                }
                onNavigateBack()
            }
        }
    }

    // Scaffold provides the basic layout structure
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Show appropriate title based on mode
                    Text(if (existingNote != null) "Edit Note" else "Add Note")
                },
                navigationIcon = {
                    // Back button: saves note if title is not blank, else just navigates back
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            saveNote()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save button: triggers saveNote logic
                    IconButton(onClick = saveNote) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Title input field
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    showEmptyTitleError = false // Reset error on change
                },
                label = { Text("Title") },
                placeholder = { Text("Enter note title") },
                isError = showEmptyTitleError,
                supportingText = {
                    if (showEmptyTitleError) {
                        Text(
                            text = "Title cannot be empty",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp)) // Space between fields

            // Description input field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter note description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                maxLines = Int.MAX_VALUE,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Character count display
            Text(
                text = "${title.length} / ${description.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

    // Show error for empty title for 2 seconds
    if (showEmptyTitleError) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showEmptyTitleError = false
        }
    }
}