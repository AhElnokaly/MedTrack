package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.Medication
import com.example.data.model.ScheduleEntry
import com.example.ui.theme.*

@Composable
fun ExpandableMedicationCard(
    medication: Medication,
    schedules: List<ScheduleEntry>,
    owner: FamilyMember?,
    onTakeDose: () -> Unit,
    onSnoozeDose: () -> Unit,
    onEditMedication: () -> Unit,
    onDeleteMedication: () -> Unit,
    onQuickRefill: (addedAmount: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "expand_icon_rotation")

    val dailyDose = if (medication.dailyDoseCount > 0) medication.dailyDoseCount else 1
    val daysRemaining = medication.currentStock / dailyDose
    val isLowStock = daysRemaining <= 5 && !medication.isPRN

    // Determine icon based on dosageUnit
    val unitIcon = when {
        medication.dosageUnit.contains("شراب") || medication.dosageUnit.contains("مل") -> "🥄"
        medication.dosageUnit.contains("قطر") -> "💧"
        medication.dosageUnit.contains("بخ") -> "💨"
        medication.dosageUnit.contains("حقن") || medication.dosageUnit.contains("امبول") -> "💉"
        else -> "💊"
    }

    val timesSummary = if (schedules.isNotEmpty()) {
        schedules.joinToString(" • ") { it.timeOfDay }
    } else {
        "عند اللزوم (PRN)"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(
            width = if (isExpanded) 1.5.dp else 1.dp,
            color = if (isExpanded) EmeraldPrimary else if (isLowStock) WarningAmber.copy(alpha = 0.7f) else BorderLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("expandable_med_card_${medication.id}")
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main Collapsed Row (Always visible & Clickable to toggle expand)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Leading Type Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                if (medication.isCritical) CriticalRedLight
                                else if (isLowStock) WarningAmberLight
                                else MintContainer
                            )
                            .border(
                                1.dp,
                                if (medication.isCritical) CriticalRed.copy(alpha = 0.5f)
                                else if (isLowStock) WarningAmber.copy(alpha = 0.5f)
                                else EmeraldPrimary.copy(alpha = 0.4f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(unitIcon, fontSize = 22.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = medication.brandName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            if (medication.isCritical) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = CriticalRedLight,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "حرج",
                                        color = CriticalRedDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Scheduled Time Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = EmeraldDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = timesSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Text("•", color = TextSecondary, fontSize = 12.sp)

                            // Owner Tag
                            Text(
                                text = owner?.name ?: "العائلة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Trailing: Quick Check Button & Expand Chevron
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Take Dose Button
                    IconButton(
                        onClick = onTakeDose,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .testTag("quick_take_button_${medication.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "أخذت الجرعة",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "طي" else "توسيع",
                            tint = TextPrimary,
                            modifier = Modifier.rotate(rotationState)
                        )
                    }
                }
            }

            // Expanded Details Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = BorderLight, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Medical Dosage & Rule Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "مقدار الجرعة",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "${medication.dosageAmount} ${medication.dosageUnit}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Text(
                                text = "قاعدة الطعام",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = medication.timingRule,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldDark
                            )
                        }

                        Column {
                            Text(
                                text = "طبيعة العلاج",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = if (medication.durationDays != null) "كورس ${medication.durationDays} أيام" else "علاج مستمر 🔄",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Stock & Refill Tracker Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceVariantLight,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📦", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "المخزون: ${medication.currentStock} ${medication.dosageUnit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLowStock) CriticalRedDark else TextPrimary
                                    )
                                }
                                val packagingBreakdown = formatPackagingStockBreakdown(
                                    currentStock = medication.currentStock,
                                    stripsCount = medication.stripsCount,
                                    pillsPerStrip = medication.pillsPerStrip,
                                    packageType = medication.packageType
                                )
                                if (packagingBreakdown != null) {
                                    Text(
                                        text = "العبوة: $packagingBreakdown",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = EmeraldDark,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = if (isLowStock) "⚠️ يوشك على النفاد (يكفي $daysRemaining أيام فقط)" else "يكفي لحوالي $daysRemaining يوماً",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLowStock) CriticalRedDark else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }

                            // Quick Refill (+ شريط / علبة ذكية)
                            val refillAmount = if (medication.packageType == "STRIPS" && medication.pillsPerStrip > 0) {
                                medication.pillsPerStrip
                            } else {
                                10
                            }
                            val refillLabel = if (medication.packageType == "STRIPS") "+ شريط ($refillAmount)" else "+$refillAmount"

                            OutlinedButton(
                                onClick = { onQuickRefill(refillAmount) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldDark),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(refillLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Sound / Notification Type & Packaging details
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val soundLabel = when (medication.soundType) {
                            "TTS" -> {
                                val ttsPhrase = when (medication.ttsTemplate) {
                                    "REMINDER" -> "تذكير طبي شفاك الله"
                                    "WATER" -> "مع كوب ماء"
                                    "SHORT" -> "مختصر"
                                    else -> "افتراضي"
                                }
                                "🎙️ نطق اسم الدواء والمريض ($ttsPhrase)"
                            }
                            "RECORDED" -> "🗣️ تسجيل صوتي شخصي"
                            "VIBRATE" -> "📳 اهتزاز فقط"
                            "SILENT" -> "🔕 تنبيه صامت خفيف"
                            else -> "🔔 نغمة منبه الجهاز"
                        }
                        Text(
                            text = "طريقة التنبيه: $soundLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        if (medication.packageType == "STRIPS" && medication.stripsCount > 0) {
                            Text(
                                text = "العبوة: علبة (${medication.stripsCount} شرائط • ${medication.pillsPerStrip} قرص بالشريط)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        } else if (medication.packageType == "BOTTLE") {
                            Text(
                                text = "العبوة: زجاجة شراب 🧪",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        } else if (medication.packageType == "DROPS") {
                            Text(
                                text = "العبوة: قطارة 💧",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        }

                        if (medication.recurrencePattern == "SPECIFIC_DAYS" && !medication.recurrenceDays.isNullOrBlank()) {
                            Text(
                                text = "أيام التكرار: ${medication.recurrenceDays}",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        } else if (medication.recurrencePattern == "INTERVAL") {
                            Text(
                                text = "التكرار: مرتين شهرياً 🌙",
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldDark,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Action Buttons (Take Dose, Snooze, Edit, Delete)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Main Take Dose Button
                        Button(
                            onClick = onTakeDose,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أخذت الجرعة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }

                        // Snooze Button
                        OutlinedButton(
                            onClick = onSnoozeDose,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BorderLight),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تأجيل 15د", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Edit Button
                        IconButton(
                            onClick = onEditMedication,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceVariantLight)
                                .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = EmeraldDark, modifier = Modifier.size(18.dp))
                        }

                        // Delete Button
                        IconButton(
                            onClick = onDeleteMedication,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CriticalRedLight)
                                .border(1.dp, CriticalRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = CriticalRedDark, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
