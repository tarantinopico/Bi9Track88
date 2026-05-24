package com.example.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.UserSettings
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val userSettings: UserSettings = UserSettings(),
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportStatus: String = "",
    val importStatus: String = ""
)

class SettingsViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                _state.update { it.copy(userSettings = settings) }
            }
        }
    }

    fun updateSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    fun getExportJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportStatus = "Generuji export...") }
            try {
                val json = repository.exportDatabaseToJson()
                onResult(json)
                _state.update { it.copy(isExporting = false, exportStatus = "Export hotov!") }
            } catch (e: Exception) {
                _state.update { it.copy(isExporting = false, exportStatus = "Chyba exportu: ${e.message}") }
            }
        }
    }

    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, importStatus = "Importuji data...") }
            try {
                repository.importDatabaseFromJson(jsonString)
                _state.update { it.copy(isImporting = false, importStatus = "Import úspěšný!") }
            } catch (e: Exception) {
                _state.update { it.copy(isImporting = false, importStatus = "Chyba importu: ${e.message}") }
            }
        }
    }
}
