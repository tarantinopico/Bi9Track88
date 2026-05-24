package com.example.ui.screens.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.pk.PKEngine
import com.example.domain.repository.BioTrackRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(repository: BioTrackRepository) {
    val viewModel: RecordViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return RecordViewModel(repository) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nový Záznam") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Substance Selection
            Text("Látka", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.substances) { sub ->
                    FilterChip(
                        selected = state.selectedSubstance?.id == sub.id,
                        onClick = { viewModel.selectSubstance(sub.id) },
                        label = { Text(sub.name) }
                    )
                }
            }

            // Variant Selection
            if (state.variants.isNotEmpty()) {
                Text("Varianta", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.variants) { vrnt ->
                        FilterChip(
                            selected = state.selectedVariant?.id == vrnt.id,
                            onClick = { viewModel.selectVariant(vrnt.id) },
                            label = { Text(vrnt.name) }
                        )
                    }
                }
            }

            if (state.selectedSubstance != null) {
                OutlinedTextField(
                    value = state.amountStr,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("Množství (${state.selectedVariant?.unitLabel ?: state.selectedSubstance!!.defaultUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Způsob užití", style = MaterialTheme.typography.titleMedium)
                ExposedDropdownRouteSelector(
                    selected = state.route,
                    onSelected = { viewModel.updateRoute(it) }
                )
            }

            AnimatedVisibility(visible = state.pkEstimate != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PK Odhad", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        
                        state.pkEstimate?.let { pk ->
                            Text("Odhad nástupu (Tmax): ${String.format("%.1f", pk.estimatedTmaxHours)}h")
                            Text("Odhad doby trvání: ${String.format("%.1f", pk.estimatedDurationHours)}h")
                            
                            if (pk.totalWarnings.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("⚠️ Upozornění", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
                                pk.totalWarnings.forEach { w ->
                                    Text(w, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        if (state.calculatedPrice > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cena: ${String.format("%.2f", state.calculatedPrice)}")
                        }
                    }
                }
            }

            Button(
                onClick = { viewModel.saveDose() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedSubstance != null && state.amountStr.toDoubleOrNull() != null
            ) {
                Text("Uložit Dávku")
            }

            if (state.doseSaved) {
                Text("Dávka úspěšně uložena!", color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    viewModel.resetDoseStatus()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExposedDropdownRouteSelector(selected: String, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PKEngine.routes.forEach { r ->
                DropdownMenuItem(
                    text = { Text(r.id) },
                    onClick = {
                        onSelected(r.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
