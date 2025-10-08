package com.example.noteapp.data.repository

import com.example.noteapp.data.local.Note
import com.example.noteapp.data.local.NoteDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository acts as a single source of truth for note data
 */
class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query)
    }

    fun getFavoriteNotes(): Flow<List<Note>> {
        return noteDao.getFavoriteNotes()
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun toggleFavorite(noteId: Int) {
        noteDao.toggleFavorite(noteId)
    }
}