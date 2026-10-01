package com.example.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import android.speech.tts.TextToSpeech
import androidx.core.app.NotificationCompat
import com.aistudio.ameen.medication.R
import com.example.MainActivity
import com.example.data.db.AmeenDatabase
import com.example.data.repository.MedicationRepository
import com.example.receiver.MedicationActionReceiver
import com.example.receiver.MedicationAlarmReceiver
import com.example.ui.screens.AlarmFullScreenActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Locale

data class ActiveAlarmState(
    val medicationId: String,
    val medicationName: String,
    val memberName: String,
    val memberId: String,
    val isCritical: Boolean,
    val scheduledAt: Long,
    val scheduleEntryId: String?,
    val escalationLevel: Int = 0,
    val isRinging: Boolean = true
)

/**
 * Foreground Service for looping alarm playback using USAGE_ALARM stream.
 * Supports Ringtone, TTS (Text-To-Speech with Arabic synthesis), Custom Audio File, and User-Recorded Voice.
 * Acquires a PARTIAL_WAKE_LOCK only for the ringing duration (max 2 min).
 * Vibrates via VibrationEffect.createWaveform with escalation tier amplitudes.
 * Supports lock-screen privacy via setPublicVersion().
 */
class AlarmSoundService : Service(), TextToSpeech.OnInitListener {

    private var mediaPlayer: MediaPlayer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var safetyTimeoutJob: Job? = null
    private var ttsLoopJob: Job? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null

    private var medicationId: String = ""
    private var medicationName: String = ""
    private var familyMemberId: String = ""
    private var familyMemberName: String = ""
    private var isCritical: Boolean = false
    private var soundType: String = "TONE"
    private var soundUri: String? = null
    private var scheduledAt: Long = 0L
    private var scheduleEntryId: String? = null
    private var ttsTemplate: String = "DEFAULT"
    private var escalationLevel: Int = 0

