package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PackagingCalculatorSection(
    packageType: String,
    onPackageTypeChanged: (String) -> Unit,
    boxCount: String,
    onBoxCountChanged: (String) -> Unit,
    extraStripsCount: String = "0",
    onExtraStripsCountChanged: (String) -> Unit = {},
    extraPillsCount: String = "0",
    onExtraPillsCountChanged: (String) -> Unit = {},
    stripsPerBox: String,
    onStripsPerBoxChanged: (String) -> Unit,
    pillsPerStrip: String,
    onPillsPerStripChanged: (String) -> Unit,
    calculatedTotal: Int,
    onCalculatedTotalApplied: (Int) -> Unit
) {
    val textFieldColors = getHighContrastTextFieldColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = "أيقونة المخزون",
                        tint = EmeraldDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "طريقة احتساب المخزون والعبوة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "حدد هل هي علبة بشرائط، زجاجة شراب، قطارة، أو عدد مباشر",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val types = listOf(
                    Triple("STRIPS", "علبة بشرائط 💊", "أقراص"),
                    Triple("BOTTLE", "زجاجة شراب 🧪", "مل"),
                    Triple("DROPS", "قطارة 💧", "نقط"),
                    Triple("DIRECT", "إدخال مباشر 🔢", "وحدات")
                )

                types.forEach { (typeKey, title, _) ->
                    val isSelected = packageType == typeKey
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onPackageTypeChanged(typeKey) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) EmeraldPrimary else SurfaceVariantLight,
                        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else BorderLight)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 2.dp),
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (packageType) {
                "STRIPS" -> {
                    // Definition of the packaging structure
                    Text(
                        text = "1. مواصفات العلبة (حجم العبوة):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = stripsPerBox,
                            onValueChange = { onStripsPerBoxChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("شرائط بالعلبة", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = pillsPerStrip,
                            onValueChange = { onPillsPerStripChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("أقراص بالشريط", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // What you currently have in hand (Boxes + Loose Strips + Loose Pills)
                    Text(
                        text = "2. ما تملكه حالياً في المنزل (علب كاملة / شرائط / أقراص متبقية):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = boxCount,
                            onValueChange = { onBoxCountChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("علب كاملة", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = extraStripsCount,
                            onValueChange = { onExtraStripsCountChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("+ شرائط منفردة", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = extraPillsCount,
                            onValueChange = { onExtraPillsCountChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("+ حبات متبقية", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val b = boxCount.toIntOrNull() ?: 0
                    val es = extraStripsCount.toIntOrNull() ?: 0
                    val ep = extraPillsCount.toIntOrNull() ?: 0
                    val s = stripsPerBox.toIntOrNull() ?: 2
                    val p = pillsPerStrip.toIntOrNull() ?: 10
                    val total = (b * s * p) + (es * p) + ep

                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "الإجمالي المحسوب: $total قرصاً",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "($b علبة + $es شريط + $ep قرص فردي)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Button(
                                onClick = { onCalculatedTotalApplied(total) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("تطبيق للرصيد", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                "BOTTLE" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = boxCount,
                            onValueChange = { onBoxCountChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("عدد الزجاجات", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = stripsPerBox,
                            onValueChange = { onStripsPerBoxChanged(it.filter { c -> c.isDigit() }) },
                            label = { Text("سعة الزجاجة (مل)", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = textFieldColors
                        )
                    }
                    val totalMl = (boxCount.toIntOrNull() ?: 1) * (stripsPerBox.toIntOrNull() ?: 100)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "المخزون الإجمالي: $totalMl مل من الشراب",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldDark
                    )
                }

                "DROPS" -> {
                    OutlinedTextField(
                        value = boxCount,
                        onValueChange = { onBoxCountChanged(it.filter { c -> c.isDigit() }) },
                        label = { Text("عدد القطارات المتوفرة", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = textFieldColors
                    )
                }

                "DIRECT" -> {
                    Text(
                        text = "يمكنك كتابة عدد الأقراص أو الوحدات الإجمالية مباشرة في خانة المخزون أدناه.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
