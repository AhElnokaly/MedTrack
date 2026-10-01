package com.example.alarm

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aistudio.ameen.medication.R
import com.example.MainActivity
import com.example.data.model.Medication
import com.example.data.model.ScheduleEntry
import com.example.receiver.MedicationActionReceiver
import com.example.receiver.MedicationAlarmReceiver
import java.util.Calendar

object AlarmOrchestrator {
    const val CHANNEL_PRIMARY_ID = "ameen_medication_primary"
    const val CHANNEL_CRITICAL_ID = "ameen_medication_critical"

    // Canonical action identifiers
    const val ACTION_TAKE_DOSE = "com.example.ameen.ACTION_TAKE_DOSE"
    const val ACTION_SNOOZE_15 = "com.example.ameen.ACTION_SNOOZE_15"
    const val ACTION_DISMISS = "com.example.ameen.ACTION_DISMISS"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val primaryChannel = NotificationChannel(
                CHANNEL_PRIMARY_ID,
                context.getString(R.string.channel_primary_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_primary_desc)
                enableVibration(true)
                vibrationPattern = AlarmConfig.TIER_0_PRIMARY.vibrationTimings
            }

            val criticalChannel = NotificationChannel(
                CHANNEL_CRITICAL_ID,
                context.getString(R.string.channel_critical_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.channel_critical_desc)
                enableVibration(true)
                vibrationPattern = AlarmConfig.TIER_2_CRITICAL.vibrationTimings
            }

