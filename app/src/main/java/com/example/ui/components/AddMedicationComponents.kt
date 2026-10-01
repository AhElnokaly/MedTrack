package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun getHighContrastTextFieldColors(): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary,
        focusedContainerColor = SurfaceLight,
        unfocusedContainerColor = SurfaceLight,
        focusedBorderColor = EmeraldPrimary,
        unfocusedBorderColor = BorderLight,
        focusedLabelColor = EmeraldPrimary,
        unfocusedLabelColor = TextSecondary,
        focusedPlaceholderColor = TextMuted,
        unfocusedPlaceholderColor = TextMuted,
        focusedLeadingIconColor = EmeraldPrimary,
        unfocusedLeadingIconColor = TextSecondary,
        focusedTrailingIconColor = EmeraldPrimary,
        unfocusedTrailingIconColor = TextSecondary
    )
}

@Composable
fun AddMedicationDialogHeader(
    isEditing: Boolean,
    onDismiss: () -> Unit
) {
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
                    contentDescription = "رجوع وإلغاء",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isEditing) "تعديل بيانات الدواء ✏️" else "إضافة دواء جديد 💊",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        TextButton(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(contentColor = CriticalRed)
        ) {
            Icon(Icons.Default.Close, contentDescription = "إلغاء", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("إلغاء", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun AddMedicationActionButtons(
    isEditing: Boolean,
    isSaveEnabled: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(0.35f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, BorderLight),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = SurfaceVariantLight,
                contentColor = TextPrimary
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("إلغاء ورجوع", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
        }

        Button(
            onClick = onSave,
            enabled = isSaveEnabled,
            modifier = Modifier
                .weight(0.65f)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isEditing) "حفظ التعديلات" else "حفظ الدواء والتنبيهات",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}
