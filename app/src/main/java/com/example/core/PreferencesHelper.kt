package com.example.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kya_presence_prefs")

class PreferencesHelper(private val context: Context) {

    companion object {
        private val EMPLOYEE_TOKEN_KEY = stringPreferencesKey("employee_token")
        private val EMPLOYEE_NAME_KEY = stringPreferencesKey("employee_name")
        private val FAST_API_URL_KEY = stringPreferencesKey("fast_api_url")
        private val IS_EN_POSTE_KEY = booleanPreferencesKey("is_en_poste")
    }

    // Default values matching production edge defaults
    val employeeToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EMPLOYEE_TOKEN_KEY] ?: "KYA-EMP-78092"
    }

    val employeeName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[EMPLOYEE_NAME_KEY] ?: "Dominique Kotoyao"
    }

    val fastApiUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FAST_API_URL_KEY] ?: "https://ais-dev-3ppigx52kqk3kl3tulir7m-630465325001.europe-west2.run.app" // Fallback to our dev URL
    }

    val isEnPoste: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_EN_POSTE_KEY] ?: false
    }

    suspend fun saveEmployeeSettings(token: String, name: String, url: String) {
        context.dataStore.edit { prefs ->
            prefs[EMPLOYEE_TOKEN_KEY] = token
            prefs[EMPLOYEE_NAME_KEY] = name
            prefs[FAST_API_URL_KEY] = url
        }
    }

    suspend fun setEnPoste(isEnPoste: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[IS_EN_POSTE_KEY] = isEnPoste
        }
    }
}
