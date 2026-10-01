package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Medication
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

@Composable
fun LateDoseDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "تسجيل جرعة متأخرة",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "هل تناولت دواء ${medication.brandName} متأخراً عن موعده الأصلي؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "نظام أمين يسجل وقت الجرعة الفعلي لمساعدة طبيبك في فهم الالتزام الدوائي وضبط المخزون بدقة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarningAmber,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("نعم، أخذتها الآن", fontWeight = FontWeight.Bold, color = Color.White)
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
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع وإلغاء",
                    modifier = Modifier.size(16.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}

@Composable
fun ResetConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "مسح كافة البيانات",
                    tint = CriticalRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مسح وتصفير كافة البيانات",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Text(
                text = "سيتم مسح جميع الأدوية، المواعيد، وسجلات الجرعات للبدء من الصفر لتتمكن من إضافة البيانات بنفسك واختبار التطبيق.\n\nهل ترغب في المتابعة؟",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 20.sp,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CriticalRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("نعم، مسح الكل", fontWeight = FontWeight.Bold, color = Color.White)
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
                Text("إلغاء", fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    )
}
