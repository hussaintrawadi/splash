package com.splash.water.ui.home

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splash.water.domain.DateUtils
import com.splash.water.ui.components.ConfettiBurst
import com.splash.water.ui.log.LogSheet
import com.splash.water.ui.theme.LocalAppGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    val gradient = LocalAppGradient.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.Logged -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                is HomeEvent.GoalReached -> {
                    showConfetti = true
                    scope.launch { snackbarHostState.showSnackbar("Goal reached, you're a hydration champion!") }
                }
                is HomeEvent.MilestoneUnlocked ->
                    scope.launch { snackbarHostState.showSnackbar("Unlocked: ${event.def.title}") }
            }
        }
    }
    LaunchedEffect(showConfetti) {
        if (showConfetti) { delay(3500); showConfetti = false }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradient.top, gradient.bottom))),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Stay hydrated", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            if (state.goalReached) "Goal complete, amazing!" else "${state.remainingMl} ml to go",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        )
                    }
                    StreakChip(state.streak)
                }
                Spacer(Modifier.height(20.dp))
                WaterOrb(
                    progress = state.progress,
                    currentMl = state.currentMl,
                    goalMl = state.goalMl,
                )
                Spacer(Modifier.height(24.dp))
                QuickAddRow(
                    defaultMl = state.defaultLogMl,
                    onQuickAdd = viewModel::log,
                    onOpenSheet = { showSheet = true },
                )
                Spacer(Modifier.height(24.dp))
                if (state.logs.isNotEmpty()) {
                    Text(
                        "Today's log",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            items(state.logs, key = { it.id }) { log ->
                LogRow(
                    amountMl = log.amountMl,
                    time = DateUtils.formatClock(log.timestamp),
                    onDelete = { viewModel.undo(log) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        AnimatedVisibility(visible = showConfetti) {
            ConfettiBurst(modifier = Modifier.fillMaxSize())
        }
    }

    if (showSheet) {
        LogSheet(
            onDismiss = { showSheet = false },
            onConfirm = { amount ->
                viewModel.log(amount)
                showSheet = false
            },
        )
    }
}

@Composable
private fun StreakChip(streak: Int) {
    AssistChip(
        onClick = {},
        label = { Text("$streak day${if (streak == 1) "" else "s"}", fontWeight = FontWeight.Bold) },
        leadingIcon = { Text("🔥") },
    )
}

@Composable
private fun QuickAddRow(
    defaultMl: Int,
    onQuickAdd: (Int) -> Unit,
    onOpenSheet: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(defaultMl, 500).distinct().forEach { amount ->
                AssistChip(onClick = { onQuickAdd(amount) }, label = { Text("+$amount ml") })
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onOpenSheet,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Log water", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun LogRow(amountMl: Int, time: String, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp).height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("💧  $amountMl ml", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
