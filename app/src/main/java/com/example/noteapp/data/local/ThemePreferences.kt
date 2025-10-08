package com.example.noteapp.data.local
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages theme preference using DataStore
 * Stores whether dark mode is manually enabled
 */
class ThemePreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("theme_prefs")
        private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        private val USE_SYSTEM_THEME_KEY = booleanPreferencesKey("use_system_theme")
    }

    // Read dark mode preference
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_MODE_KEY] ?: false
    }

    // Read whether to use system theme
    val useSystemTheme: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_SYSTEM_THEME_KEY] ?: true // Default: use system theme
    }

    // Save dark mode preference
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
            preferences[USE_SYSTEM_THEME_KEY] = false // Manual override
        }
    }

    // Reset to system theme
    suspend fun useSystemTheme() {
        context.dataStore.edit { preferences ->
            preferences[USE_SYSTEM_THEME_KEY] = true
        }
    }
}
