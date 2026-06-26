package com.splash.water.ui.settings

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import com.splash.water.reminder.ReminderSounds
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splash.water.data.local.ReminderSlotEntity
import com.splash.water.data.prefs.UserPreferences
import com.splash.water.domain.DateUtils
import com.splash.water.domain.GoalCalculator
import com.splash.water.domain.model.ActivityLevel
import com.splash.water.domain.model.Climate
import com.splash.water.domain.model.ReminderKind
import com.splash.water.domain.model.ReminderMode
import com.splash.water.domain.model.Sex
import com.splash.water.domain.model.ThemeMode
import com.splash.water.ui.theme.LocalAppGradient

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val slots by viewModel.slots.collectAsStateWithLifecycle()
    val p = prefs ?: return
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineLarge)

            GoalSection(p, viewModel)
            BodyStatsSection(p, viewModel)
            RemindersSection(p, slots, viewModel)
            SoundSection(p, viewModel)
            AppearanceSection(p, viewModel)
            AboutSection()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GoalSection(p: UserPreferences, vm: SettingsViewModel) {
    SettingsSection("Daily goal") {
        Text(
            "${p.goalMl} ml",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
        )
        SettingRow(
            "Auto-calculate from body stats",
            value = if (p.goalIsAuto) "On" else "Off",
        ) {
            Switch(
                checked = p.goalIsAuto,
                onCheckedChange = { auto -> if (auto) vm.useAutoGoal() else vm.setManualGoal(p.goalMl) },
            )
        }
        if (!p.goalIsAuto) {
            var goal by remember(p.manualGoalMl) { mutableStateOf(p.manualGoalMl.toFloat()) }
            Slider(
                value = goal,
                onValueChange = { goal = it },
                onValueChangeFinished = { vm.setManualGoal(goal.toInt()) },
                valueRange = GoalCalculator.MIN_GOAL_ML.toFloat()..GoalCalculator.MAX_GOAL_ML.toFloat(),
            )
            Text("${goal.toInt()} ml", fontWeight = FontWeight.Bold)
            if (GoalCalculator.isNearLimit(goal.toInt())) {
                Text(
                    "Heads up: this is near the recommended limit. Most adults do well between " +
                        "${GoalCalculator.MIN_GOAL_ML} and ${GoalCalculator.MAX_GOAL_ML} ml/day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "How this is calculated",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            GoalCalculator.EXPLANATION,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}

@Composable
private fun BodyStatsSection(p: UserPreferences, vm: SettingsViewModel) {
    var weightText by remember(p.weightKg) { mutableStateOf(p.weightKg?.let { it.toInt().toString() } ?: "") }
    var ageText by remember(p.age) { mutableStateOf(p.age?.toString() ?: "") }
    var sex by remember(p.sex) { mutableStateOf(p.sex) }
    var activity by remember(p.activity) { mutableStateOf(p.activity) }
    var climate by remember(p.climate) { mutableStateOf(p.climate) }

    SettingsSection("Body stats") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(
                value = weightText,
                onValueChange = { weightText = it },
                label = "Weight (kg)",
                maxLen = 3,
                modifier = Modifier.weight(1f),
            )
            NumberField(
                value = ageText,
                onValueChange = { ageText = it },
                label = "Age",
                maxLen = 3,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Sex", fontWeight = FontWeight.Medium)
        ChipSelector(Sex.entries, sex, { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }) { sex = it }
        Spacer(Modifier.height(8.dp))
        Text("Activity", fontWeight = FontWeight.Medium)
        ChipSelector(ActivityLevel.entries, activity, { it.label }) { activity = it }
        Spacer(Modifier.height(8.dp))
        Text("Climate", fontWeight = FontWeight.Medium)
        ChipSelector(Climate.entries, climate, { it.label }) { climate = it }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { vm.saveProfile(weightText.toDoubleOrNull(), sex, ageText.toIntOrNull(), activity, climate) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save & recalculate goal") }
    }
}

@Composable
private fun RemindersSection(p: UserPreferences, slots: List<ReminderSlotEntity>, vm: SettingsViewModel) {
    val context = LocalContext.current
    SettingsSection("Reminders") {
        SettingRow("Enable reminders", value = if (p.remindersEnabled) "On" else "Off") {
            Switch(checked = p.remindersEnabled, onCheckedChange = { vm.setRemindersEnabled(it) })
        }
        Spacer(Modifier.height(8.dp))
        Text("Mode", fontWeight = FontWeight.Medium)
        ChipSelector(
            ReminderMode.entries, p.reminderMode,
            { if (it == ReminderMode.SMART) "Smart (auto)" else "Manual" },
        ) { vm.setReminderMode(it) }

        Spacer(Modifier.height(12.dp))
        SettingRow("Awake from", DateUtils.formatMinuteOfDay(p.wakeStartMinute)) {
            OutlinedButton(onClick = {
                showTimePicker(context, p.wakeStartMinute) { vm.setWakeWindow(it, p.wakeEndMinute) }
            }) { Text("Change") }
        }
        SettingRow("Awake until", DateUtils.formatMinuteOfDay(p.wakeEndMinute)) {
            OutlinedButton(onClick = {
                showTimePicker(context, p.wakeEndMinute) { vm.setWakeWindow(p.wakeStartMinute, it) }
            }) { Text("Change") }
        }

        Spacer(Modifier.height(12.dp))
        Text("Quick-log amount: ${p.defaultLogMl} ml", fontWeight = FontWeight.Medium)
        var def by remember(p.defaultLogMl) { mutableStateOf(p.defaultLogMl.toFloat()) }
        Slider(
            value = def, onValueChange = { def = it },
            onValueChangeFinished = { vm.setDefaultLog(def.toInt()) },
            valueRange = 100f..1000f, steps = 17,
        )

        Spacer(Modifier.height(8.dp))
        Text("Snooze", fontWeight = FontWeight.Medium)
        ChipSelector(listOf(5, 10, 15, 30), p.snoozeMinutes, { "$it min" }) { vm.setSnoozeMinutes(it) }

        if (p.reminderMode == ReminderMode.MANUAL) {
            Spacer(Modifier.height(16.dp))
            Text("Your reminder times", fontWeight = FontWeight.Bold)
            slots.forEach { slot ->
                SlotRow(slot, onToggle = { vm.toggleSlot(slot, it) }, onDelete = { vm.deleteSlot(slot) })
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = {
                    showTimePicker(context, 9 * 60) { vm.addFixedSlot(it) }
                }) {
                    Icon(Icons.Filled.Add, null); Text(" Add time")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Or repeat every:", fontWeight = FontWeight.Medium)
            ChipSelector(listOf(60, 90, 120, 180), -1, { "${it / 60f}".trimEnd('0').trimEnd('.') + "h" }) {
                vm.addIntervalSlot(it)
            }
        }
    }
}

@Composable
private fun SlotRow(slot: ReminderSlotEntity, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    val label = when (ReminderKind.valueOf(slot.kind)) {
        ReminderKind.FIXED -> DateUtils.formatMinuteOfDay(slot.minuteOfDay)
        ReminderKind.INTERVAL -> "Every ${slot.intervalMinutes / 60f}h".replace(".0", "")
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = slot.enabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "Delete") }
        }
    }
}

@Composable
private fun SoundSection(p: UserPreferences, vm: SettingsViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            vm.setSoundUri(uri?.toString())
        }
    }

    // A short preview player for auditioning sounds (released on leave).
    val preview = remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(Unit) {
        onDispose { runCatching { preview.value?.release() }; preview.value = null }
    }
    fun play(resId: Int) {
        runCatching { preview.value?.release() }
        val mp = MediaPlayer.create(context, resId)
        preview.value = mp
        mp?.setOnCompletionListener { it.release(); if (preview.value === it) preview.value = null }
        mp?.start()
    }

    val isDeviceSound = !p.soundUri.isNullOrBlank() && !ReminderSounds.isBuiltin(p.soundUri)
    // Reflect what will actually play: a chosen built-in, a device sound, or the default built-in.
    val selectedId = when {
        ReminderSounds.isBuiltin(p.soundUri) -> ReminderSounds.optionFor(p.soundUri)?.id
        isDeviceSound -> null
        else -> ReminderSounds.default.id
    }

    SettingsSection("Sound & vibration") {
        SettingRow("Play sound", value = if (p.soundEnabled) "On" else "Off") {
            Switch(checked = p.soundEnabled, onCheckedChange = { vm.setSoundEnabled(it) })
        }
        SettingRow("Vibrate", value = if (p.vibrate) "On" else "Off") {
            Switch(checked = p.vibrate, onCheckedChange = { vm.setVibrate(it) })
        }
        Spacer(Modifier.height(8.dp))
        Text("Reminder sound", fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        ReminderSounds.options.forEach { opt ->
            SoundRow(
                label = opt.label,
                selected = selectedId == opt.id,
                onSelect = { vm.setSoundUri(ReminderSounds.storageValue(opt)); play(opt.resId) },
                onPreview = { play(opt.resId) },
            )
        }
        SoundRow(
            label = "Device sound…",
            selected = isDeviceSound,
            onSelect = {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Choose reminder sound")
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    p.soundUri?.let { putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it)) }
                }
                launcher.launch(intent)
            },
            onPreview = null,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Rings like a call. It won't sound on silent or Do Not Disturb.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
    }
}

@Composable
private fun SoundRow(label: String, selected: Boolean, onSelect: () -> Unit, onPreview: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp).clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (onPreview != null) {
            IconButton(onClick = onPreview) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Preview $label")
            }
        }
    }
}

@Composable
private fun AppearanceSection(p: UserPreferences, vm: SettingsViewModel) {
    SettingsSection("Appearance") {
        Text("Theme", fontWeight = FontWeight.Medium)
        ChipSelector(
            ThemeMode.entries, p.themeMode,
            { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
        ) { vm.setTheme(it) }
    }
}

@Composable
private fun AboutSection() {
    SettingsSection("About") {
        Text(
            "Splash 💧 v1.0\n\nHydration tips and milestones are friendly motivation, not medical " +
                "advice. If you have a health condition that affects fluid intake, follow your " +
                "doctor's guidance.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
    }
}
