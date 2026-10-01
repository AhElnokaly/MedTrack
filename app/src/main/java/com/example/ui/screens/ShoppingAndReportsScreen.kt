package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ShoppingListItem
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

@Composable
fun ShoppingAndReportsScreen(
    viewModel: MedicationViewModel
) {
    val context = LocalContext.current
    val shoppingList by viewModel.shoppingList.collectAsStateWithLifecycle()
    val medications by viewModel.allMedications.collectAsStateWithLifecycle()
    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val logs by viewModel.allLogs.collectAsStateWithLifecycle()
    val prescriptions by viewModel.allPrescriptions.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Shopping, 1: Travel, 2: Reports, 3: Prescriptions
    val tabTitles = listOf("قائمة الشراء 🛒", "وضع السفر ✈️", "تقارير الطبيب 📊", "الروشتات 📄")

    var travelDaysStr by remember { mutableStateOf("14") }
    var showAddPrescriptionDialog by remember { mutableStateOf(false) }
    var itemToPurchaseWithDialog by remember { mutableStateOf<ShoppingListItem?>(null) }

    // Dynamic compliance calculation
    val totalLogs = logs.size
    val takenLogs = logs.count { it.status == "TAKEN" }
    val lateLogs = logs.count { it.status == "LATE" }
    val missedLogs = logs.count { it.status == "MISSED" }
    val compliantLogs = takenLogs + lateLogs

    val overallComplianceText = if (totalLogs > 0) {
        val rate = (compliantLogs * 100) / totalLogs
        "$rate%"
    } else {
        "-"
    }

    val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
    val weeklyLogs = logs.filter { it.scheduledAt >= oneWeekAgo }
    val weeklyComplianceText = if (weeklyLogs.isNotEmpty()) {
        val weeklyCompliant = weeklyLogs.count { it.status == "TAKEN" || it.status == "LATE" }
        val rate = (weeklyCompliant * 100) / weeklyLogs.size
        "$rate%"
    } else if (totalLogs > 0) {
        overallComplianceText
    } else {
        "-"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Tab Header
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = SurfaceLight,
            contentColor = EmeraldDark,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            },
            edgePadding = 16.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (activeTab == index) EmeraldDark else TextSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (activeTab) {
                // ------------------ TAB 0: SHOPPING LIST ------------------
                0 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MintContainer),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "قائمة الشراء والصيدلية 🛒",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Surface(
                                        color = SurfaceLight,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, BorderLight)
                                    ) {
                                        Text(
                                            text = "${shoppingList.count { !it.isPurchased }} أدوية مطلوبة",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = EmeraldPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "تضاف الأدوية آلياً عند انخفاض المخزون (تنبيه طارئ أحمر عند متبقي ≤ يومين، وتنبيه عند متبقي ≤ 5 أيام).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (shoppingList.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🎉", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "مخزون جميع الأدوية كافٍ تماماً!",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "لا توجد أدوية تحتاج للشراء حالياً",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        val criticalItems = shoppingList.filter { it.addedReason == "CRITICAL_LOW_STOCK" && !it.isPurchased }
                        val regularItems = shoppingList.filter { !(it.addedReason == "CRITICAL_LOW_STOCK" && !it.isPurchased) }

                        if (criticalItems.isNotEmpty()) {
                            item {
                                Surface(
                                    color = CriticalRedLight,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CriticalRed),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🚨", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "أدوية في حالة نفاد وشيك (متبقي يومين أو أقل) - شراء عاجل مطلوب!",
                                            fontWeight = FontWeight.Bold,
                                            color = CriticalRedDark,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            items(criticalItems, key = { it.id }) { item ->
                                val matchingMed = medications.find { 
                                    it.id == item.medicationId || it.brandName.equals(item.medicationName, ignoreCase = true) 
                                }
                                ShoppingItemCard(
                                    item = item,
                                    medication = matchingMed,
                                    onTogglePurchased = { viewModel.toggleShoppingItemPurchased(it) },
                                    onDelete = { viewModel.deleteShoppingItem(it) },
                                    onRecordPurchase = { itemToPurchaseWithDialog = it }
                                )
                            }
                        }

                        if (regularItems.isNotEmpty()) {
                            if (criticalItems.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "باقي أدوية القائمة:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            items(regularItems, key = { it.id }) { item ->
                                val matchingMed = medications.find { 
                                    it.id == item.medicationId || it.brandName.equals(item.medicationName, ignoreCase = true) 
                                }
                                ShoppingItemCard(
                                    item = item,
                                    medication = matchingMed,
                                    onTogglePurchased = { viewModel.toggleShoppingItemPurchased(it) },
                                    onDelete = { viewModel.deleteShoppingItem(it) },
                                    onRecordPurchase = { itemToPurchaseWithDialog = it }
                                )
                            }
                        }
                    }
                }

                // ------------------ TAB 1: TRAVEL MODE ------------------
                1 -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MintContainer),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "وضع السفر الذكي ✈️",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("🧳", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "يحسب بدقة كمية الأدوية الكافية لرحلتك مع إضافة احتياط أمان يومين إضافيين لحالات الطوارئ.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                OutlinedTextField(
                                    value = travelDaysStr,
                                    onValueChange = { travelDaysStr = it.filter { c -> c.isDigit() } },
                                    label = { Text("مدة السفر بالـ أيام", fontWeight = FontWeight.SemiBold) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = getHighContrastTextFieldColors()
                                )
                            }
                        }
                    }

                    val days = travelDaysStr.toIntOrNull() ?: 14
                    item {
                        Text(
                            text = "خطة حقيبة الأدوية لرحلة مدتها $days يوماً (+ يومين احتياط):",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    items(medications.filter { !it.isPRN }) { med ->
                        val member = members.find { it.id == med.familyMemberId }
                        val needed = viewModel.calculateTravelSupply(med, days)
                        TravelSupplyCard(
                            med = med,
                            memberName = member?.name ?: "العائلة",
                            neededQuantity = needed
                        )
                    }
                }

                // ------------------ TAB 2: DOCTOR REPORTS ------------------
                2 -> {
                    item {
                        ComplianceSummaryCard(
                            context = context,
                            todayDateString = viewModel.todayDateString,
                            overallComplianceText = overallComplianceText,
                            weeklyComplianceText = weeklyComplianceText,
                            totalLogs = totalLogs,
                            takenLogs = takenLogs,
                            lateLogs = lateLogs,
                            missedLogs = missedLogs,
                            medications = medications
                        )
                    }

                    item {
                        Text(
                            text = "سجل الجرعات والالتزام الفعلي:",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (logs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("• تم أخذ جرعة أملوديبين 5 مجم في الموعد ✓", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• تم أخذ جرعة كونكور 2.5 مجم في الموعد ✓", color = SuccessGreen, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• تم أخذ جرعة فيتامين D3 متأخرة ساعة ⏰", color = if (AmeenTheme.isDark) WarningAmber else WarningAmberDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        items(logs) { log ->
                            DoseLogItemCard(log = log)
                        }
                    }
                }

                // ------------------ TAB 3: PRESCRIPTIONS ------------------
                3 -> {
                    item {
                        PrescriptionHeaderCard(onAddNew = { showAddPrescriptionDialog = true })
                    }

                    if (prescriptions.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, CardBorderColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("روشتة د. محمود عثمان (استشاري باطنة وقلب)", fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("المريض: ماما (فاطمة) • التاريخ: 2026-08-15", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                    Text("التشخيص: ارتفاع ضغط دم شرياني ومتابعة سكري النوع الثاني", fontSize = 12.sp, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = MintLight,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, BorderLight)
                                    ) {
                                        Text(
                                            text = "الأدوية المستخرجة: أملوديبين 5 مجم، جلوكوفاج 1000 مجم",
                                            color = EmeraldDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(prescriptions) { pres ->
                            val member = members.find { it.id == pres.familyMemberId }
                            PrescriptionCard(prescription = pres, member = member)
                        }
                    }
                }
            }
        }
    }

    if (showAddPrescriptionDialog) {
        AddPrescriptionDialog(
            members = members,
            onDismiss = { showAddPrescriptionDialog = false },
            onSave = { pres ->
                viewModel.addPrescription(pres)
                showAddPrescriptionDialog = false
                Toast.makeText(context, "تم حفظ الروشتة في سجل العائلة بنجاح ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Purchase Quantity Dialog (Full Boxes, Strips, and Loose Pills calculation)
    itemToPurchaseWithDialog?.let { item ->
        val matchingMed = medications.find { 
            it.id == item.medicationId || it.brandName.equals(item.medicationName, ignoreCase = true) 
        }
        PurchaseQuantityDialog(
            item = item,
            medication = matchingMed,
            onDismiss = { itemToPurchaseWithDialog = null },
            onConfirmPurchase = { addedQuantity ->
                viewModel.purchaseShoppingItemWithStock(item, addedQuantity)
                itemToPurchaseWithDialog = null
                Toast.makeText(
                    context, 
                    "تم تسجيل شراء ${item.medicationName} وإضافة +$addedQuantity للمخزون بنجاح ✓", 
                    Toast.LENGTH_LONG
                ).show()
            }
        )
    }
}
