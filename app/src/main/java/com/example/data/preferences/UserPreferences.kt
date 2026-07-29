package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val IS_DARK_THEME_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("is_dark_theme")
        val IS_ONBOARDED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("is_onboarded")
    }

    val userIdFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    val isDarkThemeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME_KEY] ?: true // Default to true (dark theme)
    }

    val isOnboardedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_ONBOARDED_KEY] ?: false
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME_KEY] = isDark
        }
    }

    suspend fun setOnboarded(isOnboarded: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_ONBOARDED_KEY] = isOnboarded
        }
    }

    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
}
