package com.example.noteapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room Database for storing notes.
 * Defines the database configuration and serves as the app's main access point to persisted data.
 */
@Database(
    entities = [Note::class], // List all entities (tables) in the database
    version = 1,              // Database version for migrations
    exportSchema = false      // Disable schema export for simplicity
)
abstract class NoteDatabase : RoomDatabase() {

    /**
     * Provides access to DAO for note operations.
     */
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        /**
         * Returns the singleton instance of the database.
         * Creates the database if it doesn't exist.
         *
         * @param context Application context
         * @return NoteDatabase instance
         */
        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    .fallbackToDestructiveMigration() // Recreates db if migration is missing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
