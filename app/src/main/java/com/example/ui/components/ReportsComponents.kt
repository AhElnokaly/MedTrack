package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DoseLog
import com.example.data.model.Medication
import com.example.ui.theme.*

@Composable
fun ComplianceSummaryCard(
    context: Context,
    todayDateString: String,
    overallComplianceText: String,
    weeklyComplianceText: String,
    totalLogs: Int,
    takenLogs: Int,
    lateLogs: Int,
    missedLogs: Int,
    medications: List<Medication>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MintContainer),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "تقرير التزام المريض للطبيب المعالج 📋",
                fontWeight = FontWeight.Bold,
                color = EmeraldDark,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "سجل موضوعي يعرض نسبة الالتزام بالعلاج، الأدوية، والجرعات الموثقة لمناقشتها في الزيارة الطبية.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                    ) {
                        Text(weeklyComplianceText, fontSize = 24.sp, fontWeight = FontWeight.Black, color = EmeraldPrimary)
                        Text("الالتزام الأسبوعي", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                    ) {
                        Text(overallComplianceText, fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        Text("الالتزام الكلي", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.weight(1f).padding(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp).fillMaxWidth()
                    ) {
                        Text("$lateLogs", fontSize = 24.sp, fontWeight = FontWeight.Black, color = WarningAmberDark)
                        Text("جرعة متأخرة", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val sb = StringBuilder()
                    sb.append("تقرير الالتزام الدوائي الطبي - تطبيق أمين (Ameen)\n")
                    sb.append("التاريخ: $todayDateString\n")
                    sb.append("نسبة الالتزام العامة المحسوبة: $overallComplianceText (الالتزام الأسبوعي: $weeklyComplianceText)\n")
                    sb.append("إحصائية الجرعات الموثقة:\n")
                    sb.append("- إجمالي الجرعات المسجلة: $totalLogs\n")
                    sb.append("- جرعات في الموعد: $takenLogs\n")
                    sb.append("- جرعات متأخرة: $lateLogs\n")
                    sb.append("- جرعات فائتة: $missedLogs\n\n")
                    sb.append("قائمة الأدوية الحالية:\n")
                    medications.forEach {
                        sb.append("- ${it.brandName} (${it.dosageAmount} ${it.dosageUnit}) - ${it.timingRule}\n")
                    }
                    sb.append("\nتم إعداد هذا التقرير آلياً لتقديمه للطبيب المعالج.")

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "مشاركة تقرير الالتزام الطبي"))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاركة تقرير الالتزام الطبي (نص مفصل)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun DoseLogItemCard(log: DoseLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("تم أخذ الجرعة بنجاح", fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(log.notes ?: "في الموعد المحدد", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
            Surface(
                color = MintContainer,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
            ) {
                Text(
                    text = log.status,
                    color = EmeraldDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
