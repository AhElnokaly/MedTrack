package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.AlarmOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_MEDICATION_ID = "medicationId"
        const val EXTRA_MEDICATION_NAME = "medicationName"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_TIMING_RULE = "timingRule"
        const val EXTRA_MEMBER_NAME = "memberName"
        const val EXTRA_FAMILY_MEMBER_ID = "familyMemberId"
        const val EXTRA_IS_CRITICAL = "isCritical"
        const val EXTRA_SCHEDULE_ENTRY_ID = "scheduleEntryId"
        const val EXTRA_BASE_REQUEST_CODE = "baseRequestCode"
        const val EXTRA_ESCALATION_LEVEL = "escalationLevel"
        const val EXTRA_SCHEDULED_AT = "scheduledAt"
        const val EXTRA_SOUND_TYPE = "soundType"
        const val EXTRA_SOUND_URI = "soundUri"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "الدواء"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val timingRule = intent.getStringExtra(EXTRA_TIMING_RULE) ?: ""
        val memberName = intent.getStringExtra(EXTRA_MEMBER_NAME) ?: "المريض"
        val familyMemberId = intent.getStringExtra(EXTRA_FAMILY_MEMBER_ID) ?: ""
        val isCritical = intent.getBooleanExtra(EXTRA_IS_CRITICAL, false)
        val scheduleEntryId = intent.getStringExtra(EXTRA_SCHEDULE_ENTRY_ID)
        val baseRequestCode = intent.getIntExtra(EXTRA_BASE_REQUEST_CODE, 1000)
        val escalationLevel = intent.getIntExtra(EXTRA_ESCALATION_LEVEL, 0)
        val scheduledAt = intent.getLongExtra(EXTRA_SCHEDULED_AT, System.currentTimeMillis())
        val soundType = intent.getStringExtra(EXTRA_SOUND_TYPE) ?: "TONE"
        val soundUri = intent.getStringExtra(EXTRA_SOUND_URI)
        val durationDays = intent.getIntExtra("durationDays", -1)
        val createdAt = intent.getLongExtra("createdAt", 0L)
        val ttsTemplate = intent.getStringExtra("ttsTemplate") ?: "DEFAULT"
        val recurrencePattern = intent.getStringExtra("recurrencePattern") ?: "DAILY"
        val recurrenceDays = intent.getStringExtra("recurrenceDays") ?: ""

        // Auto-stop notifications if temporary course duration has expired
        if (durationDays > 0 && createdAt > 0) {
            val expirationMillis = createdAt + (durationDays * 86_400_000L)
            if (System.currentTimeMillis() >= expirationMillis) {
                return
            }
        }

        // Check specific day of week if configured
        if (recurrencePattern == "SPECIFIC_DAYS" && recurrenceDays.isNotBlank()) {
            val dayNames = mapOf(
                java.util.Calendar.SATURDAY to "السبت",
                java.util.Calendar.SUNDAY to "الأحد",
                java.util.Calendar.MONDAY to "الإثنين",
                java.util.Calendar.TUESDAY to "الثلاثاء",
                java.util.Calendar.WEDNESDAY to "الأربعاء",
                java.util.Calendar.THURSDAY to "الخميس",
                java.util.Calendar.FRIDAY to "الجمعة"
            )
            val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
            val todayName = dayNames[today]
            val activeDays = recurrenceDays.split(",").map { it.trim() }
            if (todayName != null && !activeDays.contains(todayName)) {
                // Today is not a scheduled day for this medication
                return
            }
        }

        // 1. Show standard / heads-up notification
        AlarmOrchestrator.showMedicationNotification(
            context = context,
            medicationId = medicationId,
            medicationName = medicationName,
            dosage = dosage,
            timingRule = timingRule,
            memberName = memberName,
            familyMemberId = familyMemberId,
            isCritical = isCritical,
            scheduleEntryId = scheduleEntryId,
            baseRequestCode = baseRequestCode,
            escalationLevel = escalationLevel,
            scheduledAt = scheduledAt
        )

        // 2. Start Real Looping Audio Alarm Service (USAGE_ALARM foreground playback) unless Silent mode
        if (soundType != "SILENT") {
            try {
                com.example.alarm.AlarmSoundService.startAlarm(
                    context = context,
                    medicationId = medicationId,
                    medicationName = medicationName,
                    familyMemberId = familyMemberId,
                    familyMemberName = memberName,
                    isCritical = isCritical,
                    soundType = soundType,
                    soundUri = soundUri,
                    scheduledAt = scheduledAt,
                    scheduleEntryId = scheduleEntryId,
                    ttsTemplate = ttsTemplate
                )
            } catch (e: Exception) {
                android.util.Log.e("MedAlarmReceiver", "Failed to start AlarmSoundService", e)
            }
        }

        // If at final escalation level, trigger missed dose verification via goAsync
        val isFinalEscalation = if (isCritical) escalationLevel >= 2 else escalationLevel >= 1
        if (isFinalEscalation) {
            val pendingResult = goAsync()
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                try {
                    val db = com.example.data.db.AmeenDatabase.getDatabase(context, scope)
                    val repo = com.example.data.repository.MedicationRepository(db)
                    repo.checkAndMarkMissedDoses()
                } catch (e: Exception) {
                    android.util.Log.e("MedAlarmReceiver", "Error checking missed doses", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
