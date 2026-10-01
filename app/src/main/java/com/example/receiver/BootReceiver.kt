package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.AlarmOrchestrator
import com.example.data.db.AmeenDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Requirement 1:
 * Automatically re-schedules all active medication alarms on device boot,
 * timezone changes, and manual time resets.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == "android.intent.action.TIME_SET"
        ) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            val database = AmeenDatabase.getDatabase(context, scope)
            val medDao = database.medicationDao()
            val schedDao = database.scheduleDao()
            val memberDao = database.familyMemberDao()

            scope.launch {
                try {
                    val activeSchedules = schedDao.getAllActiveSchedules().first()
                    val medications = medDao.getAllMedications().first().associateBy { it.id }
                    val members = memberDao.getAllMembers().first().associateBy { it.id }

                    for (schedule in activeSchedules) {
                        val med = medications[schedule.medicationId] ?: continue
                        val member = members[med.familyMemberId]
                        val memberName = member?.name ?: "العائلة"
                        AlarmOrchestrator.scheduleMedicationAlarm(
                            context = context,
                            medication = med,
                            schedule = schedule,
                            memberName = memberName
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BootReceiver", "Error re-scheduling alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
