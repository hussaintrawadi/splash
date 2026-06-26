package com.splash.water.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val PRESETS = listOf(150, 200, 250, 330, 500, 750, 1000)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LogSheet(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var custom by remember { mutableFloatStateOf(300f) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Text("Log water 💧", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PRESETS.forEach { amount ->
                    FilterChip(
                        selected = false,
                        onClick = { onConfirm(amount) },
                        label = { Text("$amount ml") },
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Custom", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Text(
                    "${custom.toInt()} ml",
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                )
            }
            Slider(
                value = custom,
                onValueChange = { custom = it },
                valueRange = 50f..2000f,
                steps = ((2000 - 50) / 50) - 1,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onConfirm(custom.toInt()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add ${custom.toInt()} ml")
            }
        }
    }
}
