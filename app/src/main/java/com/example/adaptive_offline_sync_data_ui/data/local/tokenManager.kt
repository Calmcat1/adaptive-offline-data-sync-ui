package com.example.adaptive_offline_sync_data_ui.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")
private const val TAG = "TokenManagerDebug"

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    }

    // --- READ FLOWS ---

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        val token = preferences[ACCESS_TOKEN_KEY]
        Log.d(TAG, "Read tokenFlow -> token: ${if (token.isNullOrEmpty()) "NULL/EMPTY" else "EXISTS"}")
        token
    }

    val getUserName: Flow<String> = context.dataStore.data.map { preferences ->
        val name = preferences[USER_NAME_KEY] ?: ""
        Log.d(TAG, "Read getUserName -> value in DataStore: '$name'")
        name
    }

    val getUserEmail: Flow<String> = context.dataStore.data.map { preferences ->
        val email = preferences[USER_EMAIL_KEY] ?: ""
        Log.d(TAG, "Read getUserEmail -> value in DataStore: '$email'")
        email
    }

    // --- WRITE OPERATIONS ---

    suspend fun saveToken(token: String) {
        Log.d(TAG, "saveToken called -> Saving token only (user name and email NOT updated)")
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun saveUserSession(token: String, name: String, email: String) {
        Log.d(TAG, "saveUserSession called -> Name: '$name', Email: '$email'")
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
            preferences[USER_NAME_KEY] = name
            preferences[USER_EMAIL_KEY] = email
        }
    }

    suspend fun clearToken() {
        Log.d(TAG, "clearToken called -> Clearing DataStore preferences")
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}