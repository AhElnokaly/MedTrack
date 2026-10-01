package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MedicationRepository(
    private val database: AmeenDatabase
) {
    private val memberDao = database.familyMemberDao()
    private val medDao = database.medicationDao()
    private val schedDao = database.scheduleDao()
    private val logDao = database.doseLogDao()
    private val shopDao = database.shoppingListDao()
    private val presDao = database.prescriptionDao()

    // Members
    val allMembers: Flow<List<FamilyMember>> = memberDao.getAllMembers()
    fun getMember(id: String): Flow<FamilyMember?> = memberDao.getMemberById(id)
    suspend fun insertMember(member: FamilyMember) = memberDao.insertMember(member)
    suspend fun updateMember(member: FamilyMember) = memberDao.updateMember(member)
    suspend fun deleteMember(member: FamilyMember) = memberDao.deleteMember(member)

    // Medications
    val allMedications: Flow<List<Medication>> = medDao.getAllMedications()
    fun getMedicationsByMember(memberId: String): Flow<List<Medication>> =
        medDao.getMedicationsByMember(memberId)
    fun getMedication(id: String): Flow<Medication?> = medDao.getMedicationById(id)

    suspend fun saveMedicationWithSchedules(
        medication: Medication,
        schedules: List<ScheduleEntry>
    ) {
        medDao.insertMedication(medication)
        schedDao.deleteSchedulesForMedication(medication.id)
        schedDao.insertSchedules(schedules)
    }

    suspend fun deleteMedication(medication: Medication) {
        schedDao.deleteSchedulesForMedication(medication.id)
        medDao.deleteMedication(medication)
    }

    // Schedules
    val activeSchedules: Flow<List<ScheduleEntry>> = schedDao.getAllActiveSchedules()
    fun getSchedulesForMedication(medicationId: String): Flow<List<ScheduleEntry>> =
        schedDao.getSchedulesForMedication(medicationId)

    // Dose logs & Automatic Stock Deduction
    val allLogs: Flow<List<DoseLog>> = logDao.getAllLogs()
    fun getLogsForMember(memberId: String): Flow<List<DoseLog>> = logDao.getLogsForMember(memberId)

    /**
     * Core business logic:
     * 1. Register DoseLog with taken timestamp
     * 2. Decrement current stock by 1
     * 3. If stock <= 5 days (dailyDoseCount * 5) and autoAddToShoppingList is true, add to shopping list
     */
    suspend fun recordDoseTaken(
        medicationId: String,
        familyMemberId: String,
        scheduleEntryId: String? = null,
        scheduledAt: Long = System.currentTimeMillis(),
        isDelayed: Boolean = false,
        notes: String? = null,
        context: android.content.Context? = null
    ) {
        val now = System.currentTimeMillis()
        val effectiveScheduledAt = if (scheduledAt > 0) scheduledAt else now
        // Determine late: user explicit flag OR actual taken time is > 30 minutes past scheduled time
        val isLate = isDelayed || (now - effectiveScheduledAt > 30 * 60 * 1000L)
        val log = DoseLog(
            id = UUID.randomUUID().toString(),
            medicationId = medicationId,
            familyMemberId = familyMemberId,
            scheduleEntryId = scheduleEntryId,
            scheduledAt = effectiveScheduledAt,
            takenAt = now,
            status = if (isLate) "LATE" else "TAKEN",
            isDelayedConfirmed = isLate,
            notes = notes
        )
        logDao.insertLog(log)

        // Decrement stock
        val med = medDao.getMedicationByIdSync(medicationId)
        if (med != null && med.currentStock > 0) {
            val updatedStock = (med.currentStock - 1).coerceAtLeast(0)
            medDao.updateStock(medicationId, updatedStock)

            // Distinct low stock thresholds:
            // Threshold 1: <= 2 days (CRITICAL_LOW_STOCK) - High urgency, separate item, 2 packs
            // Threshold 2: 3..5 days (LOW_STOCK) - Standard reminder, 1 pack
            val dailyRate = if (med.dailyDoseCount > 0) med.dailyDoseCount else 1
            val daysRemaining = updatedStock / dailyRate
            if (daysRemaining <= 5 && med.autoAddToShoppingList) {
                val member = memberDao.getMemberByIdSync(med.familyMemberId)
                val memberName = member?.name ?: "العائلة"
                val urgencyReason = if (daysRemaining <= 2) "CRITICAL_LOW_STOCK" else "LOW_STOCK"
                shopDao.insertItem(
                    ShoppingListItem(
                        id = UUID.randomUUID().toString(),
                        medicationId = med.id,
                        medicationName = "${med.brandName} (${med.dosageAmount} ${med.dosageUnit})",
                        familyMemberName = memberName,
                        neededQuantity = if (daysRemaining <= 2) 2 else 1,
                        addedReason = urgencyReason,
                        isPurchased = false
                    )
                )

                context?.let { ctx ->
                    com.example.alarm.AlarmOrchestrator.sendUrgentStockNotification(
                        context = ctx,
                        medicationName = med.brandName,
                        memberName = memberName,
                        daysRemaining = daysRemaining,
                        currentStock = updatedStock
                    )
                }
            }
        }
    }

    suspend fun recordDoseMissed(
        medicationId: String,
        familyMemberId: String,
        scheduleEntryId: String? = null,
        scheduledAt: Long,
        notes: String? = "فات موعد الجرعة دون تأكيد أخذها"
    ) {
        val log = DoseLog(
            id = UUID.randomUUID().toString(),
            medicationId = medicationId,
            familyMemberId = familyMemberId,
            scheduleEntryId = scheduleEntryId,
            scheduledAt = scheduledAt,
            takenAt = null,
            status = "MISSED",
            isDelayedConfirmed = false,
            notes = notes
        )
        logDao.insertLog(log)
    }

    suspend fun checkAndMarkMissedDoses() {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L - 1

        val allMeds = medDao.getAllMedicationsSync()
        for (med in allMeds) {
            val schedules = schedDao.getSchedulesForMedicationSync(med.id)
            for (sched in schedules) {
                if (!sched.enabled) continue
                val parts = sched.timeOfDay.split(":")
                if (parts.size != 2) continue
                val hour = parts[0].toIntOrNull() ?: continue
                val minute = parts[1].toIntOrNull() ?: continue
                val schedCal = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, hour)
                    set(java.util.Calendar.MINUTE, minute)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }
                val scheduledTime = schedCal.timeInMillis
                // If scheduled time was more than 1 hour ago and is within today
                if (now - scheduledTime > 60 * 60 * 1000L && scheduledTime >= startOfDay) {
                    val existingLog = logDao.getTodayLogForSchedule(sched.id, startOfDay, endOfDay)
                    if (existingLog == null) {
                        recordDoseMissed(
                            medicationId = med.id,
                            familyMemberId = med.familyMemberId,
                            scheduleEntryId = sched.id,
                            scheduledAt = scheduledTime,
                            notes = "فاتت الجرعة المجدولة (${sched.timeOfDay}) دون تسجيل أخذها"
                        )
                    }
                }
            }
        }
    }

    suspend fun updateStock(medicationId: String, newStock: Int) {
        medDao.updateStock(medicationId, newStock.coerceAtLeast(0))
    }

    // Shopping List
    val shoppingList: Flow<List<ShoppingListItem>> = shopDao.getShoppingList()
    suspend fun addShoppingItem(item: ShoppingListItem) = shopDao.insertItem(item)
    suspend fun updateShoppingItem(item: ShoppingListItem) = shopDao.updateItem(item)
    suspend fun deleteShoppingItem(item: ShoppingListItem) = shopDao.deleteItem(item)

    // Prescriptions
    val allPrescriptions: Flow<List<Prescription>> = presDao.getAllPrescriptions()
    fun getPrescriptionsByMember(memberId: String): Flow<List<Prescription>> =
        presDao.getPrescriptionsForMember(memberId)
    suspend fun savePrescription(prescription: Prescription) = presDao.insertPrescription(prescription)
    suspend fun deletePrescription(prescription: Prescription) = presDao.deletePrescription(prescription)

    // Barcode Lookup
    suspend fun getMedicationByBarcode(barcode: String): Medication? =
        medDao.getMedicationByBarcode(barcode)

    // Clear all data
    suspend fun clearAllData() {
        database.clearAllTables()
    }

    companion object {
        private const val PREFS_NAME = "ameen_pharmacy_prefs"
        private const val KEY_PHARMACY_NAME = "pref_pharmacy_name"
        private const val KEY_PHARMACY_PHONE = "pref_pharmacy_phone"

        fun getPreferredPharmacyName(context: android.content.Context): String {
            val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            return sp.getString(KEY_PHARMACY_NAME, "") ?: ""
        }

        fun getPreferredPharmacyPhone(context: android.content.Context): String {
            val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            return sp.getString(KEY_PHARMACY_PHONE, "") ?: ""
        }

        fun savePreferredPharmacy(context: android.content.Context, name: String, phone: String) {
            val sp = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
            sp.edit().putString(KEY_PHARMACY_NAME, name).putString(KEY_PHARMACY_PHONE, phone).apply()
        }

        /**
         * Real business logic for calculating travel supply needed with a safety margin (2 extra doses)
         */
        fun calculateTravelSupply(medication: Medication, travelDays: Int): Int {
            val daily = if (medication.dailyDoseCount > 0) medication.dailyDoseCount else 1
            val base = travelDays * daily
            // Add safety buffer of 2 extra doses
            return base + 2
        }

        /**
         * Real business logic for remaining days of medication stock
         */
        fun calculateDaysRemaining(stock: Int, dailyDoseCount: Int): Int {
            if (stock <= 0) return 0
            val daily = if (dailyDoseCount > 0) dailyDoseCount else 1
            return stock / daily
        }

        /**
         * Real compliance rate percentage
         */
        fun calculateComplianceRate(totalLogs: Int, takenLogs: Int, lateLogs: Int): Int {
            if (totalLogs <= 0) return 100
            val effectiveTaken = takenLogs.toFloat() + (lateLogs.toFloat() * 0.8f)
            val rate = ((effectiveTaken / totalLogs.toFloat()) * 100f).toInt()
            return rate.coerceIn(0, 100)
        }

        /**
         * Real business logic for most missed medications ranking
         */
        fun calculateMostMissedMedications(
            medications: List<Medication>,
            logs: List<DoseLog>
        ): List<Pair<Medication, Int>> {
            val missedLogs = logs.filter { it.status == "MISSED" }
            val countByMedId = missedLogs.groupBy { it.medicationId }.mapValues { it.value.size }
            return medications
                .map { med -> Pair(med, countByMedId[med.id] ?: 0) }
                .filter { it.second > 0 }
                .sortedByDescending { it.second }
        }

        /**
         * Real renewal date calculation for prescription
         */
        fun calculatePrescriptionRenewalDate(prescription: Prescription, medication: Medication?): Long {
            if (prescription.renewalDate != null && prescription.renewalDate > 0) {
                return prescription.renewalDate
            }
            val days = prescription.durationDays ?: medication?.durationDays ?: 30
            return prescription.createdAt + (days.toLong() * 24L * 60 * 60 * 1000L)
        }

        /**
         * Checks if prescription renewal is due within 7 days
         */
        fun isPrescriptionRenewalDueSoon(
            prescription: Prescription,
            medication: Medication?,
            currentMillis: Long = System.currentTimeMillis()
        ): Boolean {
            val renewalDate = calculatePrescriptionRenewalDate(prescription, medication)
            val diffMillis = renewalDate - currentMillis
            val diffDays = diffMillis / (24L * 60 * 60 * 1000L)
            // Due within 7 days or recently expired up to 30 days
            return diffDays in -30..7
        }
    }
}
