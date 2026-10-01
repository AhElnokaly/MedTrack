package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication
import com.example.data.model.ShoppingListItem
import com.example.ui.theme.*

/**
 * Dialog to record medication purchase quantity when marking an item as bought from the shopping list.
 * Supports:
 * - Full Boxes (علب كاملة)
 * - Single Strips (شرائط منفردة)
 * - Loose Pills / Units (أقراص فردية)
 * - Direct total unit entry (إدخال مباشر)
 */
@Composable
fun PurchaseQuantityDialog(
    item: ShoppingListItem,
    medication: Medication?,
    onDismiss: () -> Unit,
    onConfirmPurchase: (addedQuantity: Int) -> Unit
) {
    val isStrips = medication?.packageType == "STRIPS" || medication == null
    val stripsPerBox = (medication?.stripsCount ?: 2).coerceAtLeast(1)
    val pillsPerStrip = (medication?.pillsPerStrip ?: 10).coerceAtLeast(1)

    // Purchase mode: "CALCULATOR" (Boxes, strips, pills) or "DIRECT" (Units)
    var purchaseMode by remember { mutableStateOf(if (isStrips) "CALCULATOR" else "DIRECT") }

    // Inputs
    var boxCountStr by remember { mutableStateOf(item.neededQuantity.coerceAtLeast(1).toString()) }
    var stripCountStr by remember { mutableStateOf("0") }
    var loosePillsStr by remember { mutableStateOf("0") }

    val defaultDirectStock = if (isStrips) {
        item.neededQuantity * stripsPerBox * pillsPerStrip
    } else {
        item.neededQuantity
    }
    var directQuantityStr by remember { mutableStateOf(defaultDirectStock.coerceAtLeast(1).toString()) }

    val calculatedTotalUnits = run {
        val b = boxCountStr.toIntOrNull() ?: 0
        val s = stripCountStr.toIntOrNull() ?: 0
        val p = loosePillsStr.toIntOrNull() ?: 0
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل شراء: ${item.medicationName}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "حدد الكمية التي قمت بشرائها فعلياً لتحديث مخزون الدواء فوراً في صيدليتك:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                if (isStrips) {
                    // Mode tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { purchaseMode = "CALCULATOR" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (purchaseMode == "CALCULATOR") EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(1.dp, if (purchaseMode == "CALCULATOR") EmeraldPrimary else BorderLight)
                        ) {
                            Text(
                                text = "علبة / شريط / أقراص 💊",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (purchaseMode == "CALCULATOR") FontWeight.Bold else FontWeight.Medium,
                                color = if (purchaseMode == "CALCULATOR") Color.White else TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { purchaseMode = "DIRECT" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (purchaseMode == "DIRECT") EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(1.dp, if (purchaseMode == "DIRECT") EmeraldPrimary else BorderLight)
                        ) {
                            Text(
                                text = "عدد أقراص مباشر 🔢",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (purchaseMode == "DIRECT") FontWeight.Bold else FontWeight.Medium,
                                color = if (purchaseMode == "DIRECT") Color.White else TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                if (purchaseMode == "CALCULATOR" && isStrips) {
                    Text(
                        text = "العلبة القياسية: $stripsPerBox شرائط × $pillsPerStrip قرص",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = boxCountStr,
                            onValueChange = { boxCountStr = it.filter { c -> c.isDigit() } },
                            label = { Text("علب كاملة", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = stripCountStr,
                            onValueChange = { stripCountStr = it.filter { c -> c.isDigit() } },
                            label = { Text("+ شرائط", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = loosePillsStr,
                            onValueChange = { loosePillsStr = it.filter { c -> c.isDigit() } },
                            label = { Text("+ أقراص", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                    }

                    // Calculation breakdown preview
                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "سيتم إضافة: $calculatedTotalUnits قرصاً للمخزون",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                            val current = medication?.currentStock ?: 0
                            val newStock = current + calculatedTotalUnits
                            Text(
                                text = "المخزون الحالي: $current ⭢ سيصبح: $newStock قرصاً",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    // Direct entry mode
                    Text(
                        text = "أدخل إجمالي الوحدات/الأقراص المشتراة:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = directQuantityStr,
                        onValueChange = { directQuantityStr = it.filter { c -> c.isDigit() } },
                        label = { Text("الكمية المشتراة (أقراص / وحدات)", fontWeight = FontWeight.SemiBold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                    val directTotal = directQuantityStr.toIntOrNull() ?: 0
                    val current = medication?.currentStock ?: 0
                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "المخزون الحالي: $current ⭢ بعد الإضافة: ${current + directTotal}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldDark,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUnitsToAdd = if (purchaseMode == "CALCULATOR" && isStrips) {
                        calculatedTotalUnits
                    } else {
                        directQuantityStr.toIntOrNull() ?: 0
                    }
                    if (finalUnitsToAdd > 0) {
                        onConfirmPurchase(finalUnitsToAdd)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تأكيد الشراء وتحديث المخزون", fontWeight = FontWeight.Bold, color = Color.White)
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
                Text("إلغاء", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}
