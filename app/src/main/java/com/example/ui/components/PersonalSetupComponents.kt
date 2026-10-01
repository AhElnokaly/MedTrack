package com.example.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.ui.theme.*
import java.util.UUID

private const val PREFS_PROFILE = "ameen_user_profile"
private const val KEY_SETUP_DONE = "user_profile_setup_done"
private const val KEY_USER_NAME = "user_full_name"
private const val KEY_USER_BIRTH = "user_birth_date"
private const val KEY_USER_GENDER = "user_gender"

fun isUserProfileSetupDone(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_SETUP_DONE, false)
}

fun getUserDisplayName(context: Context): String {
    val prefs = context.getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE)
    return prefs.getString(KEY_USER_NAME, "أحمد") ?: "أحمد"
}

@Composable
fun PersonalSetupCard(
    onSaved: (userName: String, selectedRelations: List<Pair<String, String>>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf(getUserDisplayName(context)) }
    var birthDateOrAge by remember { mutableStateOf("1992-05-14") }
    var selectedGender by remember { mutableStateOf("ذكر") }

    // Pre-configured family relatives for quick selection
    val availableRelations = remember {
        listOf(
            "الوالد (الأب)" to "أب",
            "الوالدة (الأم)" to "أم",
            "الزوجة / الزوج" to "زوج/زوجة",
            "ابن" to "ابن",
            "ابنة" to "ابنة",
            "جد / جدة" to "جد/جدة"
        )
    }

    val selectedRelations = remember {
        mutableStateListOf(
            "الوالدة (الأم)" to "أم",
            "الزوجة / الزوج" to "زوج/زوجة"
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("personal_setup_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👋", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تسجيل بياناتك الشخصية والعائلة 🌿",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "خطوة سريعة لتنظيم أدويتك ورعاية أسرتك",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Name Input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("اسمك الكريم") },
                placeholder = { Text("مثال: أحمد مصطفى") },
                leadingIcon = {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = EmeraldPrimary)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedLabelColor = EmeraldPrimary,
                    unfocusedLabelColor = TextSecondary,
                    focusedPlaceholderColor = TextMuted,
                    unfocusedPlaceholderColor = TextMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Birth date / Age & Gender Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = birthDateOrAge,
                    onValueChange = { birthDateOrAge = it },
                    label = { Text("تاريخ الميلاد أو العمر") },
                    placeholder = { Text("1992-05-14 أو 34 سنة") },
                    leadingIcon = {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldPrimary)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceLight,
                        unfocusedContainerColor = SurfaceLight,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderLight,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = TextSecondary,
                        focusedPlaceholderColor = TextMuted,
                        unfocusedPlaceholderColor = TextMuted
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("user_birthdate_input")
                )

                // Gender Toggle
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariantLight)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("ذكر", "أنثى").forEach { gender ->
                        val isSelected = selectedGender == gender
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                .clickable { selectedGender = gender },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = gender,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Related Family Members Selection
            Text(
                text = "الأشخاص المرتبطين (اختر من ترغب برعايتهم):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availableRelations) { item ->
                    val isSelected = selectedRelations.any { it.first == item.first }
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                selectedRelations.removeAll { it.first == item.first }
                            } else {
                                selectedRelations.add(item)
                            }
                        },
                        label = {
                            Text(
                                text = item.first,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = SurfaceVariantLight,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = BorderLight,
                            selectedBorderColor = EmeraldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save & Continue Button
            Button(
                onClick = {
                    val name = fullName.trim().ifEmpty { "أحمد" }
                    val prefs = context.getSharedPreferences(PREFS_PROFILE, Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean(KEY_SETUP_DONE, true)
                        .putString(KEY_USER_NAME, name)
                        .putString(KEY_USER_BIRTH, birthDateOrAge.trim())
                        .putString(KEY_USER_GENDER, selectedGender)
                        .apply()

                    onSaved(name, selectedRelations.toList())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حفظ ومتابعة إلى الجدول الطبي 🌿",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun WelcomeMedicationSummaryCard(
    userName: String,
    medicationsCount: Int,
    onAddMedicationClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, BorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("welcome_medication_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MintContainer.copy(alpha = 0.5f),
                            SurfaceLight
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "أهلاً بك يا $userName",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🌿", fontSize = 15.sp)
                            }
                            Text(
                                text = if (medicationsCount > 0) "لديك $medicationsCount أدوية مجدولة في جدول اليوم" else "جدولك جاهز، أضف أول دواء عبر الزر بالأسفل (+)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditProfileClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل البيانات",
                            tint = EmeraldDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Action: Add Medication
                    Button(
                        onClick = onAddMedicationClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة دواء جديد", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Adherence badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceLight,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("🛡️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "البيانات محمية محلياً",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
