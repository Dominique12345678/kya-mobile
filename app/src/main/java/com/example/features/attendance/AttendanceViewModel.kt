package com.example.features.attendance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.PreferencesHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AttendanceViewModel(
    application: Application,
    private val repository: AttendanceRepository,
    private val preferencesHelper: PreferencesHelper
) : AndroidViewModel(application) {

    // Preferences states as reactive Flows
    val employeeToken: StateFlow<String> = preferencesHelper.employeeToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "KYA-EMP-78092")

    val employeeName: StateFlow<String> = preferencesHelper.employeeName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Dominique Kotoyao")

    val fastApiUrl: StateFlow<String> = preferencesHelper.fastApiUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://ais-dev-3ppigx52kqk3kl3tulir7m-630465325001.europe-west2.run.app")

    val isEnPoste: StateFlow<Boolean> = preferencesHelper.isEnPoste
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Database history list state
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sync operations state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    fun saveSettings(token: String, name: String, url: String) {
        viewModelScope.launch {
            preferencesHelper.saveEmployeeSettings(token = token, name = name, url = url)
        }
    }

    fun togglePoste() {
        viewModelScope.launch {
            val currentEnPoste = isEnPoste.value
            val nextEnPoste = !currentEnPoste
            
            // 1. Toggle preferences
            preferencesHelper.setEnPoste(nextEnPoste)
            
            // 2. Log attendance punch
            val punchType = if (nextEnPoste) "IN" else "OUT"
            val token = employeeToken.value
            val name = employeeName.value
            val url = fastApiUrl.value

            repository.insertPunchRecord(
                type = punchType,
                token = token,
                name = name,
                baseUrl = url
            )
        }
    }

    fun syncLogs() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncMessage.value = "Synchronisation en cours..."
            val url = fastApiUrl.value
            val result = repository.syncUnsyncedRecords(url)
            _isSyncing.value = false
            _syncMessage.value = result.message
        }
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Factory provider class to instantiate correctly in Activity
    class Factory(
        private val application: Application,
        private val repository: AttendanceRepository,
        private val preferencesHelper: PreferencesHelper
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AttendanceViewModel(application, repository, preferencesHelper) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
