// HomeScreen.kt
// Main screen displaying the list of notes, search bar, theme selection, and favorite filter.

package com.example.noteapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.noteapp.data.local.Note
import com.example.noteapp.data.local.ThemePreferences
import com.example.noteapp.ui.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NoteViewModel, // ViewModel for note operations
    themePreferences: ThemePreferences, // Theme preferences for dark/light/system mode
    onAddClick: () -> Unit, // Callback for add note button
    onNoteClick: (Int) -> Unit // Callback for note item click
) {
    val notes by viewModel.filteredNotes.collectAsState() // List of notes filtered by search/favorite
    val searchQuery by viewModel.searchQuery.collectAsState() // Current search query
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState() // Show only favorite notes

    // Theme preferences state
    val useSystemTheme by themePreferences.useSystemTheme.collectAsState(initial = true)
    val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = false)
    val scope = rememberCoroutineScope() // Coroutine scope for theme changes

    // Controls visibility of theme selection menu
    var showThemeMenu by remember { mutableStateOf(false) }

    // Scaffold provides the main layout structure
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Notes") }, // App bar title
                actions = {
                    // Theme toggle button
                    IconButton(onClick = { showThemeMenu = true }) {
                        Icon(
                            imageVector = if (isDarkMode && !useSystemTheme)
                                Icons.Default.DarkMode
                            else Icons.Default.LightMode,
                            contentDescription = "Theme"
                        )
                    }

                    // Dropdown menu for theme selection
                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Light Mode") },
                            onClick = {
                                scope.launch {
                                    themePreferences.setDarkMode(false)
                                }
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.LightMode, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark Mode") },
                            onClick = {
                                scope.launch {
                                    themePreferences.setDarkMode(true)
                                }
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DarkMode, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("System Default") },
                            onClick = {
                                scope.launch {
                                    themePreferences.useSystemTheme()
                                }
                                showThemeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, null)
                            }
                        )
                    }

                    // Favorite filter button
                    IconButton(onClick = { viewModel.toggleFavoriteFilter() }) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Filter Favorites", // Toggles favorite filter
                            tint = if (showFavoritesOnly) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick, // Add note button
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { paddingValues ->
        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar for filtering notes
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Show empty state if no notes, else show notes list
            if (notes.isEmpty()) {
                EmptyState(showFavoritesOnly = showFavoritesOnly)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = notes,
                        key = { note -> note.id }
                    ) { note ->
                        NoteItem(
                            note = note,
                            onNoteClick = { onNoteClick(note.id) }, // Navigate to note details
                            onDeleteClick = { viewModel.deleteNote(note) }, // Delete note
                            onFavoriteClick = { viewModel.toggleFavorite(note.id) } // Toggle favorite
                        )
                    }
                }
            }
        }
    }
}

/**
 * SearchBar composable for filtering notes by query.
 * Displays a clear button when query is not empty.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search notes...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * NoteItem composable displays a single note card with title, description, timestamp,
 * favorite toggle, and delete button. Shows a confirmation dialog before deleting.
 */
@Composable
fun NoteItem(
    note: Note,
    onNoteClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNoteClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isFavorite)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (note.isFavorite) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (note.isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (note.description.isNotBlank()) {
                Text(
                    text = note.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatTimestamp(note.timestamp), // Formatted note timestamp
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

    // Confirmation dialog for deleting a note
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * EmptyState composable displays a message and icon when there are no notes to show.
 * Shows different content based on whether the favorite filter is active.
 */
@Composable
fun EmptyState(showFavoritesOnly: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (showFavoritesOnly) Icons.Default.FavoriteBorder
                else Icons.Default.Note,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (showFavoritesOnly) "No favorite notes yet"
                else "No notes yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (showFavoritesOnly) "Mark notes as favorite to see them here"
                else "Tap + to create your first note",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Formats a timestamp (Long) to a readable date string.
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}