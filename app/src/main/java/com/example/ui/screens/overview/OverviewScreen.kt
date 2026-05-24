package com.example.ui.screens.overview

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.repository.BioTrackRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(repository: BioTrackRepository) {
    val viewModel: OverviewViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return OverviewViewModel(repository) as T
            }
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Přehled") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val primary = MaterialTheme.colorScheme.primary
                        Canvas(modifier = Modifier.size(150.dp)) {
                            drawArc(
                                color = primary.copy(alpha = 0.2f),
                                startAngle = 135f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = primary,
                                startAngle = 135f,
                                sweepAngle = 270f * state.currentLoadRingPct,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Aktivní Zátěž", style = MaterialTheme.typography.labelMedium)
                            Text("${String.format("%.1f", state.currentLoadEst)} mg", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricCard(modifier = Modifier.weight(1f), label = "Aktivní", value = "${state.activeCompoundsCount}")
                    MetricCard(modifier = Modifier.weight(1f), label = "Látky", value = "${state.activeSubstancesCount}")
                    MetricCard(modifier = Modifier.weight(1f), label = "Dnes", value = "${state.todaySpent}")
                }
            }

            item {
                Text("Trend zátěže (posledních 7 dnů)", style = MaterialTheme.typography.titleMedium)
                Card(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                    val primary = MaterialTheme.colorScheme.primary
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val maxVal = state.maxTrendVal
                        val pts = state.loadTrendData
                        if (pts.size > 1) {
                            val w = size.width / (pts.size - 1)
                            val h = size.height
                            for (i in 0 until pts.size - 1) {
                                val x1 = i * w
                                val y1 = h - (pts[i] / maxVal * h)
                                val x2 = (i + 1) * w
                                val y2 = h - (pts[i + 1] / maxVal * h)
                                drawLine(
                                    color = primary,
                                    start = Offset(x1, y1),
                                    end = Offset(x2, y2),
                                    strokeWidth = 4.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("Poslední záznamy", style = MaterialTheme.typography.titleMedium)
            }
            if (state.recentDoses.isEmpty()) {
                item {
                    Text("Zatím žádné záznamy.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                items(state.recentDoses) { dose ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Látka ID: ${dose.substanceId}", style = MaterialTheme.typography.titleMedium)
                            Text("Množství: ${dose.doseAmount} ${dose.unit}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}
