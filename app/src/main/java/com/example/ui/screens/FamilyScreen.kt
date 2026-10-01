package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FamilyMember
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

@Composable
fun FamilyScreen(
    viewModel: MedicationViewModel
) {
    val context = LocalContext.current
    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val medications by viewModel.allMedications.collectAsStateWithLifecycle()

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEdit by remember { mutableStateOf<FamilyMember?>(null) }
    var memberToUpdateMeasurements by remember { mutableStateOf<FamilyMember?>(null) }
    var memberToDelete by remember { mutableStateOf<FamilyMember?>(null) }

    Scaffold(
        containerColor = AmeenTheme.colors.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddMemberDialog = true },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إضافة فرد للأسرة", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "ملفات العائلة الصحية",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "إدارة مستقلة لكل فرد، متابعة الوزن والنمو، وصلاحيات الرعاية الأسرية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            items(members, key = { it.id }) { member ->
                val memberMeds = medications.filter { it.familyMemberId == member.id }
                val isChildOrElderly = member.age < 15 || member.age > 60
                val needsGrowthUpdate = isChildOrElderly && (member.lastGrowthCheck == null ||
                        (System.currentTimeMillis() - member.lastGrowthCheck) > (30L * 24 * 60 * 60 * 1000))

                FamilyMemberCard(
                    member = member,
                    medications = memberMeds,
                    needsGrowthUpdate = needsGrowthUpdate,
                    onEdit = { memberToEdit = member },
                    onUpdateMeasurements = { memberToUpdateMeasurements = member },
                    onDelete = { memberToDelete = member }
                )
            }
        }
    }

    // 1. Add Member Dialog
    if (showAddMemberDialog) {
        AddFamilyMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAdd = { member ->
                viewModel.addFamilyMember(member)
                showAddMemberDialog = false
                Toast.makeText(context, "تمت إضافة ملف ${member.name} بنجاح ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Edit Member Dialog
    if (memberToEdit != null) {
        EditFamilyMemberDialog(
            member = memberToEdit!!,
            onDismiss = { memberToEdit = null },
            onSave = { updatedMember ->
                viewModel.updateFamilyMember(updatedMember)
                memberToEdit = null
                Toast.makeText(context, "تم حفظ تعديلات بيانات ${updatedMember.name} بنجاح ✓", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Update Weight & Height Dialog
    if (memberToUpdateMeasurements != null) {
        val mem = memberToUpdateMeasurements!!
        var weightStr by remember { mutableStateOf(mem.weightKg?.toString() ?: "70") }
        var heightStr by remember { mutableStateOf(mem.heightCm?.toString() ?: "170") }

        AlertDialog(
            onDismissRequest = { memberToUpdateMeasurements = null },
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MintLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚖️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "تحديث قياسات النمو لـ ${mem.name}",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = { memberToUpdateMeasurements = null }) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "متابعة القياسات الدورية تساعد في ضبط جرعات الأدوية والمضادات الحيوية والمؤشرات بدقة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("الوزن الحالي (كجم)", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = getHighContrastTextFieldColors()
                    )
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text("الطول الحالي (سم)", fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = getHighContrastTextFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weightStr.toDoubleOrNull() ?: mem.weightKg
                        val h = heightStr.toDoubleOrNull() ?: mem.heightCm
                        viewModel.updateFamilyMember(
                            mem.copy(
                                weightKg = w,
                                heightCm = h,
                                lastGrowthCheck = System.currentTimeMillis()
                            )
                        )
                        memberToUpdateMeasurements = null
                        Toast.makeText(context, "تم تحديث قياسات ${mem.name} ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ القياسات", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { memberToUpdateMeasurements = null },
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

    // 4. Delete Member Confirmation Dialog
    if (memberToDelete != null) {
        val member = memberToDelete!!
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            containerColor = SurfaceLight,
            titleContentColor = CriticalRedDark,
            textContentColor = TextPrimary,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "تأكيد حذف ملف الفرد",
                    fontWeight = FontWeight.Bold,
                    color = CriticalRedDark
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف ملف '${member.name}'؟ سيؤدي ذلك لحذف سجله الصحي وكافة أدوية الفرد المسجلة.",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFamilyMember(member)
                        memberToDelete = null
                        Toast.makeText(context, "تم حذف ملف ${member.name} ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CriticalRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نعم، حذف الملف", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { memberToDelete = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, BorderLight),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = SurfaceLight,
                        contentColor = TextSecondary
                    )
                ) {
                    Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, color = TextSecondary)
                }
            }
        )
    }
}
