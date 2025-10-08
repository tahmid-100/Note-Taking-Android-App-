package com.example.noteapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.Note
import com.example.noteapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing note-related UI state and business logic.
 * Handles search, favorite filtering, CRUD operations, and exposes state to UI.
 */
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // Holds the current search query entered by the user
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Controls whether only favorite notes are shown
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    /**
     * Emits a filtered list of notes based on search query and favorite filter.
     * Combines search and favorite state, then queries repository accordingly.
     */
    val filteredNotes: StateFlow<List<Note>> = combine(
        _searchQuery,
        _showFavoritesOnly
    ) { query, favoritesOnly ->
        Pair(query, favoritesOnly)
    }.flatMapLatest { (query, favoritesOnly) ->
        when {
            // If filtering favorites and searching
            favoritesOnly && query.isNotBlank() -> {
                repository.getFavoriteNotes().map { notes ->
                    notes.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }
                }
            }
            // If only filtering favorites
            favoritesOnly -> repository.getFavoriteNotes()
            // If only searching
            query.isNotBlank() -> repository.searchNotes(query)
            // Show all notes
            else -> repository.allNotes
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Updates the search query state.
     * @param query New search string
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggles the favorite filter state.
     */
    fun toggleFavoriteFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    /**
     * Inserts a new note with the given title and description.
     * Ignores blank titles.
     */
    fun insertNote(title: String, description: String) {
        if (title.isBlank()) return

        viewModelScope.launch {
            val note = Note(
                title = title.trim(),
                description = description.trim()
            )
            repository.insertNote(note)
        }
    }

    /**
     * Updates an existing note.
     * @param note The note to update
     */
    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    /**
     * Deletes a note.
     * @param note The note to delete
     */
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    /**
     * Toggles the favorite status of a note by its ID.
     * @param noteId The ID of the note
     */
    fun toggleFavorite(noteId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(noteId)
        }
    }

    /**
     * Retrieves a note by its ID.
     * @param id The ID of the note
     * @return The note if found, else null
     */
    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }
}