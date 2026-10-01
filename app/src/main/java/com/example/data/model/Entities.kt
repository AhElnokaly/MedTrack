package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val relation: String,          // أب، أم، طفل، جد، جدة، ذاتي
    val birthDate: String? = null,
    val age: Int = 30,
    val gender: String = "ذكر",    // ذكر / أنثى
    val weightKg: Double? = null,
    val heightCm: Double? = null,
    val lastGrowthCheck: Long? = null,
    val isGuardian: Boolean = false,
    val managedBy: String? = null, // id لولي الأمر
    val healthNotes: String? = null,
    val avatarColorHex: String = "#00897B",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "medications",
    indices = [Index(value = ["familyMemberId"])]
)
data class Medication(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val familyMemberId: String,
    val brandName: String,
    val genericName: String? = null,
    val dosageAmount: String = "1",      // "500", "5", "1"
    val dosageUnit: String = "قرص",      // مجم، مل، قرص، كبسولة، بخة
    val timingRule: String = "مع الأكل", // مع الأكل / قبل الأكل / على معدة فارغة / قبل النوم
    val isChronic: Boolean = false,
    val durationDays: Int? = null,       // null يعني مستمر
    val isCritical: Boolean = false,     // ضغط / سكر / قلب (تنبيه تصعيدي)
    val isPRN: Boolean = false,          // عند اللزوم (طوارئ)
    val currentStock: Int = 30,
    val initialStock: Int = 30,
    val dailyDoseCount: Int = 1,
    val autoAddToShoppingList: Boolean = true,
    val instructions: String? = null,
    val sideEffects: String? = null,
    val packageImageUri: String? = null,
    val soundType: String = "TONE", // TONE, TTS, CUSTOM_FILE, RECORDED
    val soundUri: String? = null,
    val packageType: String = "STRIPS", // STRIPS, BOTTLE, DROPS, DIRECT
    val stripsCount: Int = 2,
    val pillsPerStrip: Int = 10,
    val recurrencePattern: String = "DAILY", // DAILY, SPECIFIC_DAYS, INTERVAL
    val recurrenceDays: String = "",         // e.g. "السبت,الإثنين,الأربعاء"
    val ttsTemplate: String = "DEFAULT",
    val barcode: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "schedule_entries",
    indices = [Index(value = ["medicationId"])]
)
data class ScheduleEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val timeOfDay: String = "08:00", // 24-hr format "HH:mm"
    val prayerPhase: String? = null, // الفجر، الظهر، العصر، المغرب، العشاء
    val alarmRequestCodeBase: Int = (1000 + (Math.random() * 8000).toInt()),
    val enabled: Boolean = true
)

@Entity(
    tableName = "dose_logs",
    indices = [
        Index(value = ["medicationId", "scheduledAt"]),
        Index(value = ["familyMemberId", "scheduledAt"])
    ]
)
data class DoseLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val familyMemberId: String,
    val scheduleEntryId: String? = null,
    val scheduledAt: Long,
    val takenAt: Long? = null,
    val status: String = "TAKEN", // TAKEN, MISSED, LATE, SNOOZED
    val isDelayedConfirmed: Boolean = false,
    val notes: String? = null,
    val hijriDate: String? = null
)

@Entity(tableName = "shopping_list_items")
data class ShoppingListItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val medicationId: String,
    val medicationName: String,
    val familyMemberName: String,
    val neededQuantity: Int = 1,
    val addedReason: String = "LOW_STOCK", // LOW_STOCK, MANUAL
    val isPurchased: Boolean = false,
    val preferredPharmacy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "prescriptions",
    indices = [Index(value = ["familyMemberId"])]
)
data class Prescription(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val familyMemberId: String,
    val doctorName: String? = null,
    val clinicOrHospital: String? = null,
    val dateIssued: String = "",
    val diagnosis: String? = null,
    val medicationsNotes: String? = null,
    val imageUri: String? = null,
    val medicationId: String? = null,
    val durationDays: Int? = 30,
    val renewalDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
