package com.example.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.UserSettings
import com.example.domain.repository.BioTrackRepository
import java.io.OutputStreamWriter
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(repository: BioTrackRepository) {
    val viewModel: SettingsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            viewModel.getExportJson { jsonString ->
                try {
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        OutputStreamWriter(os).use { writer ->
                            writer.write(jsonString)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { input ->
                    val jsonContent = InputStreamReader(input).readText()
                    viewModel.importFromJson(jsonContent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nastavení") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Uživatel", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }

            item {
                OutlinedTextField(
                    value = state.userSettings.userWeightKg.toString(),
                    onValueChange = { 
                        it.toFloatOrNull()?.let { w -> viewModel.updateSettings(state.userSettings.copy(userWeightKg = w)) }
                    },
                    label = { Text("Váha (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Upozornění zapnuta", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = state.userSettings.warningsEnabled,
                        onCheckedChange = { viewModel.updateSettings(state.userSettings.copy(warningsEnabled = it)) }
                    )
                }
            }

            item { HorizontalDivider() }
            item { Text("Vzhled & Režim", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }

            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Režim Soukromí (Skrýt částky)", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = state.userSettings.privacyMode,
                        onCheckedChange = { viewModel.updateSettings(state.userSettings.copy(privacyMode = it)) }
                    )
                }
            }
            
            item {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Sledování financí", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = state.userSettings.financeMode,
                        onCheckedChange = { viewModel.updateSettings(state.userSettings.copy(financeMode = it)) }
                    )
                }
            }
            
            item { HorizontalDivider() }
            item { Text("Data & Záloha", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }

            item {
                Button(onClick = { exportLauncher.launch("biotrack_backup.json") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Exportovat Databázi")
                }
                if (state.exportStatus.isNotEmpty()) {
                    Text(state.exportStatus, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                FilledTonalButton(onClick = { importLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Importovat Databázi")
                }
                if (state.importStatus.isNotEmpty()) {
                    Text(state.importStatus, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
