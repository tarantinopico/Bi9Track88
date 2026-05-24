package com.example.ui.screens.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Compound
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.domain.model.UserSettings
import com.example.domain.model.Variant
import com.example.domain.pk.OverallPKResult
import com.example.domain.pk.PKEngine
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class RecordState(
    val substances: List<Substance> = emptyList(),
    val selectedSubstance: Substance? = null,
    val variants: List<Variant> = emptyList(),
    val selectedVariant: Variant? = null,
    val compounds: List<Compound> = emptyList(),
    val amountStr: String = "",
    val route: String = "ORAL",
    val pkEstimate: OverallPKResult? = null,
    val calculatedPrice: Double = 0.0,
    val doseSaved: Boolean = false,
    val settings: UserSettings = UserSettings()
)

class RecordViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userSettings.collect { settings ->
                _state.update { it.copy(settings = settings) }
                recalculatePK()
            }
        }
        viewModelScope.launch {
            repository.getActiveSubstances().collect { subs ->
                _state.update { it.copy(substances = subs) }
                // if selected substance vanished, clear it
                if (subs.none { it.id == _state.value.selectedSubstance?.id }) {
                    _state.update { it.copy(selectedSubstance = null, variants = emptyList(), compounds = emptyList(), selectedVariant = null) }
                    recalculatePK()
                }
            }
        }
    }

    fun selectSubstance(substanceId: String) {
        val s = _state.value.substances.find { it.id == substanceId }
        _state.update { it.copy(selectedSubstance = s, selectedVariant = null, amountStr = "") }
        if (s != null) {
            viewModelScope.launch {
                repository.getVariantsForSubstance(s.id).collect { vars ->
                    _state.update { it.copy(variants = vars) }
                }
            }
            viewModelScope.launch {
                repository.getCompoundsForSubstance(s.id).collect { cmpds ->
                    _state.update { it.copy(compounds = cmpds) }
                    recalculatePK()
                }
            }
        } else {
            _state.update { it.copy(variants = emptyList(), compounds = emptyList()) }
            recalculatePK()
        }
    }

    fun selectVariant(variantId: String) {
        val v = _state.value.variants.find { it.id == variantId }
        _state.update { 
            it.copy(
                selectedVariant = v, 
                route = v?.roaDefault?.takeIf { r -> r.isNotBlank() } ?: "ORAL" 
            ) 
        }
        recalculatePK()
    }

    fun updateAmount(amount: String) {
        _state.update { it.copy(amountStr = amount) }
        recalculatePK()
    }

    fun updateRoute(route: String) {
        _state.update { it.copy(route = route) }
        recalculatePK()
    }

    fun saveDose() {
        val currentState = _state.value
        val amount = currentState.amountStr.toDoubleOrNull() ?: return
        val sub = currentState.selectedSubstance ?: return

        viewModelScope.launch {
            repository.insertDose(
                Dose(
                    id = UUID.randomUUID().toString(),
                    substanceId = sub.id,
                    variantId = currentState.selectedVariant?.id,
                    doseAmount = amount,
                    unit = currentState.selectedVariant?.unitLabel?.takeIf { it.isNotBlank() } ?: sub.defaultUnit,
                    route = currentState.route,
                    price = currentState.calculatedPrice,
                    timestamp = System.currentTimeMillis(),
                    notes = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            _state.update { it.copy(doseSaved = true, amountStr = "") }
        }
    }

    fun resetDoseStatus() {
        _state.update { it.copy(doseSaved = false) }
    }

    private fun recalculatePK() {
        val currentState = _state.value
        val amount = currentState.amountStr.toDoubleOrNull() ?: 0.0
        
        var price = 0.0
        if (currentState.selectedVariant != null) {
            price = currentState.selectedVariant.pricePerUnit * amount
        }
        _state.update { it.copy(calculatedPrice = price) }

        if (currentState.compounds.isEmpty() || amount <= 0) {
            _state.update { it.copy(pkEstimate = null) }
            return
        }

        val ratios = currentState.selectedVariant?.ratioJson?.let { PKEngine.parseRatio(it) } ?: emptyMap()

        val results = currentState.compounds.map { compound ->
            val factor = ratios[compound.id] ?: 1.0 // If no ratio defined, assume 100% (1.0)
            val compoundDose = amount * factor
            PKEngine.calculateCompoundPK(
                compound = compound,
                doseAmountMg = compoundDose,
                route = currentState.route,
                userSettings = currentState.settings,
                timeSinceDoseHours = 0.0 // Predicting for immediately after onset
            )
        }.filter { it.peakLoadMg > 0.0 }

        val allWarnings = results.flatMap { it.warnings }
        val maxDuration = results.maxOfOrNull { it.durationHours } ?: 0.0
        val avgTmax = if (results.isNotEmpty()) results.sumOf { it.tmaxHours } / results.size else 0.0

        val overall = OverallPKResult(
            activeCompounds = results,
            totalWarnings = allWarnings,
            estimatedDurationHours = maxDuration,
            estimatedTmaxHours = avgTmax
        )
        _state.update { it.copy(pkEstimate = overall) }
    }
}
