package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun HomeMedicationCard(
    medication: Medication,
    owner: FamilyMember?,
    onTakeDose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MintContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = "أيقونة الدواء",
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = medication.brandName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (owner != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = owner.name,
                                fontSize = 10.sp,
                                color = EmeraldDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${medication.dosageAmount} ${medication.dosageUnit} • ${medication.dailyDoseCount} جرعات يومياً",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                val note = if (!medication.instructions.isNullOrBlank()) medication.instructions else medication.timingRule
                if (!note.isNullOrBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldDark,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onTakeDose,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "تسجيل أخذ الجرعة",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("أخذت", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun EmptyMedicationGuideCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = "منبهات ذكية",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("منبهات ذكية لكل موعد دواء", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("تنبيه دقيق بالصوت والاهتزاز مع شاشة كاملة لضمان عدم نسيان أي جرعة.", fontSize = 12.sp, color = TextSecondary)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = "متابعة المخزون",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("متابعة رصيد العلب ونقص الأدوية", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("خصم تلقائي لكل حبة تؤخذ وتنبيهك قبل نفاد العلبة بعدة أيام لتجديدها.", fontSize = 12.sp, color = TextSecondary)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FamilyRestroom,
                        contentDescription = "رعاية الأسرة",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("رعاية أدوية الوالدين والأسرة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("متابعة أدوية كبار السن والأبناء والاطمئنان على تناولهم العلاج بانتظام.", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun HomeShortcutsCard(
    onOpenAddMedication: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToFamily: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MintContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onOpenAddMedication() }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة دواء جديد", tint = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("إضافة دواء", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldDark)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigateToShopping() }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceLight)
                        .border(1.dp, BorderLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "نواقص الصيدلية", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("نواقص الصيدلية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigateToFamily() }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceLight)
                        .border(1.dp, BorderLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Group, contentDescription = "أفراد العائلة", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("أفراد العائلة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}
