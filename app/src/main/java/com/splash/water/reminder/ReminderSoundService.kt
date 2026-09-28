package com.splash.water.reminder

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ServiceCompat
import com.splash.water.reminder.ReminderConstants.EXTRA_AMOUNT

/**
 * Foreground service that plays a loud, looping ALARM sound + vibration and shows the ringing
 * reminder notification. It rings like an alarm clock: on the alarm stream at full volume, over a
 * wake lock so it keeps playing with the screen off, and holding audio focus so it plays over music
 * and earbuds. It keeps ringing until the user acts (Drank / Snooze / Dismiss), with a safety
 * auto-stop so it can never ring forever.
 */
class ReminderSoundService : Service() {

    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private val handler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { stopEverything() }

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

        acquireWakeLock()
        // Ring like an alarm: always sound (alarms are meant to be heard, even on silent/vibrate).
        if (soundEnabled) startSound(soundUri)
        if (vibrate) startVibration()

        // Safety net: never ring longer than this even if the user never taps a button.
        handler.removeCallbacks(autoStop)
        handler.postDelayed(autoStop, MAX_RING_MS)
        return START_STICKY
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "splash:reminder",
        ).also { runCatching { it.acquire(MAX_RING_MS + 5_000) } }
    }

    private fun startSound(soundUri: String?) {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        // Duck other audio / take focus so the alarm is clearly heard over music and on earbuds.
        audioManager = (getSystemService(Context.AUDIO_SERVICE) as AudioManager).also { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(attrs)
                    .build()
                runCatching { am.requestAudioFocus(focusRequest!!) }
            } else {
                @Suppress("DEPRECATION")
                runCatching {
                    am.requestAudioFocus(null, AudioManager.STREAM_ALARM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                }
            }
        }
        runCatching {
            val uri: Uri = ReminderSounds.resolve(this, soundUri)
            player = MediaPlayer().apply {
                setAudioAttributes(attrs)
                setDataSource(this@ReminderSoundService, uri)
                isLooping = true
                setVolume(1f, 1f)
                setOnPreparedListener { it.start() }
                setOnErrorListener { _, _, _ -> playFallbackAlarm(attrs); true }
                prepareAsync()
            }
        }.onFailure { playFallbackAlarm(attrs) }
    }

    /** If the chosen sound can't be played for any reason, fall back to the system alarm tone. */
    private fun playFallbackAlarm(attrs: AudioAttributes) {
        runCatching {
            player?.release()
            val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                ?: android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI ?: return
            player = MediaPlayer().apply {
                setAudioAttributes(attrs)
                setDataSource(this@ReminderSoundService, uri)
                isLooping = true
                setVolume(1f, 1f)
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
        val pattern = longArrayOf(0, 500, 400, 500, 1200)
        vib.vibrate(VibrationEffect.createWaveform(pattern, 0))
    }

    private fun stopEverything() {
        handler.removeCallbacks(autoStop)
        runCatching { player?.stop() }
        runCatching { player?.release() }
        player = null
        runCatching {
            val am = audioManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am?.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am?.abandonAudioFocus(null)
            }
        }
        audioManager = null
        focusRequest = null
        runCatching { vibrator?.cancel() }
        vibrator = null
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
        wakeLock = null
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

        /** Ring for at most this long if the user never responds (2 minutes). */
        private const val MAX_RING_MS = 120_000L

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
