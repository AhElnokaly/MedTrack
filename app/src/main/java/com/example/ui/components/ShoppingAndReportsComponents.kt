package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

/**
 * Shopping List Item with clear critical threshold indicators:
 * - CRITICAL_LOW_STOCK (<= 2 days remaining): Urgent Red Alert Badge + 2 boxes
 * - LOW_STOCK (<= 5 days remaining): Amber Alert Badge
 * - MANUAL: Standard item
 */
@Composable
fun ShoppingItemCard(
    item: ShoppingListItem,
    medication: com.example.data.model.Medication? = null,
    onTogglePurchased: (ShoppingListItem) -> Unit,
    onDelete: (ShoppingListItem) -> Unit,
    onRecordPurchase: ((ShoppingListItem) -> Unit)? = null
) {
    val isCritical = item.addedReason == "CRITICAL_LOW_STOCK"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                item.isPurchased -> SurfaceVariantLight
                isCritical -> CriticalRedLight
                else -> SurfaceLight
            }
        ),
        border = BorderStroke(
            1.5.dp,
            when {
                item.isPurchased -> BorderLight
                isCritical -> CriticalRed
                else -> CardBorderColor
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = item.isPurchased,
                        onCheckedChange = { 
                            if (!item.isPurchased && onRecordPurchase != null) {
                                onRecordPurchase(item)
                            } else {
                                onTogglePurchased(item)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldPrimary,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.medicationName,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isPurchased) TextMuted else TextPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (isCritical && !item.isPurchased) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = CriticalRed,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "🚨 طارئ (متبقي ≤ يومين)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "المريض: ${item.familyMemberName} • المطلوب: ${item.neededQuantity} عبوة",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCritical && !item.isPurchased) CriticalRedDark else TextSecondary,
                            fontWeight = if (isCritical && !item.isPurchased) FontWeight.Bold else FontWeight.Medium
                        )
                        if (medication != null && !item.isPurchased) {
                            val pkg = formatPackagingStockBreakdown(
                                currentStock = medication.currentStock,
                                stripsCount = medication.stripsCount,
                                pillsPerStrip = medication.pillsPerStrip,
                                packageType = medication.packageType
                            )
                            val stockInfo = if (pkg != null) {
                                "المتبقي: ${medication.currentStock} قرص ($pkg)"
                            } else {
                                "المتبقي: ${medication.currentStock} ${medication.dosageUnit}"
                            }
                            Text(
                                text = stockInfo,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCritical) CriticalRedDark else EmeraldDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!item.isPurchased && onRecordPurchase != null) {
                        Button(
                            onClick = { onRecordPurchase(item) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("تسجيل شراء 🛒", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    IconButton(
                        onClick = { onDelete(item) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(CriticalRedLight)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف البند",
                            tint = CriticalRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Travel Mode Supply Calculator Card
 */
@Composable
fun TravelSupplyCard(
    med: Medication,
    memberName: String,
    neededQuantity: Int
) {
    val shortfall = (neededQuantity - med.currentStock).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(med.brandName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Surface(
                    color = MintContainer,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = memberName,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "الكمية المطلوبة للسفر: $neededQuantity قرص / وحدة",
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                fontSize = 14.sp
            )
            Text(
                text = "المخزون الحالي: ${med.currentStock} قرص",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )

            if (shortfall > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = CriticalRedLight,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CriticalRed)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ ينقصك $shortfall قرصاً قبل السفر! يرجى الشراء وتجديد الوصفة.",
                            fontWeight = FontWeight.Bold,
                            color = CriticalRedDark,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✓ المخزون الحالي كافٍ للسفر بأمان",
                    color = SuccessGreen,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * Add Prescription Dialog
 */
@Composable
fun AddPrescriptionDialog(
    members: List<FamilyMember>,
    onDismiss: () -> Unit,
    onSave: (Prescription) -> Unit
) {
    val textFieldColors = getHighContrastTextFieldColors()

    var doctorName by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
    var notes by remember { mutableStateOf("") }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantLight)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تسجيل روشتة جديدة", fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = CriticalRed)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("اسم الطبيب / العيادة", fontWeight = FontWeight.SemiBold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = { Text("التشخيص أو التخصص", fontWeight = FontWeight.SemiBold) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("الأدوية المسجلة بالروشتة", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pres = Prescription(
                        familyMemberId = selectedMemberId,
                        doctorName = doctorName.ifBlank { "طبيب استشاري" },
                        dateIssued = "2026-09-10",
                        diagnosis = diagnosis.ifBlank { null },
                        medicationsNotes = notes.ifBlank { null }
                    )
                    onSave(pres)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("حفظ الروشتة", fontWeight = FontWeight.Bold, color = Color.White)
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
