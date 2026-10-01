package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.RecordVoiceOver
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
fun MedicationOptionsSection(
    isCritical: Boolean,
    onCriticalChange: (Boolean) -> Unit,
    isPRN: Boolean,
    onPRNChange: (Boolean) -> Unit,
    autoAddToShoppingList: Boolean,
    onAutoAddToShoppingListChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderLight)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "دواء حرج (ضغط / سكر / قلب) 🚨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CriticalRedDark
                    )
                    Text(
                        text = "تفعيل التنبيه التصعيدي الحرج (0د ثم 15د ثم 30د)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isCritical,
                    onCheckedChange = onCriticalChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CriticalRed
                    )
                )
            }

            HorizontalDivider(color = BorderLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "دواء طوارئ (عند اللزوم PRN)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "مسكن، بخاخ، أو حساسية بدون جدول يومي ثابت",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isPRN,
                    onCheckedChange = onPRNChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldPrimary
                    )
                )
            }

            HorizontalDivider(color = BorderLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "إضافة تلقائية لقائمة الشراء عند النقص",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "ينبهك ويضيف الدواء لقائمة النواقص لما يتبقى 5 أيام فقط",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = autoAddToShoppingList,
                    onCheckedChange = onAutoAddToShoppingListChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun TreatmentDurationSection(
    isChronic: Boolean,
    onChronicChanged: (Boolean) -> Unit,
    durationDays: String,
    onDurationDaysChanged: (String) -> Unit,
    recurrencePattern: String,
    onRecurrencePatternChanged: (String) -> Unit,
    selectedDays: Set<String>,
    onDaysChanged: (Set<String>) -> Unit
) {
    val textFieldColors = getHighContrastTextFieldColors()
    val allWeekDays = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val courseBlue = if (AmeenTheme.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
            val courseBlueBg = if (AmeenTheme.isDark) Color(0xFF075985) else Color(0xFFE0F2FE)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(courseBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.HourglassBottom,
                        contentDescription = "مدة الكورس",
                        tint = courseBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "مدة الكورس العلاجي وجدول الأيام",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "تتوقف التنبيهات تلقائياً عند انتهاء مدة الكورس المؤقت ⏱️",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onChronicChanged(true) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isChronic) EmeraldPrimary else SurfaceVariantLight,
                    border = BorderStroke(1.dp, if (isChronic) EmeraldPrimary else BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AllInclusive,
                            contentDescription = "دواء مستمر",
                            tint = if (isChronic) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "دواء مزمن / مستمر",
                            fontWeight = if (isChronic) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChronic) Color.White else TextPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "بدون تاريخ انتهاء",
                            fontSize = 10.sp,
                            color = if (isChronic) Color.White.copy(alpha = 0.8f) else TextSecondary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onChronicChanged(false) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!isChronic) courseBlue else SurfaceVariantLight,
                    border = BorderStroke(1.dp, if (!isChronic) courseBlue else BorderLight)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "كورس مؤقت",
                            tint = if (!isChronic) Color.White else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "كورس مؤقت بأيام",
                            fontWeight = if (!isChronic) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isChronic) Color.White else TextPrimary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "إيقاف تلقائي للتنبيه",
                            fontSize = 10.sp,
                            color = if (!isChronic) Color.White.copy(alpha = 0.8f) else TextSecondary
                        )
                    }
                }
            }

            if (!isChronic) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "عدد أيام الكورس (أزرار سريعة أو إدخال مخصص):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("3", "5", "7", "10", "14").forEach { days ->
                        val isSelected = durationDays == days
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onDurationDaysChanged(days) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) courseBlue else SurfaceVariantLight,
                            border = BorderStroke(1.dp, if (isSelected) courseBlue else BorderLight)
                        ) {
                            Text(
                                text = "$days أيام",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = durationDays,
                    onValueChange = { onDurationDaysChanged(it.filter { c -> c.isDigit() }) },
                    label = { Text("أو اكتب عدد الأيام تحديداً", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = textFieldColors
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "تكرار أخذ الجرعات:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val patterns = listOf(
                    "DAILY" to "كل يوم ☀️",
                    "SPECIFIC_DAYS" to "أيام محددة 📅",
                    "INTERVAL" to "مرتين شهرياً 🌙"
                )
                patterns.forEach { (patternKey, label) ->
                    val isSelected = recurrencePattern == patternKey
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onRecurrencePatternChanged(patternKey) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) EmeraldPrimary else SurfaceVariantLight,
                        border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else BorderLight)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            if (recurrencePattern == "SPECIFIC_DAYS") {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "اختر الأيام (مثلاً 3 أيام في الأسبوع):",
                    fontSize = 11.5.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allWeekDays.forEach { day ->
                        val isDaySelected = selectedDays.contains(day)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isDaySelected) {
                                        onDaysChanged(selectedDays - day)
                                    } else {
                                        onDaysChanged(selectedDays + day)
                                    }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDaySelected) EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(1.dp, if (isDaySelected) EmeraldPrimary else BorderLight)
                        ) {
                            Text(
                                text = day.take(3),
                                fontSize = 10.sp,
                                fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDaySelected) Color.White else TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TtsTemplateSelectorSection(
    medicationName: String,
    memberName: String,
    selectedTemplate: String,
    onTemplateSelected: (String) -> Unit
) {
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
                        Icons.Default.RecordVoiceOver,
                        contentDescription = "صيغة القراءة الصوتية",
                        tint = EmeraldDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "صيغة القراءة الصوتية لاسم الدواء",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "اختر النص الجاهز الذي سينطقه التطبيق عند موعد الجرعة",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val drug = if (medicationName.isBlank()) "الدواء" else medicationName
            val member = if (memberName.isBlank()) "المريض" else memberName

            val templates = listOf(
                "DEFAULT" to "حان موعد دواء $drug لـ $member",
                "REMINDER" to "تذكير طبي: يرجى تناول دواء $drug في موعده شفاك الله",
                "WATER" to "فضلاً تذكر أخذ دواء $drug لـ $member مع كوب من الماء",
                "SHORT" to "موعد دواء $drug"
            )

            templates.forEach { (key, sampleText) ->
                val isSelected = selectedTemplate == key
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onTemplateSelected(key) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MintContainer else SurfaceVariantLight,
                    border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else BorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onTemplateSelected(key) },
                            colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sampleText,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