            notificationManager.createNotificationChannel(primaryChannel)
            notificationManager.createNotificationChannel(criticalChannel)
        }
    }

    /**
     * Unique requestCode calculation to avoid collision:
     * level 0: primary (T+0)
     * level 1: +15 min reminder
     * level 2: +30 min critical reminder
     */
    fun getRequestCode(baseCode: Int, escalationLevel: Int): Int {
        return AlarmConfig.getRequestCode(baseCode, escalationLevel)
    }

    fun scheduleMedicationAlarm(
        context: Context,
        medication: Medication,
        schedule: ScheduleEntry,
        memberName: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Auto-stop notifications if temporary course duration has ended
        if (medication.durationDays != null) {
            val expirationMillis = medication.createdAt + (medication.durationDays * 86_400_000L)
            if (System.currentTimeMillis() >= expirationMillis) {
                return
            }
        }

        val parts = schedule.timeOfDay.split(":")
        if (parts.size != 2) return
        val hour = parts[0].toIntOrNull() ?: 8
        val minute = parts[1].toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val primaryTime = calendar.timeInMillis

        // 1. Primary Alarm (T+0) via AlarmManager.setAlarmClock()
        scheduleSingleAlarm(
            context = context,
            alarmManager = alarmManager,
            triggerAtMillis = primaryTime,
            scheduledAt = primaryTime,
            medication = medication,
            schedule = schedule,
            memberName = memberName,
            escalationLevel = 0
        )

        // 2. Escalation Level 1: T + 15 min
        val level1Time = primaryTime + AlarmConfig.TIER_1_ESCALATION_DELAY_MS
        scheduleSingleAlarm(
            context = context,
            alarmManager = alarmManager,
            triggerAtMillis = level1Time,
            scheduledAt = primaryTime,
            medication = medication,
            schedule = schedule,
            memberName = memberName,
            escalationLevel = 1
        )

        // 3. Escalation Level 2: T + 30 min (Critical medications only, e.g. blood pressure, diabetes)
        if (medication.isCritical) {
            val level2Time = primaryTime + AlarmConfig.TIER_2_ESCALATION_DELAY_MS
            scheduleSingleAlarm(
                context = context,
                alarmManager = alarmManager,
                triggerAtMillis = level2Time,
                scheduledAt = primaryTime,
                medication = medication,
                schedule = schedule,
                memberName = memberName,
                escalationLevel = 2
            )
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        scheduledAt: Long,
        medication: Medication,
        schedule: ScheduleEntry,
        memberName: String,
        escalationLevel: Int
    ) {
        val requestCode = getRequestCode(schedule.alarmRequestCodeBase, escalationLevel)
        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("medicationId", medication.id)
            putExtra("medicationName", medication.brandName)
            putExtra("dosage", "${medication.dosageAmount} ${medication.dosageUnit}")
            putExtra("timingRule", medication.timingRule)
            putExtra("memberName", memberName)
            putExtra("familyMemberId", medication.familyMemberId)
            putExtra("isCritical", medication.isCritical)
            putExtra("soundType", medication.soundType)
            putExtra("soundUri", medication.soundUri)
            putExtra("durationDays", medication.durationDays ?: -1)
            putExtra("createdAt", medication.createdAt)
            putExtra("ttsTemplate", medication.ttsTemplate)
            putExtra("recurrencePattern", medication.recurrencePattern)
            putExtra("recurrenceDays", medication.recurrenceDays)
            putExtra("scheduleEntryId", schedule.id)
            putExtra("baseRequestCode", schedule.alarmRequestCodeBase)
            putExtra("escalationLevel", escalationLevel)
            putExtra("scheduledAt", scheduledAt)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Show intent for system alarm clock status / lock screen tap
        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Requirement 1: Use AlarmManager.setAlarmClock() for highest reliability across all OEMs
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } catch (_: Exception) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        }
    }

    /**
     * Requirement 8: Helper to schedule a real test alarm in 10 seconds for user self-testing.
     */
    fun scheduleTestAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = System.currentTimeMillis() + AlarmConfig.TEST_ALARM_DELAY_MS

        val intent = Intent(context, MedicationAlarmReceiver::class.java).apply {
            putExtra("medicationId", AlarmConfig.TEST_ALARM_MED_ID)
            putExtra("medicationName", "دواء تجريبي (فحص الجاهزية)")
            putExtra("dosage", "1 قرص")
            putExtra("timingRule", "مع كوب ماء")
            putExtra("memberName", "المستخدم")
            putExtra("familyMemberId", "self")
            putExtra("isCritical", true)
            putExtra("soundType", "TONE")
            putExtra("baseRequestCode", AlarmConfig.TEST_ALARM_BASE_REQUEST_CODE)
            putExtra("escalationLevel", 0)
            putExtra("scheduledAt", triggerTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            AlarmConfig.TEST_ALARM_BASE_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            AlarmConfig.TEST_ALARM_BASE_REQUEST_CODE,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    fun cancelAllAlarmsForSchedule(
        context: Context,
        scheduleBaseCode: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (level in 0..2) {
            val requestCode = getRequestCode(scheduleBaseCode, level)
            val intent = Intent(context, MedicationAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    /**
     * Requirement 3, 5 & 6:
     * - Category CATEGORY_ALARM, Channel IMPORTANCE_HIGH
     * - Privacy: VISIBILITY_PRIVATE with setPublicVersion() displaying generic Arabic text
     * - Canonical Action Buttons: "أخدت" / "أجّل" / "تخطّى"
     */
    fun showMedicationNotification(
        context: Context,
        medicationId: String,
        medicationName: String,
        dosage: String,
        timingRule: String,
        memberName: String,
        familyMemberId: String,
        isCritical: Boolean,
        scheduleEntryId: String?,
        baseRequestCode: Int,
        escalationLevel: Int,
        scheduledAt: Long = System.currentTimeMillis()
    ) {
        createNotificationChannels(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (isCritical) CHANNEL_CRITICAL_ID else CHANNEL_PRIMARY_ID

        // Title and content depending on escalation
        val title = when (escalationLevel) {
            1 -> context.getString(R.string.alarm_private_title_reminder, medicationName, memberName)
            2 -> context.getString(R.string.alarm_private_title_critical, medicationName, memberName)
            else -> context.getString(R.string.alarm_private_title_primary, memberName, medicationName)
        }
        val content = context.getString(R.string.alarm_private_content, dosage, timingRule)

        // Main Tap Intent: opens app
        val contentIntent = PendingIntent.getActivity(
            context,
            baseRequestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "أخدت"
        val takeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = ACTION_TAKE_DOSE
            putExtra("medicationId", medicationId)
            putExtra("familyMemberId", familyMemberId)
            putExtra("scheduleEntryId", scheduleEntryId)
            putExtra("baseRequestCode", baseRequestCode)
            putExtra("notificationId", baseRequestCode)
            putExtra("scheduledAt", scheduledAt)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context,
            baseRequestCode + 100,
            takeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "أجّل"
        val snoozeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_15
            putExtra("medicationId", medicationId)
            putExtra("familyMemberId", familyMemberId)
            putExtra("scheduleEntryId", scheduleEntryId)
            putExtra("baseRequestCode", baseRequestCode)
            putExtra("notificationId", baseRequestCode)
            putExtra("isCritical", isCritical)
            putExtra("scheduledAt", scheduledAt)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            baseRequestCode + 200,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action "تخطّى" (إلغاء التنبيه وحفظ الحالة)
        val dismissIntent = Intent(context, MedicationActionReceiver::class.java).apply {
            action = ACTION_DISMISS
            putExtra("medicationId", medicationId)
            putExtra("familyMemberId", familyMemberId)
            putExtra("scheduleEntryId", scheduleEntryId)
            putExtra("baseRequestCode", baseRequestCode)
            putExtra("notificationId", baseRequestCode)
            putExtra("scheduledAt", scheduledAt)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            baseRequestCode + 300,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Requirement 5: Public version for lock-screen privacy
        val publicNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(context.getString(R.string.alarm_public_title))
            .setContentText(context.getString(R.string.alarm_public_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .build()

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicNotification)
            .setDeleteIntent(dismissPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, context.getString(R.string.action_take_dose), takePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, context.getString(R.string.action_snooze_15), snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.action_skip_dose), dismissPendingIntent)
            .build()

        notificationManager.notify(baseRequestCode, notification)
    }

    /**
     * Urgent stock threshold notification
     */
    fun sendUrgentStockNotification(
        context: Context,
        medicationName: String,
        memberName: String,
        daysRemaining: Int,
        currentStock: Int
    ) {
        createNotificationChannels(context)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = if (daysRemaining <= 2) "🚨 تحذير نفاد وشيك جداً (يومان أو أقل)!" else "⚠️ تنبيه انخفاض مخزون الدواء"
        val text = if (daysRemaining <= 2) {
            "دواء $medicationName لـ $memberName متبقي منه $currentStock جرعات فقط (يكفي $daysRemaining يوم أو أقل). تم إدراجه كبند عاجل في قائمة الشراء."
        } else {
            "دواء $medicationName لـ $memberName متبقي منه $currentStock جرعات (يكفي $daysRemaining أيام). يرجى التجهيز لشرائه."
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9001 + daysRemaining,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (daysRemaining <= 2) CHANNEL_CRITICAL_ID else CHANNEL_PRIMARY_ID
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(if (daysRemaining <= 2) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(7000 + (medicationName.hashCode().mod(1000)), notification)
    }
}
