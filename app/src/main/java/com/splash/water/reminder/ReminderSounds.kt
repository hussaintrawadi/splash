package com.splash.water.reminder

import android.content.Context
import android.net.Uri
import com.splash.water.R

/** A selectable built-in reminder tone (bundled, royalty-free, generated for this app). */
data class SoundOption(val id: String, val label: String, val resId: Int)

/**
 * Catalog of built-in reminder sounds plus URI resolution. The chosen option is persisted in
 * prefs as `builtin:<id>`; a device/system sound is stored as its raw content URI; null falls
 * back to the default built-in.
 */
object ReminderSounds {
    private const val PREFIX = "builtin:"

    val options: List<SoundOption> = listOf(
        SoundOption("droplet", "Droplet", R.raw.rem_droplet),
        SoundOption("chime", "Chime", R.raw.rem_chime),
        SoundOption("marimba", "Marimba", R.raw.rem_marimba),
        SoundOption("bell", "Bell", R.raw.rem_bell),
        SoundOption("bubbles", "Bubbles", R.raw.rem_bubbles),
        SoundOption("digital", "Digital", R.raw.rem_digital),
        SoundOption("classic", "Classic", R.raw.rem_classic),
    )

    /** Pleasant default so there's always a sound even before the user picks one. */
    val default: SoundOption = options[1] // Chime

    fun storageValue(option: SoundOption): String = PREFIX + option.id

    fun isBuiltin(pref: String?): Boolean = pref?.startsWith(PREFIX) == true

    /** The built-in option matching the stored preference, or null if it's a device sound. */
    fun optionFor(pref: String?): SoundOption? =
        if (isBuiltin(pref)) options.firstOrNull { it.id == pref!!.removePrefix(PREFIX) } else null

    fun resUri(context: Context, resId: Int): Uri =
        Uri.parse("android.resource://${context.packageName}/$resId")

    /** Resolve the current preference to an actual playable URI. */
    fun resolve(context: Context, pref: String?): Uri {
        optionFor(pref)?.let { return resUri(context, it.resId) }
        if (!pref.isNullOrBlank()) return Uri.parse(pref) // device / system sound
        return resUri(context, default.resId)
    }
}
