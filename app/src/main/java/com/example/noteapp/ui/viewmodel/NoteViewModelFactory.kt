package com.example.noteapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.noteapp.data.repository.NoteRepository

/**
 * Factory for creating instances of NoteViewModel with the required NoteRepository dependency.
 * This is used to provide the ViewModel in contexts where constructor parameters are needed.
 */
class NoteViewModelFactory(
    private val repository: NoteRepository // Repository instance for data operations
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the requested ViewModel class.
     * Throws IllegalArgumentException if the requested class is not NoteViewModel.
     *
     * @param modelClass The class of the ViewModel to create
     * @return An instance of NoteViewModel
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}