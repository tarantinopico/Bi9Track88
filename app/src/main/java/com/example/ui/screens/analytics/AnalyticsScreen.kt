package com.example.ui.screens.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.repository.BioTrackRepository
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(repository: BioTrackRepository) {
    val viewModel: AnalyticsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AnalyticsViewModel(repository) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Analytika") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = state.activeTab) {
                Tab(selected = state.activeTab == 0, onClick = { viewModel.setTab(0) }) {
                    Text("Shrnutí", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = state.activeTab == 1, onClick = { viewModel.setTab(1) }) {
                    Text("Finance", modifier = Modifier.padding(16.dp))
                }
            }
            
            Box(Modifier.fillMaxSize()) {
                if (state.activeTab == 0) {
                    SummaryTab(state)
                } else {
                    FinanceTab(state)
                }
            }
        }
    }
}

@Composable
fun SummaryTab(state: AnalyticsState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Celkový počet záznamů: ${state.totalDoses}", style = MaterialTheme.typography.titleLarge)
                    Text("Sledovaných látek: ${state.substances.size}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        item {
            Text("Dávkování (Posledních 7 dnů)", style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                val primary = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    val entries = state.dosesPerDay.entries.toList()
                    if (entries.isNotEmpty()) {
                        val maxVal = max(1, entries.maxOf { it.value })
                        val barWidth = size.width / (entries.size * 2)
                        val spacing = size.width / entries.size
                        entries.forEachIndexed { i, entry ->
                            val h = (entry.value.toFloat() / maxVal) * size.height
                            drawRect(
                                color = primary,
                                topLeft = Offset(i * spacing + spacing/2 - barWidth/2, size.height - h),
                                size = Size(barWidth, h)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceTab(state: AnalyticsState) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Celková útrata", style = MaterialTheme.typography.titleMedium)
                    Text("${String.format("%.2f", state.totalSpent)}", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text("Útrata podle látky", style = MaterialTheme.typography.titleMedium)
        }
        items(state.spendingPerSubstance.entries.toList()) { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entry.key, style = MaterialTheme.typography.bodyLarge)
                    Text(String.format("%.2f", entry.value), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
