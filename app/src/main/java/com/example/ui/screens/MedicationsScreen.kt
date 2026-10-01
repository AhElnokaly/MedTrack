package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.Medication
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

@Composable
fun MedicationsScreen(
    viewModel: MedicationViewModel,
    onOpenAddMedication: () -> Unit
) {
    val context = LocalContext.current
    val medications by viewModel.filteredMedications.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()
    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val schedules by viewModel.activeSchedules.collectAsStateWithLifecycle()
    val selectedMemberId by viewModel.selectedMemberId.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTab by remember { mutableStateOf("ALL") } // ALL, CRITICAL, PRN, LOW_STOCK
    var medicationDetailDialog by remember { mutableStateOf<Medication?>(null) }
    var adjustStockDialog by remember { mutableStateOf<Medication?>(null) }
    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }
    var medicationToEdit by remember { mutableStateOf<Medication?>(null) }

    val filteredList = remember(medications, searchQuery, selectedFilterTab) {
        medications.filter { med ->
            val matchQuery = searchQuery.isBlank() ||
                    med.brandName.contains(searchQuery, ignoreCase = true) ||
                    (med.genericName?.contains(searchQuery, ignoreCase = true) == true)

            val matchFilter = when (selectedFilterTab) {
                "CRITICAL" -> med.isCritical
                "PRN" -> med.isPRN
                "LOW_STOCK" -> {
                    val daily = if (med.dailyDoseCount > 0) med.dailyDoseCount else 1
                    (med.currentStock / daily) <= 5 && !med.isPRN
                }
                else -> true
            }

            matchQuery && matchFilter
        }
    }

    Scaffold(
        containerColor = AmeenTheme.colors.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddMedication,
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("add_medication_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة دواء جديد", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "صيدلية المنزل والأدوية",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "إدارة المخزون، مواعيد التناول، وقواعد الأمان الدوائي لكل فرد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    placeholder = { Text("ابحث عن دواء بالاسم التجاري أو العلمي...", color = TextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = getHighContrastTextFieldColors()
                )
            }

            // Family Member Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMemberId == null,
                            onClick = { viewModel.setSelectedMember(null) },
                            label = { Text("الكل (${allMedications.size})", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceLight,
                                labelColor = TextSecondary
                            )
                        )
                    }

                    items(members, key = { it.id }) { member ->
                        val count = allMedications.count { it.familyMemberId == member.id }
                        FilterChip(
                            selected = selectedMemberId == member.id,
                            onClick = { viewModel.setSelectedMember(member.id) },
                            label = { Text("${member.name} ($count)", fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceLight,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Quick Category Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterTabButton(
                        text = "جميع الأدوية",
                        isSelected = selectedFilterTab == "ALL",
                        onClick = { selectedFilterTab = "ALL" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "أدوية حرجة 🚨",
                        isSelected = selectedFilterTab == "CRITICAL",
                        onClick = { selectedFilterTab = "CRITICAL" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "طوارئ (PRN)",
                        isSelected = selectedFilterTab == "PRN",
                        onClick = { selectedFilterTab = "PRN" },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        text = "مخزون منخفض ⚠️",
                        isSelected = selectedFilterTab == "LOW_STOCK",
                        onClick = { selectedFilterTab = "LOW_STOCK" },
                        modifier = Modifier.weight(1.1f)
                    )
                }
            }

            // Medication List
            if (filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💊", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا توجد أدوية تطابق بحثك" else "لا توجد أدوية مسجلة في هذا القسم",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "يمكنك إضافة دواء جديد بالضغط على زر الإضافة أدناه",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { medication ->
                    val member = members.find { it.id == medication.familyMemberId }
                    val medSchedules = schedules.filter { it.medicationId == medication.id }

                    ExpandableMedicationCard(
                        medication = medication,
                        schedules = medSchedules,
                        owner = member,
                        onTakeDose = {
                            viewModel.takeDose(medicationId = medication.id, familyMemberId = medication.familyMemberId)
                            Toast.makeText(context, "تم تسجيل تناول ${medication.brandName} وخصم قرص من المخزون ✓", Toast.LENGTH_SHORT).show()
                        },
                        onSnoozeDose = {
                            Toast.makeText(context, "تم تأجيل تنبيه ${medication.brandName} لمدة 15 دقيقة ⏰", Toast.LENGTH_SHORT).show()
                        },
                        onEditMedication = {
                            medicationToEdit = medication
                        },
                        onDeleteMedication = {
                            medicationToDelete = medication
                        },
                        onQuickRefill = { addedAmount ->
                            val newStock = medication.currentStock + addedAmount
                            viewModel.updateStock(medication.id, newStock)
                            Toast.makeText(context, "تمت تعبئة المخزون: +$addedAmount قرصاً (الإجمالي: $newStock) ✓", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Edit Medication Dialog
    if (medicationToEdit != null) {
        val med = medicationToEdit!!
        val editSchedules = schedules.filter { it.medicationId == med.id }.map { it.timeOfDay }
        AddMedicationDialog(
            viewModel = viewModel,
            members = members,
            medicationToEdit = med,
            initialSchedules = editSchedules,
            onDismiss = { medicationToEdit = null }
        )
    }

    // Detail Dialog
    if (medicationDetailDialog != null) {
        MedicationDetailDialog(
            med = medicationDetailDialog!!,
            onDismiss = { medicationDetailDialog = null }
        )
    }

    // Adjust Stock Dialog
    if (adjustStockDialog != null) {
        val med = adjustStockDialog!!
        AdjustStockDialog(
            med = med,
            onDismiss = { adjustStockDialog = null },
            onConfirm = { count ->
                viewModel.updateStock(med.id, count)
                adjustStockDialog = null
                Toast.makeText(context, "تم تحديث المخزون إلى $count", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Medication Confirmation Dialog
    if (medicationToDelete != null) {
        val med = medicationToDelete!!
        DeleteMedicationConfirmationDialog(
            med = med,
            onDismiss = { medicationToDelete = null },
            onConfirm = {
                viewModel.deleteMedication(med, context)
                medicationToDelete = null
                Toast.makeText(context, "تم حذف الدواء وإلغاء تنبيهاته ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun FilterTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) EmeraldPrimary else SurfaceLight,
        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else BorderLight),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary,
                maxLines = 1
            )
        }
    }
}
