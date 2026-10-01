package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication
import com.example.ui.theme.*

@Composable
fun NextMedicationCard(
    medication: Medication,
    ownerName: String,
    onTakeDoseClick: () -> Unit,
    onTakeLateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("next_dose_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.5.dp, CardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Category & Critical Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MintLight)
                            .border(1.dp, MintSecondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💊", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "الدواء القادم ($ownerName)",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (medication.isCritical) {
                    Surface(
                        color = CriticalRedLight,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CriticalRed)
                    ) {
                        Text(
                            text = "🚨 دواء حرج",
                            color = CriticalRedDark,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = MintContainer,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = medication.timingRule,
                            color = EmeraldDark,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Brand name and dosage (High contrast)
            Text(
                text = medication.brandName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${medication.genericName ?: ""} • ${medication.dosageAmount} ${medication.dosageUnit}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Time Remaining & Stock
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantLight)
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الوقت المتبقي: 1 ساعة و 20 دقيقة",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "المخزون: ${medication.currentStock} قرص",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (medication.currentStock <= 5) CriticalRedDark else TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTakeDoseClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("take_dose_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "أخذت الجرعة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = onTakeLateClick,
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceVariantLight,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("أخذت متأخر؟", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun CaregiverReassuranceCard(
    mamaName: String,
    medicationsCount: Int,
    onCheckMamaClick: () -> Unit
) {
    val cardBg = WarningAmberLight
    val cardBorder = WarningAmber.copy(alpha = 0.6f)
    val textColor = if (AmeenTheme.isDark) WarningAmber else WarningAmberDark

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable { onCheckMamaClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.5.dp, cardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text("👵", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "دواء $mamaName اتخد؟",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "تم تناول جرعة الصباح بنجاح • متبقي جرعة المساء",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "عرض التفاصيل",
                tint = textColor
            )
        }
    }
}

@Composable
fun AiInsightCard() {
    val insightBorder = if (AmeenTheme.isDark) AmeenTheme.colors.secondary.copy(alpha = 0.5f) else Color(0xFF99F6E4)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MintContainer),
        border = BorderStroke(1.5.dp, insightBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("✨", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "رؤية من الذكاء الاصطناعي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "نومك أقل من المعتاد هذا الأسبوع بنسبة 23%، وشرب الماء أقل بـ 18%، مما قد يسبب لك إرهاق وزيادة في الرغبة بالسكريات. حافظ على رشفات منتظمة اليوم.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    lineHeight = 20.sp,
                    fontSize = 13.sp
                )
            }
        }
    }
}
