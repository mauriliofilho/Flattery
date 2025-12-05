package com.agronick.launcher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore for launcher preferences
 * Replaces deprecated SharedPreferences with modern type-safe alternative
 */
class LauncherPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "launcher_settings"
        )

        private val ICON_SIZE_KEY = intPreferencesKey("icon_size")
        private val MARGIN_KEY = intPreferencesKey("margin")
        private val OFFSET_X_KEY = intPreferencesKey("offset_x")
        private val OFFSET_Y_KEY = intPreferencesKey("offset_y")

        const val DEFAULT_ICON_SIZE = 24
        const val DEFAULT_MARGIN = 1
    }

    /**
     * Get icon size as Flow
     */
    val iconSize: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[ICON_SIZE_KEY] ?: DEFAULT_ICON_SIZE
    }

    /**
     * Get margin as Flow
     */
    val margin: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MARGIN_KEY] ?: DEFAULT_MARGIN
    }

    /**
     * Get offset X as Flow
     */
    val offsetX: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[OFFSET_X_KEY] ?: 0
    }

    /**
     * Get offset Y as Flow
     */
    val offsetY: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[OFFSET_Y_KEY] ?: 0
    }

    /**
     * Save icon size
     */
    suspend fun saveIconSize(size: Int) {
        context.dataStore.edit { preferences ->
            preferences[ICON_SIZE_KEY] = size
        }
    }

    /**
     * Save margin
     */
    suspend fun saveMargin(margin: Int) {
        context.dataStore.edit { preferences ->
            preferences[MARGIN_KEY] = margin
        }
    }

    /**
     * Save viewport offset
     */
    suspend fun saveOffset(x: Int, y: Int) {
        context.dataStore.edit { preferences ->
            preferences[OFFSET_X_KEY] = x
            preferences[OFFSET_Y_KEY] = y
        }
    }

    /**
     * Clear all preferences
     */
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

