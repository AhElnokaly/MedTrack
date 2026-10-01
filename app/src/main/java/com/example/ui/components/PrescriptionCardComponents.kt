package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.Prescription
import com.example.ui.theme.*

@Composable
fun PrescriptionHeaderCard(onAddNew: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MintContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "أرشيف الروشتات الطبية 📸",
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("🩺", fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "حفظ وتوثيق الروشتات بالصورة والتاريخ لكل فرد بالأسرة.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddNew,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل روشتة جديدة", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun PrescriptionCard(
    prescription: Prescription,
    member: FamilyMember?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(prescription.doctorName ?: "روشتة عيادة", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(prescription.dateIssued, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
            Text("المريض: ${member?.name ?: "العائلة"}", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            if (!prescription.diagnosis.isNullOrBlank()) {
                Text("التشخيص: ${prescription.diagnosis}", fontSize = 12.sp, color = TextSecondary)
            }
            if (!prescription.medicationsNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MintLight,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Text(
                        text = "الأدوية: ${prescription.medicationsNotes}",
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
