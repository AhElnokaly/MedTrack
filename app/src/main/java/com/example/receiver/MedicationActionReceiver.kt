package com.example.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.aistudio.ameen.medication.R
import com.example.MainActivity
import com.example.alarm.AlarmConfig
import com.example.alarm.AlarmOrchestrator
import com.example.alarm.AlarmSoundService
import com.example.data.db.AmeenDatabase
import com.example.data.repository.MedicationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Requirement 6:
 * Actions: "أخدت" (ACTION_TAKE_DOSE), "أجّل" (ACTION_SNOOZE_15), "تخطّى" (ACTION_DISMISS).
 * Each writes to compliance log, cancels pending escalation tiers, and halts AlarmSoundService.
 */
class MedicationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra("notificationId", 1000)
        val baseRequestCode = intent.getIntExtra("baseRequestCode", 1000)
        val medicationId = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID)
            ?: intent.getStringExtra("medicationId")
            ?: ""
        val familyMemberId = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID)
            ?: intent.getStringExtra("familyMemberId")
            ?: ""
        val scheduleEntryId = intent.getStringExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID)
            ?: intent.getStringExtra("scheduleEntryId")
        val scheduledAt = intent.getLongExtra(
            MedicationAlarmReceiver.EXTRA_SCHEDULED_AT,
            intent.getLongExtra("scheduledAt", System.currentTimeMillis())
        )

        // 1. ALWAYS unconditionally halt looping alarm audio and release wake lock immediately
        try {
            AlarmSoundService.stopAlarm(context)
        } catch (_: Exception) {}

        // 2. Clear both the foreground alarm notification and regular notification
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            notificationManager.cancel(AlarmSoundService.NOTIFICATION_ID)
            if (baseRequestCode != 1000) {
                notificationManager.cancel(baseRequestCode)
            }
        } catch (_: Exception) {}

        when (intent.action) {
            AlarmOrchestrator.ACTION_TAKE_DOSE -> {
                // Cancel future escalations for this dose
                if (baseRequestCode != 1000) {
                    AlarmOrchestrator.cancelAllAlarmsForSchedule(context, baseRequestCode)
                }

                if (medicationId.isNotBlank() && medicationId != AlarmConfig.TEST_ALARM_MED_ID) {
                    val pendingResult = goAsync()
                    val scope = CoroutineScope(Dispatchers.IO)
                    val database = AmeenDatabase.getDatabase(context, scope)
                    val repository = MedicationRepository(database)

                    scope.launch {
                        try {
                            repository.recordDoseTaken(
                                medicationId = medicationId,
                                familyMemberId = familyMemberId,
                                scheduleEntryId = scheduleEntryId,
                                scheduledAt = scheduledAt,
                                isDelayed = false,
                                context = context
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("MedActionReceiver", "Error in recordDoseTaken", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
                Toast.makeText(context, context.getString(R.string.toast_dose_taken), Toast.LENGTH_SHORT).show()
            }

            AlarmOrchestrator.ACTION_SNOOZE_15 -> {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val snoozeTime = System.currentTimeMillis() + AlarmConfig.SNOOZE_DELAY_MS
                val alarmIntent = Intent(context, MedicationAlarmReceiver::class.java).apply {
                    putExtras(intent)
                    putExtra("escalationLevel", 1)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    baseRequestCode + 500,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val showIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val showPendingIntent = PendingIntent.getActivity(
                    context,
                    baseRequestCode + 500,
                    showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                try {
                    val clockInfo = AlarmManager.AlarmClockInfo(snoozeTime, showPendingIntent)
                    alarmManager.setAlarmClock(clockInfo, pendingIntent)
                } catch (_: SecurityException) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
                    }
                }

                Toast.makeText(context, context.getString(R.string.toast_dose_snoozed), Toast.LENGTH_SHORT).show()
            }

            AlarmOrchestrator.ACTION_DISMISS -> {
                // User chose "تخطّى" -> cancel remaining escalation tiers and record skipped in dose log
                if (baseRequestCode != 1000) {
                    AlarmOrchestrator.cancelAllAlarmsForSchedule(context, baseRequestCode)
                }

                if (medicationId.isNotBlank() && medicationId != AlarmConfig.TEST_ALARM_MED_ID) {
                    val pendingResult = goAsync()
                    val scope = CoroutineScope(Dispatchers.IO)
                    val database = AmeenDatabase.getDatabase(context, scope)
                    val repository = MedicationRepository(database)

                    scope.launch {
                        try {
                            repository.recordDoseMissed(
                                medicationId = medicationId,
                                familyMemberId = familyMemberId,
                                scheduleEntryId = scheduleEntryId,
                                scheduledAt = scheduledAt,
                                notes = "تم تخطي الجرعة يدوياً بواسطة المستخدم"
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("MedActionReceiver", "Error recording skipped dose", e)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
                Toast.makeText(context, context.getString(R.string.toast_dose_skipped), Toast.LENGTH_SHORT).show()
            }
        }
    }
}
