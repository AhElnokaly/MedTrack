package com.example

import com.example.data.model.FamilyMember
import com.example.data.model.Medication
import org.junit.Assert.assertEquals
import org.junit.Test

class AmeenBusinessLogicUnitTest {

    @Test
    fun testTravelSupplyCalculationWithSafetyBuffer() {
        val testMed = Medication(
            id = "med_1",
            familyMemberId = "mem_1",
            brandName = "Concor",
            genericName = "Bisoprolol",
            dosageAmount = "5",
            dosageUnit = "مجم",
            dailyDoseCount = 2,
            currentStock = 60,
            isCritical = true
        )

        val travelDays = 7
        val dailyCount = if (testMed.dailyDoseCount > 0) testMed.dailyDoseCount else 1
        val needed = (travelDays + 2) * dailyCount // (7 + 2) * 2 = 18

        assertEquals(18, needed)
    }

    @Test
    fun testCriticalLowStockDetection() {
        val medWithLowStock = Medication(
            id = "med_2",
            familyMemberId = "mem_1",
            brandName = "Amlodipine",
            genericName = "Amlodipine",
            dosageAmount = "5",
            dosageUnit = "مجم",
            dailyDoseCount = 1,
            currentStock = 2, // 2 days remaining (<= 2 is critical)
            isCritical = true
        )

        val dailyConsumption = medWithLowStock.dailyDoseCount.coerceAtLeast(1)
        val daysRemaining = medWithLowStock.currentStock / dailyConsumption

        val isCriticalThreshold = daysRemaining <= 2
        assertEquals(true, isCriticalThreshold)
    }

    @Test
    fun testCompliancePercentageCalculation() {
        val totalLogs = 10
        val takenLogs = 7
        val lateLogs = 2
        val missedLogs = 1

        val compliantLogs = takenLogs + lateLogs // 9
        val rate = (compliantLogs * 100) / totalLogs

        assertEquals(90, rate)
    }

    @Test
    fun testFamilyMemberDefaultValues() {
        val member = FamilyMember(
            id = "fam_1",
            name = "سارة",
            relation = "ابنة",
            age = 8,
            gender = "أنثى",
            weightKg = 25.5,
            heightCm = 122.0,
            isGuardian = false
        )

        assertEquals("سارة", member.name)
        assertEquals(25.5, member.weightKg!!, 0.01)
        assertEquals(false, member.isGuardian)
    }
}
