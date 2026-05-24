package com.example.ui.screens.laboratory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Compound
import com.example.domain.model.Substance
import com.example.domain.model.Variant
import com.example.domain.repository.BioTrackRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaboratoryScreen(repository: BioTrackRepository) {
    val viewModel: LaboratoryViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LaboratoryViewModel(repository) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var showSubstanceDialog by remember { mutableStateOf(false) }
    var substanceToEdit by remember { mutableStateOf<Substance?>(null) }
    
    var showCompoundDialog by remember { mutableStateOf(false) }
    var compoundToEdit by remember { mutableStateOf<Compound?>(null) }
    
    var showVariantDialog by remember { mutableStateOf(false) }
    var variantToEdit by remember { mutableStateOf<Variant?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.selectedSubstance == null) "Laboratoř" else state.selectedSubstance!!.name) },
                navigationIcon = {
                    if (state.selectedSubstance != null) {
                        IconButton(onClick = { viewModel.selectSubstance(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                        }
                    }
                },
                actions = {
                    if (state.selectedSubstance != null) {
                        IconButton(onClick = {
                            substanceToEdit = state.selectedSubstance
                            showSubstanceDialog = true
                        }) { Icon(Icons.Default.Edit, contentDescription = "Upravit") }
                        IconButton(onClick = { viewModel.archiveSubstance(state.selectedSubstance!!) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Archivovat")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.selectedSubstance == null) {
                FloatingActionButton(onClick = { 
                    substanceToEdit = null
                    showSubstanceDialog = true 
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Přidat Látku")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.selectedSubstance == null) {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.substances) { sub ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.selectSubstance(sub) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(sub.name, style = MaterialTheme.typography.titleMedium)
                                Text(sub.category, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item {
                        Text("Účinné Látky (Compounds)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(state.compounds) { cmp ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(cmp.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("Poločas rozpadu: ${cmp.halfLifeHours}h", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { compoundToEdit = cmp; showCompoundDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Upravit")
                                    }
                                    IconButton(onClick = { viewModel.deleteCompound(cmp.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Smazat")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Button(onClick = { compoundToEdit = null; showCompoundDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Přidat Compound")
                        }
                    }
                    
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        Text("Varianty / Receptury", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    items(state.variants) { vrnt ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(vrnt.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("Cena: ${vrnt.pricePerUnit} / ${vrnt.unitLabel}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row {
                                    IconButton(onClick = { variantToEdit = vrnt; showVariantDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Upravit")
                                    }
                                    IconButton(onClick = { viewModel.deleteVariant(vrnt.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Smazat")
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Button(onClick = { variantToEdit = null; showVariantDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Přidat Variantu")
                        }
                    }
                }
            }
        }
    }

    if (showSubstanceDialog) {
        SubstanceEditorDialog(
            initial = substanceToEdit,
            onDismiss = { showSubstanceDialog = false },
            onSave = { 
                viewModel.saveSubstance(it)
                showSubstanceDialog = false 
            }
        )
    }

    if (showCompoundDialog) {
        CompoundEditorDialog(
            initial = compoundToEdit,
            onDismiss = { showCompoundDialog = false },
            onSave = { 
                viewModel.saveCompound(it)
                showCompoundDialog = false 
            }
        )
    }

    if (showVariantDialog) {
        VariantEditorDialog(
            initial = variantToEdit,
            compounds = state.compounds,
            onDismiss = { showVariantDialog = false },
            onSave = { 
                viewModel.saveVariant(it)
                showVariantDialog = false 
            }
        )
    }
}

@Composable
fun SubstanceEditorDialog(initial: Substance?, onDismiss: () -> Unit, onSave: (Substance) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var alias by remember { mutableStateOf(initial?.alias ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var defaultUnit by remember { mutableStateOf(initial?.defaultUnit ?: "mg") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Přidat Látku" else "Upravit Látku") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Název") }, singleLine = true)
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("Alias") }, singleLine = true)
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Kategorie") }, singleLine = true)
                OutlinedTextField(value = defaultUnit, onValueChange = { defaultUnit = it }, label = { Text("Výchozí Jednotka") }, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val sub = initial?.copy(
                    name = name, alias = alias, category = category, defaultUnit = defaultUnit, updatedAt = System.currentTimeMillis()
                ) ?: Substance(
                    id = "", name = name, alias = alias, category = category, iconKey = "", defaultUnit = defaultUnit,
                    active = true, notes = "", createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis(), archivedAt = null
                )
                onSave(sub)
            }) { Text("Uložit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

@Composable
fun CompoundEditorDialog(initial: Compound?, onDismiss: () -> Unit, onSave: (Compound) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var halfLifeStr by remember { mutableStateOf(initial?.halfLifeHours?.toString() ?: "") }
    var thresholdStr by remember { mutableStateOf(initial?.thresholdDose?.toString() ?: "") }
    var onsetStr by remember { mutableStateOf(initial?.onsetMin?.toString() ?: "") }
    var durationStr by remember { mutableStateOf(initial?.durationHours?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Přidat Compound" else "Upravit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Název") })
                OutlinedTextField(value = halfLifeStr, onValueChange = { halfLifeStr = it }, label = { Text("Poločas rozpadu (h)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = onsetStr, onValueChange = { onsetStr = it }, label = { Text("Nástup účinku (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = durationStr, onValueChange = { durationStr = it }, label = { Text("Doba účinku (h)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = thresholdStr, onValueChange = { thresholdStr = it }, label = { Text("Hranice vnímání (mg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hl = halfLifeStr.toDoubleOrNull() ?: 1.0
                    val onset = onsetStr.toIntOrNull() ?: 30
                    val dur = durationStr.toDoubleOrNull() ?: 4.0
                    val th = thresholdStr.toDoubleOrNull() ?: 1.0
                    val c = initial?.copy(
                        name = name, halfLifeHours = hl, onsetMin = onset, durationHours = dur, thresholdDose = th, updatedAt = System.currentTimeMillis()
                    ) ?: Compound(
                        id = "", substanceId = "", name = name, halfLifeHours = hl, onsetMin = onset, peakMin = onset * 2,
                        durationHours = dur, thresholdDose = th, commonDose = th * 5, strongDose = th * 10, molecularWeight = 0.0,
                        active = true, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
                    )
                    onSave(c)
                },
                enabled = name.isNotBlank() && halfLifeStr.toDoubleOrNull() != null
            ) { Text("Uložit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}

@Composable
fun VariantEditorDialog(initial: Variant?, compounds: List<Compound>, onDismiss: () -> Unit, onSave: (Variant) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var priceStr by remember { mutableStateOf(initial?.pricePerUnit?.toString() ?: "") }
    var unitStr by remember { mutableStateOf(initial?.unitLabel ?: "") }
    var roaStr by remember { mutableStateOf(initial?.roaDefault ?: "ORAL") }
    var ratioStr by remember { mutableStateOf(initial?.ratioJson ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Přidat Variantu" else "Upravit") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Název") })
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Cena za jednotku") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = unitStr, onValueChange = { unitStr = it }, label = { Text("Text Jednotky (např. pill, mg)") })
                OutlinedTextField(value = roaStr, onValueChange = { roaStr = it }, label = { Text("Výchozí Route (ORAL, INTRANASAL, atd.)") })
                OutlinedTextField(value = ratioStr, onValueChange = { ratioStr = it }, label = { Text("JSON Poměr (např. {\"id\": 1.0})") })
                Text("Dostupná ID Compoundů:", style = MaterialTheme.typography.labelSmall)
                compounds.forEach { c ->
                    Text("${c.name}: ${c.id}", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = priceStr.toDoubleOrNull() ?: 0.0
                    val v = initial?.copy(
                        name = name, pricePerUnit = p, unitLabel = unitStr, roaDefault = roaStr, ratioJson = ratioStr, updatedAt = System.currentTimeMillis()
                    ) ?: Variant(
                        id = "", substanceId = "", name = name, colorHex = "#FFFFFF", pricePerUnit = p, unitLabel = unitStr,
                        ratioJson = ratioStr, roaDefault = roaStr, active = true, createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis()
                    )
                    onSave(v)
                }
            ) { Text("Uložit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Zrušit") } }
    )
}
