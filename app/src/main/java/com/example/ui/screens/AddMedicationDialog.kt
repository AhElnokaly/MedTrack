package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ai.DrugInfo
import com.example.ai.DrugKnowledgeService
import com.example.data.model.FamilyMember
import com.example.data.model.Medication
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    viewModel: MedicationViewModel,
    members: List<FamilyMember>,
    medicationToEdit: Medication? = null,
    initialSchedules: List<String> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val textFieldColors = getHighContrastTextFieldColors()

    // Form fields
    var brandName by remember { mutableStateOf(medicationToEdit?.brandName ?: "") }
    var genericName by remember { mutableStateOf(medicationToEdit?.genericName ?: "") }
    var dosageAmount by remember { mutableStateOf(medicationToEdit?.dosageAmount ?: "1") }
    var dosageUnit by remember { mutableStateOf(medicationToEdit?.dosageUnit ?: "قرص") }
    var timingRule by remember { mutableStateOf(medicationToEdit?.timingRule ?: "مع الأكل") }
    var isCritical by remember { mutableStateOf(medicationToEdit?.isCritical ?: false) }
    var isPRN by remember { mutableStateOf(medicationToEdit?.isPRN ?: false) }
    var currentStock by remember { mutableStateOf(medicationToEdit?.currentStock?.toString() ?: "30") }
    var autoAddToShoppingList by remember { mutableStateOf(medicationToEdit?.autoAddToShoppingList ?: true) }
    var selectedMemberId by remember {
        mutableStateOf(medicationToEdit?.familyMemberId ?: members.firstOrNull()?.id ?: "")
    }
    var instructions by remember { mutableStateOf(medicationToEdit?.instructions ?: "") }
    var sideEffects by remember { mutableStateOf(medicationToEdit?.sideEffects ?: "") }

    // Packaging calculator fields
    var packageType by remember { mutableStateOf(medicationToEdit?.packageType ?: "STRIPS") }
    var boxCount by remember { mutableStateOf("1") }
    var extraStripsCount by remember { mutableStateOf("0") }
    var extraPillsCount by remember { mutableStateOf("0") }
    var stripsPerBox by remember { mutableStateOf(medicationToEdit?.stripsCount?.toString() ?: "2") }
    var pillsPerStrip by remember { mutableStateOf(medicationToEdit?.pillsPerStrip?.toString() ?: "10") }

    // Course Duration & Recurrence pattern
    var isChronic by remember { mutableStateOf(medicationToEdit?.isChronic ?: (medicationToEdit?.durationDays == null)) }
    var durationDays by remember { mutableStateOf(medicationToEdit?.durationDays?.toString() ?: "7") }
    var recurrencePattern by remember { mutableStateOf(medicationToEdit?.recurrencePattern ?: "DAILY") }
    var selectedDays by remember {
        mutableStateOf(
            if (medicationToEdit != null && !medicationToEdit.recurrenceDays.isNullOrBlank())
                medicationToEdit.recurrenceDays.split(",").toSet()
            else setOf("السبت", "الإثنين", "الأربعاء")
        )
    }

    // Sound configuration for Real Alarm System
    var soundType by remember { mutableStateOf(medicationToEdit?.soundType ?: "TONE") } // TONE, TTS, CUSTOM_FILE, RECORDED
    var soundUri by remember { mutableStateOf(medicationToEdit?.soundUri) }
    var ttsTemplate by remember { mutableStateOf(medicationToEdit?.ttsTemplate ?: "DEFAULT") }

    // Scheduled times
    var timesOfDay by remember {
        mutableStateOf(
            if (initialSchedules.isNotEmpty()) initialSchedules
            else if (medicationToEdit != null && medicationToEdit.dailyDoseCount > 0) listOf("08:00")
            else listOf("08:00")
        )
    }

    // Drug search state
    var searchSuggestions by remember { mutableStateOf<List<DrugInfo>>(emptyList()) }
    var selectedDrugInfo by remember { mutableStateOf<DrugInfo?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceLight,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, BorderLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Row
                AddMedicationDialogHeader(
                    isEditing = medicationToEdit != null,
                    onDismiss = onDismiss
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight, thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Search & Brand Name
                    item {
                        Text(
                            text = "اسم الدواء (تجاري أو علمي) 🔍",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = brandName,
                            onValueChange = { query ->
                                brandName = query
                                searchSuggestions = DrugKnowledgeService.searchLocal(query)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("مثال: أملوديبين، كونكور، جلوكوفاج، بانادول...") },
                            leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null, tint = EmeraldPrimary) },
                            trailingIcon = {
                                if (brandName.isNotEmpty()) {
                                    IconButton(onClick = {
                                        brandName = ""
                                        searchSuggestions = emptyList()
                                        selectedDrugInfo = null
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "مسح", tint = TextSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = textFieldColors
                        )

                        if (brandName.isNotBlank() && searchSuggestions.isEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(
                                            context,
                                            "ميزة البحث الموسع بالذكاء الاصطناعي (Gemini) قيد التفعيل في التحديث القادم.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = EmeraldDark
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldDark)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بحث AI • قريباً", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Local Suggestions List
                    if (searchSuggestions.isNotEmpty()) {
                        items(searchSuggestions) { drug ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        brandName = drug.brandName
                                        genericName = drug.genericName
                                        dosageAmount = drug.commonDosage
                                        dosageUnit = drug.unit
                                        timingRule = drug.timingRule
                                        instructions = drug.indications
                                        sideEffects = "${drug.contraindications} • ${drug.foodAndDrugInteractions}"
                                        isCritical = drug.isCritical
                                        isPRN = drug.isPRN
                                        selectedDrugInfo = drug
                                        searchSuggestions = emptyList()
                                    },
                                colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderLight)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(drug.brandName, fontWeight = FontWeight.Bold, color = EmeraldDark, fontSize = 15.sp)
                                        Text(drug.commonDosage + " " + drug.unit, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    }
                                    Text("الاسم العلمي: ${drug.genericName} • ${drug.timingRule}", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Family Member Selector
                    item {
                        FamilyMemberSelectorSection(
                            members = members,
                            selectedMemberId = selectedMemberId,
                            onMemberSelected = { selectedMemberId = it }
                        )
                    }

                    // Dosage & Unit
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = dosageAmount,
                                onValueChange = { dosageAmount = it },
                                label = { Text("الجرعة (رقم)", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors
                            )
                            OutlinedTextField(
                                value = dosageUnit,
                                onValueChange = { dosageUnit = it },
                                label = { Text("الوحدة (قرص، مجم)", fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = textFieldColors
                            )
                        }
                    }

                    // Timing rule
                    item {
                        TimingRuleSelectorSection(
                            timingRule = timingRule,
                            onTimingRuleSelected = { timingRule = it }
                        )
                    }

                    // Scheduled Times (for non-PRN)
                    if (!isPRN) {
                        item {
                            DailyTimesSection(
                                timesOfDay = timesOfDay,
                                onTimesChanged = { timesOfDay = it }
                            )
                        }
                    }

                    // Smart Packaging Calculator Section (Boxes, Strips, Bottles, Drops)
                    item {
                        val bInt = boxCount.toIntOrNull() ?: 0
                        val esInt = extraStripsCount.toIntOrNull() ?: 0
                        val epInt = extraPillsCount.toIntOrNull() ?: 0
                        val sInt = stripsPerBox.toIntOrNull() ?: 2
                        val pInt = pillsPerStrip.toIntOrNull() ?: 10
                        val computed = (bInt * sInt * pInt) + (esInt * pInt) + epInt

                        PackagingCalculatorSection(
                            packageType = packageType,
                            onPackageTypeChanged = { packageType = it },
                            boxCount = boxCount,
                            onBoxCountChanged = { boxCount = it },
                            extraStripsCount = extraStripsCount,
                            onExtraStripsCountChanged = { extraStripsCount = it },
                            extraPillsCount = extraPillsCount,
                            onExtraPillsCountChanged = { extraPillsCount = it },
                            stripsPerBox = stripsPerBox,
                            onStripsPerBoxChanged = { stripsPerBox = it },
                            pillsPerStrip = pillsPerStrip,
                            onPillsPerStripChanged = { pillsPerStrip = it },
                            calculatedTotal = computed,
                            onCalculatedTotalApplied = { totalPills ->
                                currentStock = totalPills.toString()
                                Toast.makeText(context, "تم تطبيق إجمالي المخزون: $totalPills قرصاً ✓", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Total Stock in Units
                    item {
                        Text(
                            text = "الكمية المتوفرة في المخزون 📦",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = currentStock,
                            onValueChange = { currentStock = it.filter { c -> c.isDigit() } },
                            label = { Text("عدد الأقراص / العبوات المتوفرة", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                    }

                    // Treatment Duration & Recurrence Pattern
                    if (!isPRN) {
                        item {
                            TreatmentDurationSection(
                                isChronic = isChronic,
                                onChronicChanged = { isChronic = it },
                                durationDays = durationDays,
                                onDurationDaysChanged = { durationDays = it },
                                recurrencePattern = recurrencePattern,
                                onRecurrencePatternChanged = { recurrencePattern = it },
                                selectedDays = selectedDays,
                                onDaysChanged = { selectedDays = it }
                            )
                        }
                    }

                    // Critical, PRN, Shopping list switches
                    item {
                        MedicationOptionsSection(
                            isCritical = isCritical,
                            onCriticalChange = { isCritical = it },
                            isPRN = isPRN,
                            onPRNChange = { isPRN = it },
                            autoAddToShoppingList = autoAddToShoppingList,
                            onAutoAddToShoppingListChange = { autoAddToShoppingList = it }
                        )
                    }

                    // Sound Type Selector for Real Alarm System
                    if (!isPRN) {
                        item {
                            SoundTypeSelectorSection(
                                context = context,
                                selectedSoundType = soundType,
                                soundUri = soundUri,
                                onSoundTypeSelected = { soundType = it },
                                onSoundUriChanged = { soundUri = it }
                            )
                        }

                        // If TTS is selected, display customizable speech phrasing templates
                        if (soundType == "TTS") {
                            item {
                                val selectedMember = members.find { it.id == selectedMemberId }
                                TtsTemplateSelectorSection(
                                    medicationName = brandName,
                                    memberName = selectedMember?.name ?: "المريض",
                                    selectedTemplate = ttsTemplate,
                                    onTemplateSelected = { ttsTemplate = it }
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderLight, thickness = 1.dp)

                // Action Buttons (Cancel / Save)
                AddMedicationActionButtons(
                    isEditing = medicationToEdit != null,
                    isSaveEnabled = brandName.isNotBlank(),
                    onDismiss = onDismiss,
                    onSave = {
                        val stockInt = currentStock.toIntOrNull() ?: 30
                        val daysInt = if (isChronic) null else (durationDays.toIntOrNull() ?: 7)

                        val med = Medication(
                            id = medicationToEdit?.id ?: java.util.UUID.randomUUID().toString(),
                            familyMemberId = selectedMemberId,
                            brandName = brandName.trim(),
                            genericName = genericName.ifBlank { null },
                            dosageAmount = dosageAmount,
                            dosageUnit = dosageUnit,
                            timingRule = timingRule,
                            isCritical = isCritical,
                            isPRN = isPRN,
                            soundType = soundType,
                            soundUri = soundUri,
                            packageType = packageType,
                            stripsCount = stripsPerBox.toIntOrNull() ?: 2,
                            pillsPerStrip = pillsPerStrip.toIntOrNull() ?: 10,
                            isChronic = isChronic,
                            durationDays = daysInt,
                            recurrencePattern = recurrencePattern,
                            recurrenceDays = if (recurrencePattern == "SPECIFIC_DAYS") selectedDays.joinToString(",") else "",
                            ttsTemplate = ttsTemplate,
                            currentStock = stockInt,
                            initialStock = if (medicationToEdit != null) medicationToEdit.initialStock else stockInt,
                            dailyDoseCount = if (isPRN) 0 else timesOfDay.size,
                            autoAddToShoppingList = autoAddToShoppingList,
                            instructions = instructions.ifBlank { null },
                            sideEffects = sideEffects.ifBlank { null },
                            createdAt = medicationToEdit?.createdAt ?: System.currentTimeMillis()
                        )

                        viewModel.saveMedication(med, if (isPRN) emptyList() else timesOfDay, context)
                        Toast.makeText(context, "تم حفظ الدواء وجدولة التنبيهات بنجاح ✓", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )
            }
        }
    }
}
