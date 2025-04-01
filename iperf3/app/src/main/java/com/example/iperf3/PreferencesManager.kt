package com.example.iperf3

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "app_preferences")

class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore
    private val lastCommandKey = stringPreferencesKey("last_command")

    suspend fun saveLastCommand(command: String) {
        dataStore.edit { preferences ->
            preferences[lastCommandKey] = command
        }
    }

    suspend fun getLastCommand(): String? {
        return dataStore.data
            .map { preferences ->
                preferences[lastCommandKey]
            }
            .first()
    }
}