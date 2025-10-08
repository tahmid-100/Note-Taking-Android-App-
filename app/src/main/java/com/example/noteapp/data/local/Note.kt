package com.example.noteapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Note entity representing a note in the database
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)