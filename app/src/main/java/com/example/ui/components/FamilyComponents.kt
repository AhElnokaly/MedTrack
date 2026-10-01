package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.Medication
import com.example.ui.theme.*

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    medications: List<Medication>,
    needsGrowthUpdate: Boolean,
    onEdit: () -> Unit,
    onUpdateMeasurements: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Member Row Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                try {
                                    Color(android.graphics.Color.parseColor(member.avatarColorHex))
                                } catch (_: Exception) {
                                    EmeraldPrimary
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MintContainer,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = member.relation,
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "العمر: ${member.age} سنة • ${member.gender}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Action Buttons: Guardian badge + Edit + Delete
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (member.isGuardian) {
                        Surface(
                            color = WarningAmberLight,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, WarningAmber)
                        ) {
                            Text(
                                text = "ولي أمر 👑",
                                color = if (AmeenTheme.isDark) WarningAmber else WarningAmberDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Edit Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MintLight)
                    ) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "تعديل البيانات والاسم",
                            tint = EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (!member.isGuardian) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CriticalRedLight)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "حذف الملف",
                                tint = CriticalRed,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Metrics: Weight & Height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantLight)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⚖️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الوزن: ${member.weightKg?.let { "$it كجم" } ?: "غير مسجل"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("📏", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الطول: ${member.heightCm?.let { "$it سم" } ?: "غير مسجل"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onUpdateMeasurements,
                    colors = ButtonDefaults.textButtonColors(contentColor = EmeraldPrimary)
                ) {
                    Text(
                        text = "تحديث القياس ✎",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Growth update warning
            if (needsGrowthUpdate) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = WarningAmberLight,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, WarningAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مر أكثر من 30 يوماً منذ آخر قياس لـ ${member.name}. يرجى التحديث لملاءمة جرعات الأدوية.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmberDark
                        )
                    }
                }
            }

            // Health Notes
            if (!member.healthNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = SurfaceVariantLight,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🩺", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الحالة الصحية: ${member.healthNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active medications list for this member
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأدوية المسجلة (${medications.size}):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            if (medications.isEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "لا توجد أدوية مسجلة حالياً لهذا الفرد",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    medications.forEach { med ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariantLight)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${med.brandName} (${med.dosageAmount} ${med.dosageUnit})",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مخزون: ${med.currentStock}",
                                fontSize = 12.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
