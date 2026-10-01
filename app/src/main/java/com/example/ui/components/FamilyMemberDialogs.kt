package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.ui.theme.*

@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onAdd: (FamilyMember) -> Unit
) {
    val textFieldColors = getHighContrastTextFieldColors()

    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("طفل") }
    var ageStr by remember { mutableStateOf("7") }
    var gender by remember { mutableStateOf("ذكر") }
    var weightStr by remember { mutableStateOf("25") }
    var heightStr by remember { mutableStateOf("120") }
    var healthNotes by remember { mutableStateOf("") }
    var isGuardian by remember { mutableStateOf(false) }

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
                    Text(
                        text = "إضافة فرد جديد للأسرة",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextSecondary)
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                }
                item {
                    Text("صلة القرابة:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("أنا", "أب", "أم", "زوجة", "طفل", "جد").forEach { rel ->
                            val isSelected = relation == rel
                            FilterChip(
                                selected = isSelected,
                                onClick = { relation = rel },
                                label = {
                                    Text(
                                        text = rel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceVariantLight,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = BorderLight,
                                    selectedBorderColor = EmeraldPrimary,
                                    borderWidth = 1.5.dp
                                )
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("العمر (سنة)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("النوع (ذكر/أنثى)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("الوزن (كجم)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text("الطول (سم)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = healthNotes,
                        onValueChange = { healthNotes = it },
                        label = { Text("الحالة الصحية وملاحظات الأمراض", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                }
                item {
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ولي أمر 👑 (صلاحيات كاملة)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Checkbox(
                                checked = isGuardian,
                                onCheckedChange = { isGuardian = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmeraldPrimary,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val avatarColors = listOf("#059669", "#0284C7", "#D97706", "#DC2626", "#7C3AED", "#DB2777")
                    val member = FamilyMember(
                        name = name.trim(),
                        relation = relation,
                        age = ageStr.toIntOrNull() ?: 10,
                        gender = gender,
                        weightKg = weightStr.toDoubleOrNull(),
                        heightCm = heightStr.toDoubleOrNull(),
                        healthNotes = healthNotes.ifBlank { null },
                        avatarColorHex = avatarColors.random(),
                        isGuardian = isGuardian
                    )
                    onAdd(member)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة الفرد", fontWeight = FontWeight.Bold, color = Color.White)
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
                Spacer(modifier = Modifier.width(6.dp))
                Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}

@Composable
fun EditFamilyMemberDialog(
    member: FamilyMember,
    onDismiss: () -> Unit,
    onSave: (FamilyMember) -> Unit
) {
    val textFieldColors = getHighContrastTextFieldColors()

    var name by remember { mutableStateOf(member.name) }
    var relation by remember { mutableStateOf(member.relation) }
    var ageStr by remember { mutableStateOf(member.age.toString()) }
    var gender by remember { mutableStateOf(member.gender) }
    var weightStr by remember { mutableStateOf(member.weightKg?.toString() ?: "") }
    var heightStr by remember { mutableStateOf(member.heightCm?.toString() ?: "") }
    var healthNotes by remember { mutableStateOf(member.healthNotes ?: "") }
    var isGuardian by remember { mutableStateOf(member.isGuardian) }

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
                    Text(
                        text = "تعديل بيانات: ${member.name}",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 17.sp
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextSecondary)
                }
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                }
                item {
                    Text("صلة القرابة:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("أنا", "أب", "أم", "زوجة", "زوج", "طفل").forEach { rel ->
                            val isSelected = relation == rel
                            FilterChip(
                                selected = isSelected,
                                onClick = { relation = rel },
                                label = {
                                    Text(
                                        text = rel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextPrimary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = SurfaceVariantLight,
                                    labelColor = TextPrimary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = BorderLight,
                                    selectedBorderColor = EmeraldPrimary,
                                    borderWidth = 1.5.dp
                                )
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = { Text("العمر (سنة)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("النوع (ذكر/أنثى)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("الوزن (كجم)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text("الطول (سم)", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = textFieldColors
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = healthNotes,
                        onValueChange = { healthNotes = it },
                        label = { Text("الحالة الصحية وملاحظات الأمراض", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                }
                item {
                    Surface(
                        color = SurfaceVariantLight,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ولي أمر 👑 (صلاحيات كاملة)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Checkbox(
                                checked = isGuardian,
                                onCheckedChange = { isGuardian = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = EmeraldPrimary,
                                    checkmarkColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) return@Button
                    val updated = member.copy(
                        name = name.trim(),
                        relation = relation,
                        age = ageStr.toIntOrNull() ?: member.age,
                        gender = gender,
                        weightKg = weightStr.toDoubleOrNull(),
                        heightCm = heightStr.toDoubleOrNull(),
                        healthNotes = healthNotes.ifBlank { null },
                        isGuardian = isGuardian
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ التعديلات", fontWeight = FontWeight.Bold, color = Color.White)
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
                Spacer(modifier = Modifier.width(6.dp))
                Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}
