package com.example.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
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
import com.example.data.model.FamilyMember
import com.example.ui.theme.*

/**
 * Format 24-hour time string ("HH:mm") into user-friendly Arabic 12-hour format ("hh:mm ص/م")
 */
fun formatTimeArabic(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1].toIntOrNull() ?: return time
    val period = if (hour < 12) "ص" else "م"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(java.util.Locale.US, "%02d:%02d %s", displayHour, minute, period)
}

/**
 * Automatically distribute dose times across 24 hours based on frequency and base start time.
 * E.g. start 00:00 (12 AM) with frequency 2 -> 00:00, 12:00
 * E.g. start 00:00 (12 AM) with frequency 3 -> 00:00, 08:00, 16:00
 */
fun calculateDistributedTimes(startHour: Int, startMinute: Int, frequency: Int): List<String> {
    val validFreq = frequency.coerceIn(1, 12)
    val intervalHours = 24 / validFreq
    val times = mutableListOf<String>()
    for (i in 0 until validFreq) {
        val h = (startHour + (i * intervalHours)) % 24
        times.add(String.format(java.util.Locale.US, "%02d:%02d", h, startMinute))
    }
    return times
}

@Composable
fun FamilyMemberSelectorSection(
    members: List<FamilyMember>,
    selectedMemberId: String,
    onMemberSelected: (String) -> Unit
) {
    Text(
        text = "لمن هذا الدواء؟ *",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(members, key = { it.id }) { member ->
            val isSelected = member.id == selectedMemberId
            FilterChip(
                selected = isSelected,
                onClick = { onMemberSelected(member.id) },
                label = {
                    Text(
                        text = "${member.name} (${member.relation})",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun TimingRuleSelectorSection(
    timingRule: String,
    onTimingRuleSelected: (String) -> Unit
) {
    val timingRules = listOf(
        "مع الأكل",
        "قبل الأكل",
        "بعد الأكل",
        "على معدة فارغة",
        "قبل النوم",
        "صباحاً على الريق"
    )

    Text(
        text = "إرشادات التناول المرتبطة بالطعام",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(timingRules, key = { it }) { rule ->
            val isSelected = timingRule == rule
            FilterChip(
                selected = isSelected,
                onClick = { onTimingRuleSelected(rule) },
                label = {
                    Text(
                        text = rule,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

/**
 * Enhanced Daily Times & Alarm Scheduling Section.
 * Solves:
 * 1. Selecting base starting alarm time via native TimePicker.
 * 2. Automatic distribution of alarms when taking medicine multiple times/day (e.g. 12 AM -> 12 AM, 12 PM; or 12, 8, 16).
 * 3. Individual inspection and editing of each alarm time via TimePicker.
 * 4. Adding custom additional dose times and removing individual dose times.
 */
@Composable
fun DailyTimesSection(
    timesOfDay: List<String>,
    onTimesChanged: (List<String>) -> Unit
) {
    val context = LocalContext.current

    // Extract first dose or default to 08:00
    val firstTime = timesOfDay.firstOrNull() ?: "08:00"
    val firstParts = firstTime.split(":")
    var baseHour by remember(firstTime) { mutableIntStateOf(firstParts.getOrNull(0)?.toIntOrNull() ?: 8) }
    var baseMinute by remember(firstTime) { mutableIntStateOf(firstParts.getOrNull(1)?.toIntOrNull() ?: 0) }

    val frequencies = listOf(
        1 to "مرة واحدة",
        2 to "مرتان (كل 12 س)",
        3 to "3 مرات (كل 8 س)",
        4 to "4 مرات (كل 6 س)"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceVariantLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ضبط مواعيد منبه الجرعات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    color = MintContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${timesOfDay.size} منبهات يومياً",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step 1: Base time selector & Quick Auto-Distribution Trigger
            Text(
                text = "1. وقت أول جرعة (المنبه الأساسي):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to pick base time
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                baseHour = hourOfDay
                                baseMinute = minute
                                // Automatically recalculate distributed times based on current frequency
                                val currentCount = timesOfDay.size.coerceIn(1, 6)
                                val recalculated = calculateDistributedTimes(hourOfDay, minute, currentCount)
                                onTimesChanged(recalculated)
                            },
                            baseHour,
                            baseMinute,
                            false
                        ).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceLight,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimeArabic(String.format(java.util.Locale.US, "%02d:%02d", baseHour, baseMinute)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(تغيير)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 2: Frequency Selector (Auto-calculates intervals)
            Text(
                text = "2. عدد مرات تناول الدواء باليوم (ضبط تلقائي للمنبهات):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                frequencies.forEach { (count, label) ->
                    val isSelected = timesOfDay.size == count
                    OutlinedButton(
                        onClick = {
                            val newTimes = calculateDistributedTimes(baseHour, baseMinute, count)
                            onTimesChanged(newTimes)
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else BorderLight),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MintContainer else SurfaceLight,
                            contentColor = if (isSelected) EmeraldDark else TextPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$count مرات",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Text(
                                text = if (count == 1) "كل 24س" else if (count == 2) "كل 12س" else if (count == 3) "كل 8س" else "كل 6س",
                                fontSize = 9.sp,
                                color = if (isSelected) EmeraldDark else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 3: List of individual alarm times with independent Edit & Delete
            Text(
                text = "3. مواعيد المنبهات المحددة (اضغط لتعديل أي وقت على حدة):",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timesOfDay.forEachIndexed { index, timeStr ->
                    val parts = timeStr.split(":")
                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Allow tapping entire row to edit time
                                TimePickerDialog(
                                    context,
                                    { _, newHour, newMinute ->
                                        val formatted = String.format(java.util.Locale.US, "%02d:%02d", newHour, newMinute)
                                        val mutable = timesOfDay.toMutableList()
                                        mutable[index] = formatted
                                        onTimesChanged(mutable)
                                    },
                                    h,
                                    m,
                                    false
                                ).show()
                            },
                        color = SurfaceLight,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MintContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = EmeraldDark
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "الجرعة ${index + 1}",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${formatTimeArabic(timeStr)} ($timeStr)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Edit button
                                IconButton(
                                    onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, newHour, newMinute ->
                                                val formatted = String.format(java.util.Locale.US, "%02d:%02d", newHour, newMinute)
                                                val mutable = timesOfDay.toMutableList()
                                                mutable[index] = formatted
                                                onTimesChanged(mutable)
                                            },
                                            h,
                                            m,
                                            false
                                        ).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل الموعد",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Delete button (if more than 1)
                                if (timesOfDay.size > 1) {
                                    IconButton(
                                        onClick = {
                                            val mutable = timesOfDay.toMutableList()
                                            mutable.removeAt(index)
                                            onTimesChanged(mutable)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف الموعد",
                                            tint = CoralRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Add extra custom dose time button
                OutlinedButton(
                    onClick = {
                        val lastTime = timesOfDay.lastOrNull() ?: "08:00"
                        val p = lastTime.split(":")
                        val lastH = p.getOrNull(0)?.toIntOrNull() ?: 8
                        val lastM = p.getOrNull(1)?.toIntOrNull() ?: 0
                        TimePickerDialog(
                            context,
                            { _, newHour, newMinute ->
                                val formatted = String.format(java.util.Locale.US, "%02d:%02d", newHour, newMinute)
                                val mutable = timesOfDay.toMutableList()
                                if (!mutable.contains(formatted)) {
                                    mutable.add(formatted)
                                    mutable.sort()
                                    onTimesChanged(mutable)
                                }
                            },
                            (lastH + 4) % 24,
                            lastM,
                            false
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldDark
                    ),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = EmeraldDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إضافة موعد جرعة إضافي مخصص",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
