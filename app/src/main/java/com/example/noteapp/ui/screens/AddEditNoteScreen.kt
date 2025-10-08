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
    viewModel: NoteViewModel,
    noteId: Int?,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var existingNote by remember { mutableStateOf<Note?>(null) }
    var showEmptyTitleError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (existingNote != null) "Edit Note" else "Add Note")
                },
                navigationIcon = {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    showEmptyTitleError = false
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

            Spacer(modifier = Modifier.height(16.dp))

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

            Text(
                text = "${title.length} / ${description.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

    if (showEmptyTitleError) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showEmptyTitleError = false
        }
    }
}