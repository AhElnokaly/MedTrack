package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY isGuardian DESC, createdAt ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    fun getMemberById(id: String): Flow<FamilyMember?>

    @Query("SELECT * FROM family_members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdSync(id: String): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember)

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)
}

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY isCritical DESC, createdAt DESC")
    fun getAllMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY isCritical DESC, createdAt DESC")
    suspend fun getAllMedicationsSync(): List<Medication>

    @Query("SELECT * FROM medications WHERE familyMemberId = :memberId ORDER BY isCritical DESC, createdAt DESC")
    fun getMedicationsByMember(memberId: String): Flow<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    fun getMedicationById(id: String): Flow<Medication?>

    @Query("SELECT * FROM medications WHERE id = :id LIMIT 1")
    suspend fun getMedicationByIdSync(id: String): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("UPDATE medications SET currentStock = :newStock WHERE id = :medicationId")
    suspend fun updateStock(medicationId: String, newStock: Int)

    @Query("SELECT * FROM medications WHERE barcode = :barcode LIMIT 1")
    suspend fun getMedicationByBarcode(barcode: String): Medication?
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_entries WHERE enabled = 1 ORDER BY timeOfDay ASC")
    fun getAllActiveSchedules(): Flow<List<ScheduleEntry>>

    @Query("SELECT * FROM schedule_entries WHERE medicationId = :medicationId")
    fun getSchedulesForMedication(medicationId: String): Flow<List<ScheduleEntry>>

    @Query("SELECT * FROM schedule_entries WHERE medicationId = :medicationId")
    suspend fun getSchedulesForMedicationSync(medicationId: String): List<ScheduleEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntry>)

    @Query("DELETE FROM schedule_entries WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: String)
}

@Dao
interface DoseLogDao {
    @Query("SELECT * FROM dose_logs ORDER BY scheduledAt DESC")
    fun getAllLogs(): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE familyMemberId = :memberId ORDER BY scheduledAt DESC")
    fun getLogsForMember(memberId: String): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE medicationId = :medicationId ORDER BY scheduledAt DESC")
    fun getLogsForMedication(medicationId: String): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE scheduledAt BETWEEN :startOfDay AND :endOfDay ORDER BY scheduledAt ASC")
    fun getTodayLogs(startOfDay: Long, endOfDay: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE scheduleEntryId = :scheduleEntryId AND scheduledAt BETWEEN :startOfDay AND :endOfDay LIMIT 1")
    suspend fun getTodayLogForSchedule(scheduleEntryId: String, startOfDay: Long, endOfDay: Long): DoseLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DoseLog)

    @Update
    suspend fun updateLog(log: DoseLog)
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY isPurchased ASC, createdAt DESC")
    fun getShoppingList(): Flow<List<ShoppingListItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItem)

    @Update
    suspend fun updateItem(item: ShoppingListItem)

    @Delete
    suspend fun deleteItem(item: ShoppingListItem)

    @Query("DELETE FROM shopping_list_items WHERE medicationId = :medicationId")
    suspend fun deleteByMedicationId(medicationId: String)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE familyMemberId = :memberId ORDER BY createdAt DESC")
    fun getPrescriptionsForMember(memberId: String): Flow<List<Prescription>>

    @Query("SELECT * FROM prescriptions ORDER BY createdAt DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: Prescription)

    @Delete
    suspend fun deletePrescription(prescription: Prescription)
}
