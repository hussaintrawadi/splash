package com.splash.water.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splash.water.ui.components.BarChart
import com.splash.water.ui.theme.LocalAppGradient
import com.splash.water.ui.theme.MintGreen
import com.splash.water.ui.theme.WaterMid

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gradient = LocalAppGradient.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradient.top, gradient.bottom))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Text("History", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))

            TabRow(
                selectedTabIndex = state.tab.ordinal,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
            ) {
                HistoryTab.entries.forEach { tab ->
                    Tab(
                        selected = state.tab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = { Text(tab.label, fontWeight = FontWeight.SemiBold) },
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Intake (${state.tab.label.lowercase()})", style = MaterialTheme.typography.titleMedium)
                    BarChart(bars = state.bars, goalMl = state.goalMl)
                    Spacer(Modifier.height(12.dp))
                    ChartLegend(goalMl = state.goalMl)
                }
            }

            Spacer(Modifier.height(20.dp))
            StatGrid(state.stats)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChartLegend(goalMl: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LegendSwatch(MintGreen)
        Spacer(Modifier.width(6.dp))
        Text(
            "Goal reached ($goalMl ml)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.width(16.dp))
        LegendSwatch(WaterMid)
        Spacer(Modifier.width(6.dp))
        Text(
            "Under goal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun LegendSwatch(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color),
    )
}

@Composable
private fun StatGrid(stats: List<com.splash.water.ui.history.StatItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { stat ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stat.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stat.value,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
