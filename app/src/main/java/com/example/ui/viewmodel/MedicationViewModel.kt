package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmOrchestrator
import com.example.data.db.AmeenDatabase
import com.example.data.model.*
import com.example.data.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class TodayAdherence(
    val totalDoses: Int = 0,
    val takenDoses: Int = 0,
    val percentage: Int = 0
)

class MedicationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicationRepository

    init {
        val database = AmeenDatabase.getDatabase(application, viewModelScope)
        repository = MedicationRepository(database)
        AlarmOrchestrator.createNotificationChannels(application)

        // Clear dummy data so the user can start with a clean slate and add data themselves
        val cleanupPrefs = application.getSharedPreferences("ameen_cleanup_prefs", Context.MODE_PRIVATE)
        if (!cleanupPrefs.getBoolean("dummy_data_cleared_user_request_v1", false)) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.clearAllData()
                application.getSharedPreferences("ameen_user_profile", Context.MODE_PRIVATE)
                    .edit().clear().apply()
                cleanupPrefs.edit().putBoolean("dummy_data_cleared_user_request_v1", true).apply()
            }
        }

        checkMissedDoses()
    }

    // State: Theme Mode (Dark Mode toggle persisted)
    private val _isDarkMode = MutableStateFlow(
        application.getSharedPreferences("ameen_settings", Context.MODE_PRIVATE)
            .getBoolean("dark_mode_enabled", false)
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        getApplication<Application>()
            .getSharedPreferences("ameen_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("dark_mode_enabled", newValue)
            .apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        getApplication<Application>()
            .getSharedPreferences("ameen_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("dark_mode_enabled", enabled)
            .apply()
    }

    // State: Members
    val allMembers: StateFlow<List<FamilyMember>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMemberId = MutableStateFlow<String?>(null)
    val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

    fun setSelectedMember(id: String?) {
        _selectedMemberId.value = id
    }

    // State: Medications
    val allMedications: StateFlow<List<Medication>> = repository.allMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredMedications: StateFlow<List<Medication>> = combine(
        allMedications,
        selectedMemberId
    ) { meds, memberId ->
        if (memberId == null) meds else meds.filter { it.familyMemberId == memberId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State: Schedules
    val activeSchedules: StateFlow<List<ScheduleEntry>> = repository.activeSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State: Logs
    val allLogs: StateFlow<List<DoseLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time Today's Adherence calculation (Progress Bar)
    val todayAdherence: StateFlow<TodayAdherence> = combine(
        allMedications,
        activeSchedules,
        allLogs,
        selectedMemberId
    ) { meds, schedules, logs, memberId ->
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 24 * 60 * 60 * 1000L - 1

        val relevantMeds = meds.filter { med ->
            !med.isPRN && (memberId == null || med.familyMemberId == memberId)
        }
        val relevantMedIds = relevantMeds.map { it.id }.toSet()

        var totalScheduled = 0
        for (med in relevantMeds) {
            val medSchedules = schedules.filter { it.medicationId == med.id && it.enabled }
            totalScheduled += if (medSchedules.isNotEmpty()) medSchedules.size else med.dailyDoseCount.coerceAtLeast(1)
        }

        val todayTakenLogs = logs.filter { log ->
            log.medicationId in relevantMedIds &&
            (log.status == "TAKEN" || log.status == "LATE") &&
            ((log.scheduledAt in startOfDay..endOfDay) || (log.takenAt != null && log.takenAt in startOfDay..endOfDay))
        }
        val takenCount = todayTakenLogs.size.coerceAtMost(totalScheduled.coerceAtLeast(todayTakenLogs.size))

        val percentage = if (totalScheduled == 0) {
            0
        } else {
            ((takenCount.toFloat() / totalScheduled.toFloat()) * 100).toInt().coerceIn(0, 100)
        }

        TodayAdherence(
            totalDoses = totalScheduled,
            takenDoses = takenCount,
            percentage = percentage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayAdherence(0, 0, 0))

    // State: Shopping List
    val shoppingList: StateFlow<List<ShoppingListItem>> = repository.shoppingList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State: Prescriptions
    val allPrescriptions: StateFlow<List<Prescription>> = repository.allPrescriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State: Today's Health Metric (Dynamic date based on LocalDate.now())
    private fun computeTodayDate(): String {
        return try {
            java.time.LocalDate.now().toString()
        } catch (_: Exception) {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        }
    }

    val todayDateString: String get() = computeTodayDate()

    /**
     * Records that a dose was taken:
     * - Decrements stock automatically
     * - Checks if stock <= 5 days (or <= 2 days critical) to add to shopping list
     * - Records DoseLog with real scheduledAt timestamp
     */
    fun takeDose(
        medicationId: String,
        familyMemberId: String,
        scheduleEntryId: String? = null,
        scheduledAt: Long = System.currentTimeMillis(),
        isDelayed: Boolean = false,
        notes: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordDoseTaken(
                medicationId = medicationId,
                familyMemberId = familyMemberId,
                scheduleEntryId = scheduleEntryId,
                scheduledAt = scheduledAt,
                isDelayed = isDelayed,
                notes = notes
            )
        }
    }

    fun markDoseMissed(
        medicationId: String,
        familyMemberId: String,
        scheduleEntryId: String? = null,
        scheduledAt: Long
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordDoseMissed(
                medicationId = medicationId,
                familyMemberId = familyMemberId,
                scheduleEntryId = scheduleEntryId,
                scheduledAt = scheduledAt
            )
        }
    }

    fun checkMissedDoses() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndMarkMissedDoses()
        }
    }

    /**
     * Add or update medication and schedule exact alarms
     */
    fun saveMedication(
        medication: Medication,
        timesOfDay: List<String>,
        context: Context
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = allMembers.value.find { it.id == medication.familyMemberId }
            val memberName = member?.name ?: "العائلة"

            // Cancel any old alarms if editing existing medication
            val currentSchedules = activeSchedules.value.filter { it.medicationId == medication.id }
            for (sched in currentSchedules) {
                AlarmOrchestrator.cancelAllAlarmsForSchedule(context, sched.alarmRequestCodeBase)
            }

            val schedules = timesOfDay.mapIndexed { index, time ->
                ScheduleEntry(
                    id = UUID.randomUUID().toString(),
                    medicationId = medication.id,
                    timeOfDay = time,
                    alarmRequestCodeBase = (1000 + (Math.random() * 8000).toInt() + index),
                    enabled = true
                )
            }

            repository.saveMedicationWithSchedules(medication, schedules)

            // Schedule alarms with AlarmOrchestrator
            for (schedule in schedules) {
                AlarmOrchestrator.scheduleMedicationAlarm(
                    context = context,
                    medication = medication,
                    schedule = schedule,
                    memberName = memberName
                )
            }
        }
    }

    fun deleteMedication(medication: Medication, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentSchedules = activeSchedules.value.filter { it.medicationId == medication.id }
            for (sched in currentSchedules) {
                AlarmOrchestrator.cancelAllAlarmsForSchedule(context, sched.alarmRequestCodeBase)
            }
            repository.deleteMedication(medication)
        }
    }

    fun updateStock(medicationId: String, newStock: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStock(medicationId, newStock)
        }
    }

    // Family Member Actions
    fun addFamilyMember(member: FamilyMember) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMember(member)
        }
    }

    fun updateFamilyMember(member: FamilyMember) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateMember(member)
        }
    }

    fun deleteFamilyMember(member: FamilyMember) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMember(member)
        }
    }

    // Shopping List Actions
    fun addShoppingItem(
        medicationName: String,
        memberName: String,
        neededQuantity: Int,
        preferredPharmacy: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = ShoppingListItem(
                medicationId = UUID.randomUUID().toString(),
                medicationName = medicationName,
                familyMemberName = memberName,
                neededQuantity = neededQuantity,
                addedReason = "MANUAL",
                isPurchased = false,
                preferredPharmacy = preferredPharmacy
            )
            repository.addShoppingItem(item)
        }
    }

    fun toggleShoppingItemPurchased(item: ShoppingListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShoppingItem(item.copy(isPurchased = !item.isPurchased))
        }
    }

    /**
     * Mark shopping item as purchased and simultaneously add purchased units to the medication stock
     */
    fun purchaseShoppingItemWithStock(item: ShoppingListItem, addedQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Mark item as purchased
            repository.updateShoppingItem(item.copy(isPurchased = true))

            // 2. Find matching medication by ID or name and update stock
            val matchingMed = allMedications.value.find { 
                it.id == item.medicationId || it.brandName.equals(item.medicationName, ignoreCase = true)
            }
            if (matchingMed != null) {
                val newStock = matchingMed.currentStock + addedQuantity
                repository.updateStock(matchingMed.id, newStock)
            }
        }
    }

    fun deleteShoppingItem(item: ShoppingListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShoppingItem(item)
        }
    }

    // Prescriptions
    fun addPrescription(prescription: Prescription) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.savePrescription(prescription)
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePrescription(prescription)
        }
    }

    /**
     * Travel Mode calculation:
     * Calculates needed dosage units for given duration in days, plus a 2-day safety buffer!
     */
    fun calculateTravelSupply(med: Medication, days: Int): Int {
        return MedicationRepository.calculateTravelSupply(med, days)
    }

    fun calculateDaysRemaining(stock: Int, dailyDoseCount: Int): Int {
        return MedicationRepository.calculateDaysRemaining(stock, dailyDoseCount)
    }

    fun calculateComplianceRate(total: Int, taken: Int, late: Int): Int {
        return MedicationRepository.calculateComplianceRate(total, taken, late)
    }

    suspend fun findMedicationByBarcode(barcode: String): Medication? {
        return repository.getMedicationByBarcode(barcode)
    }

    /**
     * Clear all data from the database and cancel scheduled alarms
     */
    fun clearAllData(context: Context? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            context?.let { ctx ->
                activeSchedules.value.forEach { sched ->
                    AlarmOrchestrator.cancelAllAlarmsForSchedule(ctx, sched.alarmRequestCodeBase)
                }
            }
            repository.clearAllData()
            context?.let { ctx ->
                ctx.getSharedPreferences("ameen_user_profile", Context.MODE_PRIVATE)
                    .edit().clear().apply()
            }
        }
    }
}
