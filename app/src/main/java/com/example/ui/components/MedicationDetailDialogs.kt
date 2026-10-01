package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication
import com.example.data.model.ScheduleEntry
import com.example.ui.theme.*

@Composable
fun MedicationItemCard(
    medication: Medication,
    ownerName: String,
    schedules: List<ScheduleEntry>,
    onTakeDose: () -> Unit,
    onAdjustStock: () -> Unit,
    onInfoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val daily = if (medication.dailyDoseCount > 0) medication.dailyDoseCount else 1
    val daysRemaining = if (medication.isPRN) 999 else (medication.currentStock / daily)
    val isLowStock = daysRemaining <= 5 && !medication.isPRN
    val isCriticalStock = daysRemaining <= 2 && !medication.isPRN

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Member & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = ownerName,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (medication.isCritical) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = CriticalRedLight,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CriticalRed)
                        ) {
                            Text(
                                text = "حرج 🚨",
                                color = CriticalRedDark,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (medication.isPRN) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (AmeenTheme.isDark) Color(0xFF3B0764) else Color(0xFFF3E8FF),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (AmeenTheme.isDark) Color(0xFFC084FC) else Color(0xFF9333EA))
                        ) {
                            Text(
                                text = "طوارئ (عند اللزوم)",
                                color = if (AmeenTheme.isDark) Color(0xFFE9D5FF) else Color(0xFF6B21A8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onInfoClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantLight)
                    ) {
                        Icon(Icons.Outlined.Info, contentDescription = "معلومات الدواء", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CriticalRedLight)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = CriticalRed, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Brand & Dose
            Text(
                text = medication.brandName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 17.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${medication.dosageAmount} ${medication.dosageUnit} • ${medication.timingRule}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            // Scheduled Times
            if (schedules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(16.dp))
                    Text(
                        text = "المواعيد: " + schedules.joinToString(" ، ") { it.timeOfDay },
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock Tracker & Countdown with 2-day urgent distinction
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCriticalStock) CriticalRedLight
                        else if (isLowStock) WarningAmberLight
                        else SurfaceVariantLight
                    )
                    .border(
                        1.dp,
                        if (isCriticalStock) CriticalRed
                        else if (isLowStock) WarningAmber
                        else BorderLight,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (medication.isPRN) "المخزون: ${medication.currentStock} وحدة"
                        else "المخزون: ${medication.currentStock} قرص (يكفي $daysRemaining يوم)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isCriticalStock) CriticalRedDark else if (isLowStock) WarningAmberDark else TextPrimary
                    )
                    val pkgBreakdown = formatPackagingStockBreakdown(
                        currentStock = medication.currentStock,
                        stripsCount = medication.stripsCount,
                        pillsPerStrip = medication.pillsPerStrip,
                        packageType = medication.packageType
                    )
                    if (pkgBreakdown != null) {
                        Text(
                            text = "العبوة: $pkgBreakdown",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldDark
                        )
                    }
                    if (isLowStock) {
                        Text(
                            text = if (isCriticalStock) "🚨 تحذير: يتبقى يومان فقط على النفاد!" else "⚠️ تنبيه: يتبقى 5 أيام فقط على النفاد",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCriticalStock) CriticalRedDark else WarningAmberDark
                        )
                    }
                }

                OutlinedButton(
                    onClick = onAdjustStock,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary)
                ) {
                    Text("تعديل", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Take Dose (High Contrast button)
            Button(
                onClick = onTakeDose,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("أخذت جرعة الآن (خصم من المخزون)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MedicationDetailDialog(
    med: Medication,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = med.brandName,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!med.genericName.isNullOrBlank()) {
                    Text("الاسم العلمي: ${med.genericName}", fontWeight = FontWeight.Medium, color = TextPrimary)
                }
                Text("الجرعة: ${med.dosageAmount} ${med.dosageUnit}", color = TextPrimary)
                Text("القاعدة الزمنية: ${med.timingRule}", color = TextPrimary)
                Text("صوت التنبيه: ${med.soundType}", color = TextPrimary)
                if (!med.instructions.isNullOrBlank()) {
                    Text("تعليمات خاصة: ${med.instructions}", color = TextSecondary)
                }
                if (!med.sideEffects.isNullOrBlank()) {
                    Text("الآثار الجانبية والتداخلات: ${med.sideEffects}", color = CriticalRedDark)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("حسناً", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

@Composable
fun AdjustStockDialog(
    med: Medication,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var entryMode by remember { mutableStateOf(if (med.packageType == "STRIPS") "CALCULATOR" else "DIRECT") }
    var stockCountStr by remember { mutableStateOf(med.currentStock.toString()) }
    
    // Calculator values
    var boxCount by remember { mutableStateOf("0") }
    var extraStrips by remember { mutableStateOf("0") }
    var extraPills by remember { mutableStateOf("0") }
    val stripsPerBox = med.stripsCount.coerceAtLeast(1)
    val pillsPerStrip = med.pillsPerStrip.coerceAtLeast(1)

    val calculatedTotal = run {
        val b = boxCount.toIntOrNull() ?: 0
        val s = extraStrips.toIntOrNull() ?: 0
        val p = extraPills.toIntOrNull() ?: 0
        (b * stripsPerBox * pillsPerStrip) + (s * pillsPerStrip) + p
    }

    val textFieldColors = getHighContrastTextFieldColors()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تعديل مخزون ${med.brandName}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (med.packageType == "STRIPS") {
                    // Mode Switcher Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { entryMode = "CALCULATOR" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (entryMode == "CALCULATOR") EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(1.dp, if (entryMode == "CALCULATOR") EmeraldPrimary else BorderLight)
                        ) {
                            Text(
                                text = "بالعلبة والشريط 💊",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (entryMode == "CALCULATOR") FontWeight.Bold else FontWeight.Medium,
                                color = if (entryMode == "CALCULATOR") Color.White else TextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { entryMode = "DIRECT" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (entryMode == "DIRECT") EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(1.dp, if (entryMode == "DIRECT") EmeraldPrimary else BorderLight)
                        ) {
                            Text(
                                text = "إدخال مباشر (أقراص) 🔢",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (entryMode == "DIRECT") FontWeight.Bold else FontWeight.Medium,
                                color = if (entryMode == "DIRECT") Color.White else TextPrimary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                if (entryMode == "CALCULATOR" && med.packageType == "STRIPS") {
                    Text(
                        text = "العبوة: $stripsPerBox شرائط × $pillsPerStrip قرص",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = boxCount,
                            onValueChange = { boxCount = it.filter { c -> c.isDigit() } },
                            label = { Text("علب كاملة", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = extraStrips,
                            onValueChange = { extraStrips = it.filter { c -> c.isDigit() } },
                            label = { Text("+ شرائط", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = extraPills,
                            onValueChange = { extraPills = it.filter { c -> c.isDigit() } },
                            label = { Text("+ حبات", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                    }

                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "الإجمالي المحسوب: $calculatedTotal قرصاً",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            val daily = if (med.dailyDoseCount > 0) med.dailyDoseCount else 1
                            val days = calculatedTotal / daily
                            Text(
                                text = "يكفيك لمدة حوالي $days يوماً",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    Text("أدخل عدد الأقراص / الوحدات المتوفرة فعلياً في صيدلية المنزل:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = stockCountStr,
                        onValueChange = { stockCountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("المخزون الحالي (وحدات)", fontWeight = FontWeight.SemiBold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalCount = if (entryMode == "CALCULATOR" && med.packageType == "STRIPS") {
                        calculatedTotal
                    } else {
                        stockCountStr.toIntOrNull() ?: med.currentStock
                    }
                    onConfirm(finalCount)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حفظ التعديل", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceLight,
                    contentColor = TextSecondary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextSecondary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}

@Composable
fun DeleteMedicationConfirmationDialog(
    med: Medication,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        titleContentColor = CriticalRedDark,
        textContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "تأكيد حذف الدواء",
                fontWeight = FontWeight.Bold,
                color = CriticalRedDark
            )
        },
        text = {
            Text(
                text = "هل أنت متأكد من حذف دواء '${med.brandName}'؟ سيتم إلغاء كافة التنبيهات المجدولة له وسجل المخزون المرتبط به.",
                color = TextPrimary,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CriticalRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("نعم، حذف الدواء", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = SurfaceLight,
                    contentColor = TextSecondary
                )
            ) {
                Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}
