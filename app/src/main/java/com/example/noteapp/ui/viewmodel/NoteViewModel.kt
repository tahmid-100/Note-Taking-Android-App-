package com.example.noteapp.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.local.Note
import com.example.noteapp.data.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing note-related UI state and business logic
 */
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    val filteredNotes: StateFlow<List<Note>> = combine(
        _searchQuery,
        _showFavoritesOnly
    ) { query, favoritesOnly ->
        Pair(query, favoritesOnly)
    }.flatMapLatest { (query, favoritesOnly) ->
        when {
            favoritesOnly && query.isNotBlank() -> {
                repository.getFavoriteNotes().map { notes ->
                    notes.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.description.contains(query, ignoreCase = true)
                    }
                }
            }
            favoritesOnly -> repository.getFavoriteNotes()
            query.isNotBlank() -> repository.searchNotes(query)
            else -> repository.allNotes
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavoriteFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

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

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleFavorite(noteId: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(noteId)
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return repository.getNoteById(id)
    }
}