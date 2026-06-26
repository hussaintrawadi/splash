package com.splash.water.ui.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.splash.water.domain.DateUtils
import com.splash.water.domain.GoalCalculator
import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Climate
import com.splash.water.domain.model.ReminderMode
import com.splash.water.domain.model.Sex
import com.splash.water.ui.settings.ChipSelector
import com.splash.water.ui.settings.NumberField
import com.splash.water.ui.settings.showTimePicker
import com.splash.water.ui.theme.WaterDeep
import com.splash.water.ui.theme.WaterLight
import com.splash.water.ui.theme.WaterMid

@Composable
fun OnboardingFlow(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }

    // Collected inputs
    var useStats by remember { mutableStateOf(true) }
    var weightText by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf(Sex.UNSPECIFIED) }
    var activity by remember { mutableStateOf(ActivityLevel.LIGHT) }
    var climate by remember { mutableStateOf(Climate.TEMPERATE) }
    var wakeStart by remember { mutableIntStateOf(8 * 60) }
    var wakeEnd by remember { mutableIntStateOf(22 * 60) }
    var mode by remember { mutableStateOf(ReminderMode.SMART) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    val previewGoal = if (useStats) {
        GoalCalculator.computeGoal(weightText.toDoubleOrNull(), sex, ageText.toIntOrNull(), activity, climate)
    } else {
        GoalCalculator.DEFAULT_GOAL_ML
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(WaterLight, WaterMid, WaterDeep))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Column(
              modifier = Modifier
                  .weight(1f)
                  .fillMaxWidth()
                  .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Spacer(Modifier.height(24.dp))
            AnimatedContent(targetState = step, transitionSpec = { fadeThrough() }, label = "step") { s ->
                when (s) {
                    0 -> Welcome()
                    1 -> BodyStats(
                        useStats, { useStats = it }, weightText, { weightText = it }, ageText, { ageText = it },
                        sex, { sex = it }, activity, { activity = it }, climate, { climate = it },
                    )
                    2 -> GoalPreview(previewGoal, useStats)
                    3 -> Schedule(
                        context, wakeStart, { wakeStart = it }, wakeEnd, { wakeEnd = it }, mode, { mode = it },
                    )
                    else -> Permissions(
                        onGrantNotifications = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onGrantAlarms = { openExactAlarmSettings(context) },
                        onIgnoreBattery = { openBatterySettings(context) },
                    )
                }
            }
          }

            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) { Text("Back", color = Color.White) }
                } else {
                    Spacer(Modifier.height(1.dp))
                }
                Button(
                    onClick = {
                        if (step < 4) {
                            step++
                        } else {
                            viewModel.complete(
                                useStats, weightText.toDoubleOrNull(), sex, ageText.toIntOrNull(), activity, climate,
                                wakeStart, wakeEnd, mode, onDone,
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = WaterDeep),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Text(if (step < 4) "Next" else "Start sipping 💧", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Welcome() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("💧", fontSize = 96.sp)
        Spacer(Modifier.height(16.dp))
        Text("Welcome to Splash", color = Color.White, fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.displayLarge, fontSize = 34.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "Your playful hydration buddy. Log every sip, watch your ring fill up, and unlock fun " +
                "rewards as you build a healthy habit.",
            color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun BodyStats(
    useStats: Boolean, onUseStats: (Boolean) -> Unit,
    weightText: String, onWeight: (String) -> Unit,
    ageText: String, onAge: (String) -> Unit,
    sex: Sex, onSex: (Sex) -> Unit,
    activity: ActivityLevel, onActivity: (ActivityLevel) -> Unit,
    climate: Climate, onClimate: (Climate) -> Unit,
) {
    OnboardCard("Tell us about you", "We'll estimate a healthy daily goal. You can change it anytime.") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Use my body stats", fontWeight = FontWeight.Medium)
            Switch(checked = useStats, onCheckedChange = onUseStats)
        }
        if (useStats) {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(weightText, onWeight, "Weight (kg)", 3, Modifier.weight(1f))
                NumberField(ageText, onAge, "Age", 3, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text("Sex", fontWeight = FontWeight.Medium)
            ChipSelector(Sex.entries, sex, { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }, onSelect = onSex)
            Spacer(Modifier.height(8.dp))
            Text("Activity", fontWeight = FontWeight.Medium)
            ChipSelector(ActivityLevel.entries, activity, { it.label }, onSelect = onActivity)
            Spacer(Modifier.height(8.dp))
            Text("Climate", fontWeight = FontWeight.Medium)
            ChipSelector(Climate.entries, climate, { it.label }, onSelect = onClimate)
        } else {
            Spacer(Modifier.height(8.dp))
            Text("No problem, we'll start you at ${GoalCalculator.DEFAULT_GOAL_ML} ml/day.")
        }
    }
}

@Composable
private fun GoalPreview(goal: Int, useStats: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Your daily goal", color = Color.White, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text("$goal ml", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 64.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            if (useStats) "Calculated from your stats, always editable in Settings."
            else "A sensible default, editable in Settings.",
            color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Schedule(
    context: android.content.Context,
    wakeStart: Int, onWakeStart: (Int) -> Unit,
    wakeEnd: Int, onWakeEnd: (Int) -> Unit,
    mode: ReminderMode, onMode: (ReminderMode) -> Unit,
) {
    OnboardCard("When should we remind you?", "Smart mode spreads reminders across your day automatically.") {
        Text("Reminder style", fontWeight = FontWeight.Medium)
        ChipSelector(ReminderMode.entries, mode,
            { if (it == ReminderMode.SMART) "Smart (auto)" else "Manual" }, onSelect = onMode)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Awake from ${DateUtils.formatMinuteOfDay(wakeStart)}")
            OutlinedButton(onClick = { showTimePicker(context, wakeStart, onWakeStart) }) { Text("Change") }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Awake until ${DateUtils.formatMinuteOfDay(wakeEnd)}")
            OutlinedButton(onClick = { showTimePicker(context, wakeEnd, onWakeEnd) }) { Text("Change") }
        }
    }
}

@Composable
private fun Permissions(
    onGrantNotifications: () -> Unit,
    onGrantAlarms: () -> Unit,
    onIgnoreBattery: () -> Unit,
) {
    OnboardCard("One last thing", "Allow these so reminders actually reach you on time.") {
        Button(onClick = onGrantNotifications, modifier = Modifier.fillMaxWidth()) {
            Text("Allow notifications")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onGrantAlarms, modifier = Modifier.fillMaxWidth()) {
            Text("Allow alarms & full-screen")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onIgnoreBattery, modifier = Modifier.fillMaxWidth()) {
            Text("Ignore battery optimization")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "These are optional but strongly recommended for reliable reminders.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun OnboardCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(28.dp))
            .padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(Modifier.height(16.dp))
        content()
    }
}

private fun fadeThrough() =
    (androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut())

private fun openExactAlarmSettings(context: android.content.Context) {
    runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${context.packageName}".toUri())
            )
        } else {
            openAppDetails(context)
        }
    }.onFailure { openAppDetails(context) }
}

private fun openBatterySettings(context: android.content.Context) {
    runCatching {
        context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
    }.onFailure { openAppDetails(context) }
}

private fun openAppDetails(context: android.content.Context) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
        )
    }
}

private fun String.toUri() = android.net.Uri.parse(this)
