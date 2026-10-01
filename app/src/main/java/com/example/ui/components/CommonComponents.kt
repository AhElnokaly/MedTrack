package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    androidx.compose.material3.Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CircularProgressDisplay(
    percentage: Int,
    label: String,
    modifier: Modifier = Modifier,
    subLabel: String? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(110.dp)
    ) {
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = EmeraldPrimary,
            trackColor = MintLight,
            strokeWidth = 10.dp
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Formats inventory stock into friendly Egyptian/Arabic packaging units:
 * e.g. "1 علبة + 1 شريط + 4 أقراص" or "2 شريط + 3 أقراص"
 */
fun formatPackagingStockBreakdown(
    currentStock: Int,
    stripsCount: Int,
    pillsPerStrip: Int,
    packageType: String
): String? {
    if (packageType != "STRIPS" || pillsPerStrip <= 0) return null
    val stripsPerBox = stripsCount.coerceAtLeast(1)
    val pillsPerBox = stripsPerBox * pillsPerStrip

    val boxes = currentStock / pillsPerBox
    val remAfterBoxes = currentStock % pillsPerBox
    val strips = remAfterBoxes / pillsPerStrip
    val loosePills = remAfterBoxes % pillsPerStrip

    val parts = mutableListOf<String>()
    if (boxes > 0) parts.add("$boxes علبة")
    if (strips > 0) parts.add("$strips شريط")
    if (loosePills > 0) parts.add("$loosePills قرص")

    return if (parts.isNotEmpty()) parts.joinToString(" + ") else "0 قرص"
}
