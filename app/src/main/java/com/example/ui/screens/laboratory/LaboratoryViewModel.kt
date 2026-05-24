package com.example.ui.screens.laboratory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Compound
import com.example.domain.model.Substance
import com.example.domain.model.Variant
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LabState(
    val substances: List<Substance> = emptyList(),
    val compounds: List<Compound> = emptyList(),
    val variants: List<Variant> = emptyList(),
    val selectedSubstance: Substance? = null
)

class LaboratoryViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(LabState())
    val state: StateFlow<LabState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getActiveSubstances().collect { subs ->
                _state.update { it.copy(substances = subs) }
                // Update selected substance if it exists
                val currentSelectedId = _state.value.selectedSubstance?.id
                if (currentSelectedId != null) {
                    val updatedSub = subs.find { it.id == currentSelectedId }
                    if (updatedSub != null) {
                        _state.update { it.copy(selectedSubstance = updatedSub) }
                    } else {
                        // It was deleted/archived
                        _state.update { it.copy(selectedSubstance = null, compounds = emptyList(), variants = emptyList()) }
                    }
                }
            }
        }
    }

    fun selectSubstance(substance: Substance?) {
        _state.update { it.copy(selectedSubstance = substance) }
        if (substance != null) {
            viewModelScope.launch {
                repository.getCompoundsForSubstance(substance.id).collect { cmpds ->
                    _state.update { it.copy(compounds = cmpds) }
                }
            }
            viewModelScope.launch {
                repository.getVariantsForSubstance(substance.id).collect { vars ->
                    _state.update { it.copy(variants = vars) }
                }
            }
        } else {
            _state.update { it.copy(compounds = emptyList(), variants = emptyList()) }
        }
    }

    fun saveSubstance(substance: Substance) {
        viewModelScope.launch {
            val existing = _state.value.substances.find { it.id == substance.id }
            if (existing != null) {
                repository.updateSubstance(substance)
            } else {
                repository.insertSubstance(substance.copy(id = UUID.randomUUID().toString()))
            }
        }
    }

    fun archiveSubstance(substance: Substance) {
        viewModelScope.launch {
            repository.archiveSubstance(substance.id, System.currentTimeMillis())
            if (_state.value.selectedSubstance?.id == substance.id) {
                selectSubstance(null)
            }
        }
    }

    fun saveCompound(compound: Compound) {
        viewModelScope.launch {
            val existingId = if (compound.id.isEmpty() || compound.id == "0") UUID.randomUUID().toString() else compound.id
            val toSave = compound.copy(
                id = existingId,
                substanceId = _state.value.selectedSubstance?.id ?: return@launch
            )
            if (_state.value.compounds.any { it.id == toSave.id }) {
                repository.updateCompound(toSave)
            } else {
                repository.insertCompound(toSave)
            }
        }
    }

    fun deleteCompound(id: String) {
        viewModelScope.launch { repository.deleteCompound(id) }
    }

    fun saveVariant(variant: Variant) {
        viewModelScope.launch {
            val existingId = if (variant.id.isEmpty() || variant.id == "0") UUID.randomUUID().toString() else variant.id
            val toSave = variant.copy(
                id = existingId,
                substanceId = _state.value.selectedSubstance?.id ?: return@launch
            )
            if (_state.value.variants.any { it.id == toSave.id }) {
                repository.updateVariant(toSave)
            } else {
                repository.insertVariant(toSave)
            }
        }
    }

    fun deleteVariant(id: String) {
        viewModelScope.launch { repository.deleteVariant(id) }
    }
}
