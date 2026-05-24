package com.example.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Dose
import com.example.domain.model.Substance
import com.example.domain.repository.BioTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class AnalyticsState(
    val totalDoses: Int = 0,
    val totalSpent: Double = 0.0,
    val dosesPerDay: Map<String, Int> = emptyMap(),
    val spendingPerSubstance: Map<String, Double> = emptyMap(),
    val substances: List<Substance> = emptyList(),
    val activeTab: Int = 0
)

class AnalyticsViewModel(private val repository: BioTrackRepository) : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getAllDoses(),
                repository.getActiveSubstances()
            ) { doses, substances ->
                val totalDoses = doses.size
                val totalSpent = doses.sumOf { it.price }

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
                val perDay = mutableMapOf<String, Int>()
                val spendingSubMap = mutableMapOf<String, Double>()
                
                val subMap = substances.associateBy { it.id }

                for (d in doses) {
                    val date = formatter.format(Instant.ofEpochMilli(d.timestamp))
                    perDay[date] = (perDay[date] ?: 0) + 1
                    
                    val subName = subMap[d.substanceId]?.name ?: "Neznámá"
                    spendingSubMap[subName] = (spendingSubMap[subName] ?: 0.0) + d.price
                }

                // Fill missing days with 0 in the last 7 days for the chart
                val last7Days = (6 downTo 0).map { 
                    formatter.format(Instant.now().minus(it.toLong(), ChronoUnit.DAYS))
                }
                
                val alignedPerDay = last7Days.associateWith { perDay[it] ?: 0 }

                _state.update {
                    it.copy(
                        totalDoses = totalDoses,
                        totalSpent = totalSpent,
                        dosesPerDay = alignedPerDay,
                        spendingPerSubstance = spendingSubMap,
                        substances = substances
                    )
                }
            }.collect()
        }
    }

    fun setTab(index: Int) {
        _state.update { it.copy(activeTab = index) }
    }
}
