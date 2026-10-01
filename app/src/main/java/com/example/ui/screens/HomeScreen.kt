package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FamilyMember
import com.example.data.model.Medication
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

@Composable
fun HomeScreen(
    viewModel: MedicationViewModel,
    onNavigateToMedications: () -> Unit,
    onNavigateToFamily: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onOpenAddMedication: () -> Unit,
    onNavigateToDiagnostics: () -> Unit = {}
) {
    val context = LocalContext.current
    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val medications by viewModel.allMedications.collectAsStateWithLifecycle()
    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()

    var showLateDoseDialog by remember { mutableStateOf<Medication?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var doseSuccessCelebration by remember { mutableStateOf(false) }

    // Filtered medications for selected family member
    val memberMeds = remember(medications, selectedMemberId) {
        if (selectedMemberId == null) medications else medications.filter { it.familyMemberId == selectedMemberId }
    }

    // Low stock medications (remaining doses <= 5 or <= 3 days)
    val lowStockMeds = remember(memberMeds) {
        memberMeds.filter { med ->
            val daily = if (med.dailyDoseCount > 0) med.dailyDoseCount else 1
            !med.isPRN && ((med.currentStock / daily) <= 5 || med.currentStock <= 5)
        }
    }

    // Next upcoming medication
    val nextMedication = remember(memberMeds) {
        memberMeds.firstOrNull { !it.isPRN } ?: memberMeds.firstOrNull()
    }

    val mamaMember = members.find { it.relation.contains("جد") || it.relation.contains("أم") || it.name.contains("ماما") }
    val mamaMedications = medications.filter { it.familyMemberId == mamaMember?.id }

    val isProfileSetupDoneState = remember { mutableStateOf(isUserProfileSetupDone(context)) }
    var isProfileSetupDone by isProfileSetupDoneState
    var currentUserName by remember { mutableStateOf(getUserDisplayName(context)) }
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmeenTheme.colors.background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Top Header Bar
        item {
            HeaderBar(
                userName = currentUserName,
                isDarkMode = isDarkMode,
                onToggleDarkMode = {
                    viewModel.toggleDarkMode()
                    Toast.makeText(
                        context,
                        if (!isDarkMode) "تم تفعيل الوضع الليلي 🌙" else "تم تفعيل الوضع الفاتح ☀️",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onNotificationClick = {
                    onNavigateToDiagnostics()
                },
                onResetDataClick = {
                    showResetConfirmDialog = true
                }
            )
        }

        // 2. Personal Setup Card or Welcome Medication Summary Card
        item {
            AnimatedContent(
                targetState = isProfileSetupDone,
                label = "profile_setup_transition",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            ) { isDone ->
                if (!isDone) {
                    PersonalSetupCard(
                        onSaved = { name, selectedRelations ->
                            currentUserName = name
                            isProfileSetupDone = true

                            // Update or insert primary self member
                            val selfMember = members.find { it.relation == "أنا" }
                            if (selfMember != null) {
                                viewModel.updateFamilyMember(selfMember.copy(name = name))
                            } else {
                                viewModel.addFamilyMember(
                                    FamilyMember(
                                        name = name,
                                        relation = "أنا",
                                        isGuardian = true,
                                        avatarColorHex = "#00897B"
                                    )
                                )
                            }

                            // Add any newly selected family relatives
                            selectedRelations.forEach { (displayName, relationTag) ->
                                val exists = members.any { it.relation == relationTag || it.name.contains(displayName) }
                                if (!exists) {
                                    viewModel.addFamilyMember(
                                        FamilyMember(
                                            name = displayName,
                                            relation = relationTag,
                                            avatarColorHex = when (relationTag) {
                                                "أم", "جدة" -> "#E91E63"
                                                "زوج/زوجة" -> "#8E24AA"
                                                "ابن", "ابنة" -> "#26A69A"
                                                else -> "#00897B"
                                            }
                                        )
                                    )
                                }
                            }
                            Toast.makeText(context, "أهلاً بك يا $name! تم حفظ بياناتك وعائلتك بنجاح 🌿", Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    WelcomeMedicationSummaryCard(
                        userName = currentUserName,
                        medicationsCount = medications.size,
                        onAddMedicationClick = onOpenAddMedication,
                        onEditProfileClick = {
                            isProfileSetupDone = false
                        }
                    )
                }
            }
        }

        // 2. Family Members Chips Filter
        item {
            FamilyFilterChips(
                members = members,
                selectedMemberId = selectedMemberId,
                onSelectMember = { viewModel.setSelectedMember(it) },
                onAddMemberClick = onNavigateToFamily
            )
        }

        // 3. Hero "الدواء القادم" Next Medication Card or Empty State
        item {
            if (nextMedication != null) {
                NextMedicationCard(
                    medication = nextMedication,
                    ownerName = members.find { it.id == nextMedication.familyMemberId }?.name ?: "العائلة",
                    onTakeDoseClick = {
                        viewModel.takeDose(
                            medicationId = nextMedication.id,
                            familyMemberId = nextMedication.familyMemberId
                        )
                        doseSuccessCelebration = true
                        Toast.makeText(context, "بارك الله فيك! تم تسجيل الجرعة وتحديث المخزون ✓", Toast.LENGTH_LONG).show()
                    },
                    onTakeLateClick = {
                        showLateDoseDialog = nextMedication
                    }
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AmeenTheme.colors.surface),
                    border = BorderStroke(1.5.dp, AmeenTheme.colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(AmeenTheme.colors.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = AmeenTheme.colors.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "لا توجد أدوية مجدولة حالياً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AmeenTheme.colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أضف أدويتك لتحديد مواعيد الجرعات والتنبيهات ومتابعة المخزون والالتزام بدقة 🌿",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmeenTheme.colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenAddMedication,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmeenTheme.colors.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة دواء جديد", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // 5. "دواء ماما اتخد؟" (Caregiver status bar)
        if (mamaMember != null && mamaMedications.isNotEmpty()) {
            item {
                CaregiverReassuranceCard(
                    mamaName = mamaMember.name,
                    medicationsCount = mamaMedications.size,
                    onCheckMamaClick = {
                        viewModel.setSelectedMember(mamaMember.id)
                        onNavigateToMedications()
                    }
                )
            }
        }

        // 6. Low Stock Alert Banner (Refill Reminders)
        if (lowStockMeds.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = WarningAmberLight),
                    border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val amberAccent = if (AmeenTheme.isDark) WarningAmber else WarningAmberDark
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = amberAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تنبيه نقص المخزون الدوائي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = amberAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "يوجد ${lowStockMeds.size} دواء قارب على النفاد. احرص على تجديد العلب من الصيدلية في أقرب وقت.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            lowStockMeds.take(2).forEach { med ->
                                Surface(
                                    color = SurfaceLight,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderLight),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = med.brandName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${med.currentStock} حبات",
                                            fontSize = 11.sp,
                                            color = amberAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onNavigateToShopping,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, amberAccent),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = amberAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح قائمة مشتريات الصيدلية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 7. Today's Medication Schedule / List
        if (memberMeds.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "أدويتي ومواعيد الجرعات (${memberMeds.size})",
                    actionText = "عرض الكل",
                    onActionClick = onNavigateToMedications
                )
            }

            items(memberMeds, key = { it.id }) { med ->
                val owner = members.find { it.id == med.familyMemberId }
                HomeMedicationCard(
                    medication = med,
                    owner = owner,
                    onTakeDose = {
                        viewModel.takeDose(med.id, med.familyMemberId)
                        doseSuccessCelebration = true
                        Toast.makeText(context, "تم تسجيل جرعة ${med.brandName} وتحديث المخزون ✓", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            // Medication Guide for Empty State
            item {
                SectionHeader(title = "مميزات منبه ومساعد الأدوية")
            }
            item {
                EmptyMedicationGuideCard()
            }
        }

        // 8. Quick Shortcuts Card
        item {
            HomeShortcutsCard(
                onOpenAddMedication = onOpenAddMedication,
                onNavigateToShopping = onNavigateToShopping,
                onNavigateToFamily = onNavigateToFamily
            )
        }
    }

    // Delayed Dose Confirmation Dialog
    if (showLateDoseDialog != null) {
        val med = showLateDoseDialog!!
        LateDoseDialog(
            medication = med,
            onDismiss = { showLateDoseDialog = null },
            onConfirm = {
                viewModel.takeDose(
                    medicationId = med.id,
                    familyMemberId = med.familyMemberId,
                    isDelayed = true,
                    notes = "جرعة متأخرة مع تأكيد المستخدم"
                )
                showLateDoseDialog = null
                Toast.makeText(context, "تم تسجيل الجرعة المتأخرة بدقة ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Reset All Data Confirmation Dialog
    if (showResetConfirmDialog) {
        ResetConfirmDialog(
            onDismiss = { showResetConfirmDialog = false },
            onConfirm = {
                viewModel.clearAllData(context)
                showResetConfirmDialog = false
                isProfileSetupDone = false
                currentUserName = "المستخدم"
                Toast.makeText(context, "تم مسح جميع البيانات بنجاح للبدء من الصفر 🌿", Toast.LENGTH_LONG).show()
            }
        )
    }
}
