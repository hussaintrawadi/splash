package com.splash.water.reminder

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import com.splash.water.reminder.ReminderConstants.EXTRA_AMOUNT

/**
 * Foreground service that plays a looping alarm sound + vibration and shows the ringing reminder
 * notification. It keeps going until an action (Drank / Snooze / Dismiss) stops it, by design,
 * so the user has to consciously respond.
 *
 * Needed values are passed as intent extras (already read from prefs by [ReminderReceiver]) so the
 * service can call startForeground immediately without any async work.
 */
class ReminderSoundService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ReminderConstants.ACTION_STOP_SOUND) {
            stopEverything()
            return START_NOT_STICKY
        }

        val amount = intent?.getIntExtra(EXTRA_AMOUNT, 250) ?: 250
        val soundEnabled = intent?.getBooleanExtra(EXTRA_SOUND, true) ?: true
        val vibrate = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true
        val soundUri = intent?.getStringExtra(EXTRA_SOUND_URI)
        val skipCount = intent?.getIntExtra(ReminderConstants.EXTRA_SKIP_COUNT, 0) ?: 0
        val lastDrink = intent?.getLongExtra(ReminderConstants.EXTRA_LAST_DRINK, 0L) ?: 0L

        val notification = NotificationHelper.buildReminderNotification(this, amount, skipCount, lastDrink)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this, ReminderConstants.FOREGROUND_NOTIF_ID, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(ReminderConstants.FOREGROUND_NOTIF_ID, notification)
        }

        // Respect the user's ringer: never ring on silent or while Do Not Disturb is active.
        val silenced = isSilencedBySystem()
        if (soundEnabled && !silenced) startSound(soundUri)
        // Vibrate unless fully silenced (vibrate ringer mode still gets a buzz).
        if (vibrate && !isVibrationSuppressed()) startVibration()
        return START_STICKY
    }

    /** True when the phone is on silent/vibrate or Do Not Disturb, so we shouldn't play a sound. */
    private fun isSilencedBySystem(): Boolean {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.ringerMode != AudioManager.RINGER_MODE_NORMAL) return true
        return isDndActive()
    }

    /** Under full-silence DND no buzz either; otherwise vibrate is allowed. */
    private fun isVibrationSuppressed(): Boolean {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (am.ringerMode == AudioManager.RINGER_MODE_SILENT) return true
        val nm = getSystemService(NotificationManager::class.java)
        return nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
    }

    private fun isDndActive(): Boolean {
        val nm = getSystemService(NotificationManager::class.java)
        val filter = nm.currentInterruptionFilter
        return filter != NotificationManager.INTERRUPTION_FILTER_ALL &&
            filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
    }

    private fun startSound(soundUri: String?) {
        runCatching {
            val uri: Uri = ReminderSounds.resolve(this, soundUri)
            player = MediaPlayer().apply {
                // Ringtone usage = "call" category: respects the ringer and DND like a phone call.
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@ReminderSoundService, uri)
                isLooping = true
                prepare()
                start()
            }
        }
    }

    private fun startVibration() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator = vib
        val pattern = longArrayOf(0, 450, 350, 450, 1600)
        vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopEverything() {
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        runCatching { vibrator?.cancel() }
        vibrator = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopEverything()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_SOUND = "extra_sound_enabled"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_SOUND_URI = "extra_sound_uri"

        fun start(
            context: Context,
            amountMl: Int,
            soundEnabled: Boolean,
            vibrate: Boolean,
            soundUri: String?,
            skipCount: Int = 0,
            lastDrink: Long = 0L,
        ) {
            val intent = Intent(context, ReminderSoundService::class.java).apply {
                putExtra(EXTRA_AMOUNT, amountMl)
                putExtra(EXTRA_SOUND, soundEnabled)
                putExtra(EXTRA_VIBRATE, vibrate)
                putExtra(EXTRA_SOUND_URI, soundUri)
                putExtra(ReminderConstants.EXTRA_SKIP_COUNT, skipCount)
                putExtra(ReminderConstants.EXTRA_LAST_DRINK, lastDrink)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ReminderSoundService::class.java).apply {
                action = ReminderConstants.ACTION_STOP_SOUND
            }
            context.startService(intent)
        }
    }
}
