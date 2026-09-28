package com.splash.water.reminder

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import com.splash.water.R

/**
 * A selectable reminder tone. [resId] 0 is the special "system alarm" option, which uses the
 * phone's own alarm sound — the most reliably loud/long ring on any device, so it's the default.
 */
data class SoundOption(val id: String, val label: String, val resId: Int) {
    val isSystem: Boolean get() = resId == 0
}

/**
 * Catalog of reminder sounds plus URI resolution. The chosen option is persisted as `builtin:<id>`;
 * a device/system sound is stored as its raw content URI; null falls back to the default (system
 * alarm). Everything is played on the ALARM stream, so it rings like an alarm clock.
 */
object ReminderSounds {
    private const val PREFIX = "builtin:"
    const val SYSTEM_ID = "system"

    /** The phone's own alarm sound — guaranteed loud and long. Default out of the box. */
    val systemAlarm = SoundOption(SYSTEM_ID, "Default alarm", 0)

    /** Bundled, royalty-free continuous ring tones (generated for this app). */
    val builtins: List<SoundOption> = listOf(
        SoundOption("droplet", "Droplet", R.raw.rem_droplet),
        SoundOption("chime", "Chime", R.raw.rem_chime),
        SoundOption("marimba", "Marimba", R.raw.rem_marimba),
        SoundOption("bell", "Bell", R.raw.rem_bell),
        SoundOption("bubbles", "Bubbles", R.raw.rem_bubbles),
        SoundOption("digital", "Digital", R.raw.rem_digital),
        SoundOption("classic", "Classic", R.raw.rem_classic),
    )

    /** All selectable options, system alarm first. */
    val options: List<SoundOption> = listOf(systemAlarm) + builtins

    val default: SoundOption = systemAlarm

    fun storageValue(option: SoundOption): String = PREFIX + option.id

    fun isBuiltin(pref: String?): Boolean = pref?.startsWith(PREFIX) == true

    /** The catalog option matching the stored preference, or null if it's a device sound URI. */
    fun optionFor(pref: String?): SoundOption? =
        if (isBuiltin(pref)) options.firstOrNull { it.id == pref!!.removePrefix(PREFIX) } else null

    private fun systemAlarmUri(): Uri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: Settings.System.DEFAULT_ALARM_ALERT_URI

    fun resUri(context: Context, resId: Int): Uri =
        Uri.parse("android.resource://${context.packageName}/$resId")

    /** The playable URI for a specific option (used for previews). */
    fun uriFor(context: Context, option: SoundOption): Uri =
        if (option.isSystem) systemAlarmUri() else resUri(context, option.resId)

    /** Resolve the current preference to an actual playable URI. */
    fun resolve(context: Context, pref: String?): Uri {
        optionFor(pref)?.let { return uriFor(context, it) }
        if (!pref.isNullOrBlank()) return Uri.parse(pref) // device / system sound picked by the user
        return systemAlarmUri() // default
    }
}
