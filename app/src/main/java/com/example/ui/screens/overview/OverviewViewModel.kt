package com.example.ui.screens.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.domain.model.Compound
import com.example.domain.model.Variant
import com.example.domain.model.UserSettings
import com.example.domain.pk.PKEngine
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.max

data class OverviewState(
    val currentLoadRingPct: Float = 0f,
    val activeCompoundsCount: Int = 0,
    val totalDosesCount: Int = 0,
    val activeSubstancesCount: Int = 0,
    val todaySpent: Double = 0.0,
    val recentDoses: List<Dose> = emptyList(),
    val loadTrendData: List<Float> = emptyList(),
    val maxTrendVal: Float = 1f,
    val currentLoadEst: Double = 0.0
)

class OverviewViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(OverviewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllDoses(),
                repository.getAllCompounds(),
                repository.getAllVariants(),
                repository.getActiveSubstances(),
                repository.userSettings
            ) { doses, compounds, variants, substances, settings ->
                
                val now = Instant.now().toEpochMilli()
                val twoDaysAgo = Instant.now().minus(48, ChronoUnit.HOURS).toEpochMilli()
                val recentDoses = doses.filter { it.timestamp >= twoDaysAgo }

                var currentTotalLoad = 0.0
                var activeCompsCount = 0
                val trend = mutableListOf<Float>()

                val compoundsMap = compounds.groupBy { it.substanceId }
                val variantsMap = variants.associateBy { it.id }

                // Very simple approximation:
                // We'll calculate current load right now across all recent doses.
                for (dose in recentDoses) {
                    val doseCompounds = compoundsMap[dose.substanceId] ?: emptyList()
                    val variant = dose.variantId?.let { variantsMap[it] }
                    val ratios = variant?.ratioJson?.let { PKEngine.parseRatio(it) } ?: emptyMap()
                    
                    val timeSinceHours = max(0.0, (now - dose.timestamp).toDouble() / (1000 * 60 * 60))
                    
                    var doseLoad = 0.0
                    for (c in doseCompounds) {
                        val portion = ratios[c.id] ?: 1.0
                        val res = PKEngine.calculateCompoundPK(
                            compound = c,
                            doseAmountMg = dose.doseAmount * portion,
                            route = dose.route,
                            userSettings = settings,
                            timeSinceDoseHours = timeSinceHours
                        )
                        doseLoad += res.currentLoadMg
                        if (res.currentLoadMg > c.thresholdDose * 0.1) {
                            activeCompsCount++
                        }
                    }
                    currentTotalLoad += doseLoad
                }

                // Trend mapping: past 7 days, 1 point per day
                val trendStart = Instant.now().minus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
                var maxTrend = 0f
                for (i in 0..7) {
                    val dayStart = trendStart.plus(i.toLong(), ChronoUnit.DAYS).toEpochMilli()
                    val dayDoses = doses.filter { it.timestamp >= dayStart && it.timestamp < dayStart + 86400_000L }
                    val daySum = dayDoses.sumOf { it.doseAmount }
                    trend.add(daySum.toFloat())
                    if (daySum > maxTrend) maxTrend = daySum.toFloat()
                }

                val todayStart = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli()
                val todaySpent = doses.filter { it.timestamp >= todayStart }.sumOf { it.price }

                val loadPct = (currentTotalLoad / 100.0).toFloat().coerceIn(0f, 1f) // Arbitrary normalization for UI 

                _state.update {
                    it.copy(
                        currentLoadRingPct = loadPct,
                        currentLoadEst = currentTotalLoad,
                        activeCompoundsCount = activeCompsCount,
                        totalDosesCount = doses.size,
                        activeSubstancesCount = substances.size,
                        todaySpent = todaySpent,
                        recentDoses = doses.take(5),
                        loadTrendData = trend,
                        maxTrendVal = max(1f, maxTrend)
                    )
                }
            }.collect()
        }
    }
}