    companion object {
        const val CHANNEL_ID = "ameen_alarm_playback_channel"
        const val NOTIFICATION_ID = 9901

        const val ACTION_START_ALARM = "com.example.ameen.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.ameen.ACTION_STOP_ALARM"

        const val EXTRA_MED_ID = "extra_med_id"
        const val EXTRA_MED_NAME = "extra_med_name"
        const val EXTRA_MEMBER_ID = "extra_member_id"
        const val EXTRA_MEMBER_NAME = "extra_member_name"
        const val EXTRA_IS_CRITICAL = "extra_is_critical"
        const val EXTRA_SOUND_TYPE = "extra_sound_type"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_SCHEDULED_AT = "extra_scheduled_at"
        const val EXTRA_SCHEDULE_ENTRY_ID = "extra_schedule_entry_id"
        const val EXTRA_TTS_TEMPLATE = "extra_tts_template"
        const val EXTRA_ESCALATION_LEVEL = "extra_escalation_level"

        // Global StateFlow to track active alarm in UI (e.g., MainActivity sticky banner)
        private val _currentActiveAlarm = MutableStateFlow<ActiveAlarmState?>(null)
        val currentActiveAlarm: StateFlow<ActiveAlarmState?> = _currentActiveAlarm.asStateFlow()

        fun startAlarm(
            context: Context,
            medicationId: String,
            medicationName: String,
            familyMemberId: String,
            familyMemberName: String,
            isCritical: Boolean,
            soundType: String,
            soundUri: String?,
            scheduledAt: Long,
            scheduleEntryId: String?,
            ttsTemplate: String = "DEFAULT",
            escalationLevel: Int = 0
        ) {
            val intent = Intent(context, AlarmSoundService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_MED_ID, medicationId)
                putExtra(EXTRA_MED_NAME, medicationName)
                putExtra(EXTRA_MEMBER_ID, familyMemberId)
                putExtra(EXTRA_MEMBER_NAME, familyMemberName)
                putExtra(EXTRA_IS_CRITICAL, isCritical)
                putExtra(EXTRA_SOUND_TYPE, soundType)
                putExtra(EXTRA_SOUND_URI, soundUri)
                putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
                putExtra(EXTRA_SCHEDULE_ENTRY_ID, scheduleEntryId)
                putExtra(EXTRA_TTS_TEMPLATE, ttsTemplate)
                putExtra(EXTRA_ESCALATION_LEVEL, escalationLevel)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAlarm(context: Context) {
            _currentActiveAlarm.value = null
            try {
                val intent = Intent(context, AlarmSoundService::class.java).apply {
                    action = ACTION_STOP_ALARM
                }
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("AlarmSoundService", "Error stopping AlarmSoundService", e)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopPlaybackAndService()
            return START_NOT_STICKY
        }

        if (action == ACTION_START_ALARM) {
            medicationId = intent.getStringExtra(EXTRA_MED_ID) ?: ""
            medicationName = intent.getStringExtra(EXTRA_MED_NAME) ?: "الدواء"
            familyMemberId = intent.getStringExtra(EXTRA_MEMBER_ID) ?: ""
            familyMemberName = intent.getStringExtra(EXTRA_MEMBER_NAME) ?: "العائلة"
            isCritical = intent.getBooleanExtra(EXTRA_IS_CRITICAL, false)
            soundType = intent.getStringExtra(EXTRA_SOUND_TYPE) ?: "TONE"
            soundUri = intent.getStringExtra(EXTRA_SOUND_URI)
            scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, System.currentTimeMillis())
            scheduleEntryId = intent.getStringExtra(EXTRA_SCHEDULE_ENTRY_ID)
            ttsTemplate = intent.getStringExtra(EXTRA_TTS_TEMPLATE) ?: "DEFAULT"
            escalationLevel = intent.getIntExtra(EXTRA_ESCALATION_LEVEL, 0)

            _currentActiveAlarm.value = ActiveAlarmState(
                medicationId = medicationId,
                medicationName = medicationName,
                memberName = familyMemberName,
                memberId = familyMemberId,
                isCritical = isCritical,
                scheduledAt = scheduledAt,
                scheduleEntryId = scheduleEntryId,
                escalationLevel = escalationLevel,
                isRinging = true
            )

            // Requirement 2: Acquire WAKE_LOCK only for the ringing duration
            acquireWakeLock()

            val notification = buildForegroundNotification()
            startForeground(NOTIFICATION_ID, notification)

            startAudioPlayback()
            startVibration()
            startSafetyTimeoutTimer()
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "Ameen::AlarmRingingWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.acquire(AlarmConfig.RINGING_SAFETY_TIMEOUT_MS)
        } catch (e: Exception) {
            android.util.Log.e("AlarmSoundService", "Error acquiring WakeLock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
        wakeLock = null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.channel_playback_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.channel_playback_desc)
                setSound(null, null) // Audio handled by service MediaPlayer
                enableVibration(false) // Handled explicitly via Vibrator for exact amplitude control
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        // Take action ("أخدت")
        val takeIntent = Intent(this, MedicationActionReceiver::class.java).apply {
            action = AlarmOrchestrator.ACTION_TAKE_DOSE
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID, scheduleEntryId)
            putExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID, familyMemberId)
            putExtra("notificationId", NOTIFICATION_ID)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            this,
            201,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze action ("أجّل")
        val snoozeIntent = Intent(this, MedicationActionReceiver::class.java).apply {
            action = AlarmOrchestrator.ACTION_SNOOZE_15
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID, scheduleEntryId)
            putExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID, familyMemberId)
            putExtra(MedicationAlarmReceiver.EXTRA_IS_CRITICAL, isCritical)
            putExtra("notificationId", NOTIFICATION_ID)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this,
            202,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Skip / Dismiss action ("تخطّى")
        val dismissIntent = Intent(this, MedicationActionReceiver::class.java).apply {
            action = AlarmOrchestrator.ACTION_DISMISS
            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra("baseRequestCode", 1000)
            putExtra("notificationId", NOTIFICATION_ID)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this,
            204,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full Screen Activity Intent
        val fullScreenIntent = Intent(this, AlarmFullScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_MED_ID, medicationId)
            putExtra(EXTRA_MED_NAME, medicationName)
            putExtra(EXTRA_MEMBER_NAME, familyMemberName)
            putExtra(EXTRA_MEMBER_ID, familyMemberId)
            putExtra(EXTRA_IS_CRITICAL, isCritical)
            putExtra(EXTRA_SCHEDULED_AT, scheduledAt)
            putExtra(EXTRA_SCHEDULE_ENTRY_ID, scheduleEntryId)
            putExtra(EXTRA_ESCALATION_LEVEL, escalationLevel)
        }
        val fullScreenPending = PendingIntent.getActivity(
            this,
            203,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (escalationLevel) {
            1 -> getString(R.string.alarm_private_title_reminder, medicationName, familyMemberName)
            2 -> getString(R.string.alarm_private_title_critical, medicationName, familyMemberName)
            else -> getString(R.string.alarm_private_title_primary, familyMemberName, medicationName)
        }
        val text = "يرجى تناول الدواء لـ ($familyMemberName) في موعده المحدد."

        // Requirement 4: Check NotificationManager.canUseFullScreenIntent() on API 34+
        val canUseFullScreen = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.canUseFullScreenIntent() ?: true
        } else {
            true
        }

        // Requirement 5: Privacy lock-screen notification
        val publicNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(getString(R.string.alarm_public_title))
            .setContentText(getString(R.string.alarm_public_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(fullScreenPending)
            .build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setOngoing(false) // Allow swipe dismiss to cancel cleanly
            .setAutoCancel(true)
            .setContentIntent(fullScreenPending)
            .setDeleteIntent(dismissPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, getString(R.string.action_take_dose), takePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, getString(R.string.action_snooze_15), snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.action_skip_dose), dismissPendingIntent)

        if (canUseFullScreen) {
            builder.setFullScreenIntent(fullScreenPending, true)
        }

        return builder.build()
    }

    private fun startAudioPlayback() {
        val tierProfile = AlarmConfig.getTierProfile(escalationLevel)
        when (soundType) {
            "SILENT" -> {}
            "VIBRATE" -> {}
            "TTS" -> {
                textToSpeech = TextToSpeech(this, this)
            }
            "CUSTOM_FILE" -> {
                playCustomAudioFile(tierProfile.volume)
            }
            "RECORDED" -> {
                playRecordedVoice(tierProfile.volume)
            }
            else -> {
                playSystemAlarmTone(tierProfile.volume)
            }
        }
    }

    private fun startVibration() {
        val tierProfile = AlarmConfig.getTierProfile(escalationLevel)
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    tierProfile.vibrationTimings,
                    tierProfile.vibrationAmplitudes,
                    tierProfile.repeatIndex
                )
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(tierProfile.vibrationTimings, tierProfile.repeatIndex)
            }
        } catch (e: Exception) {
            android.util.Log.e("AlarmSoundService", "Error starting vibration", e)
        }
    }

    private fun playSystemAlarmTone(volume: Float) {
        try {
            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(this@AlarmSoundService, alarmUri!!)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playCustomAudioFile(volume: Float) {
        if (soundUri.isNullOrBlank()) {
            playSystemAlarmTone(volume)
            return
        }
        try {
            val uri = Uri.parse(soundUri)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(this@AlarmSoundService, uri)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSystemAlarmTone(volume)
        }
    }

    private fun playRecordedVoice(volume: Float) {
        if (soundUri.isNullOrBlank()) {
            playSystemAlarmTone(volume)
            return
        }
        try {
            val file = File(soundUri!!)
            if (!file.exists()) {
                playSystemAlarmTone(volume)
                return
            }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build()
                )
                setDataSource(file.absolutePath)
                isLooping = true
                setVolume(volume, volume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            playSystemAlarmTone(volume)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("ar"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.stop()
                textToSpeech?.shutdown()
                textToSpeech = null
                val tierProfile = AlarmConfig.getTierProfile(escalationLevel)
                playSystemAlarmTone(tierProfile.volume)
            } else {
                isTtsReady = true
                startTtsSpeechLoop()
            }
        } else {
            val tierProfile = AlarmConfig.getTierProfile(escalationLevel)
            playSystemAlarmTone(tierProfile.volume)
        }
    }

    private fun startTtsSpeechLoop() {
        val speechText = when (ttsTemplate) {
            "REMINDER" -> "تذكير طبي: يرجى تناول دواء $medicationName في موعده، شفاك الله وعافاك"
            "WATER" -> "فضلاً تذكر أخذ دواء $medicationName لـ $familyMemberName مع كوب من الماء"
            "SHORT" -> "موعد دواء $medicationName"
            else -> "حان الآن موعد دواء $medicationName لـ $familyMemberName"
        }
        ttsLoopJob?.cancel()
        ttsLoopJob = serviceScope.launch {
            while (isActive) {
                textToSpeech?.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "AmeenTtsDoseId")
                delay(6000)
            }
        }
    }

    private fun startSafetyTimeoutTimer() {
        safetyTimeoutJob?.cancel()
        safetyTimeoutJob = serviceScope.launch {
            delay(AlarmConfig.RINGING_SAFETY_TIMEOUT_MS)
            try {
                val db = AmeenDatabase.getDatabase(applicationContext, serviceScope)
                val repo = MedicationRepository(db)
                repo.recordDoseMissed(
                    medicationId = medicationId,
                    familyMemberId = familyMemberId,
                    scheduleEntryId = scheduleEntryId,
                    scheduledAt = scheduledAt,
                    notes = "فات موعد الجرعة تلقائياً بعد رنين المنبه لمدة دقيقتين دون استجابة"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            stopPlaybackAndService()
        }
    }

    private fun stopPlaybackAndService() {
        _currentActiveAlarm.value = null
        safetyTimeoutJob?.cancel()
        ttsLoopJob?.cancel()

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null

        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
        textToSpeech = null

        try {
            vibrator?.cancel()
        } catch (_: Exception) {}
        vibrator = null

        releaseWakeLock()

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        _currentActiveAlarm.value = null
        serviceScope.cancel()
        stopPlaybackAndService()
        super.onDestroy()
    }
}
