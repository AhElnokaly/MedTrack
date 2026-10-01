package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.ui.theme.*

@Composable
fun HeaderBar(
    userName: String,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onNotificationClick: () -> Unit,
    onResetDataClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // User Avatar & Greeting
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AmeenTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مرحباً $userName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmeenTheme.colors.textPrimary,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("🌿", fontSize = 15.sp)
                }
                Text(
                    text = "كيف صحتك وصحة عائلتك اليوم؟",
                    style = MaterialTheme.typography.bodySmall,
                    color = AmeenTheme.colors.textSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Actions: Dark Mode Toggle, Clear Data & Notification Bell
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Dark Mode Toggle Button
            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AmeenTheme.colors.surface)
                    .border(1.dp, AmeenTheme.colors.border, CircleShape)
                    .testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = if (isDarkMode) "تفعيل المظهر الفاتح" else "تفعيل المظهر الداكن",
                    tint = if (isDarkMode) WarningAmber else AmeenTheme.colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onResetDataClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AmeenTheme.colors.surface)
                    .border(1.dp, AmeenTheme.colors.border, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "مسح وتصفير السجلات للبدء من جديد",
                    tint = AmeenTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AmeenTheme.colors.surface)
                    .border(1.dp, AmeenTheme.colors.border, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "التنبيهات",
                    tint = AmeenTheme.colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun FamilyFilterChips(
    members: List<FamilyMember>,
    selectedMemberId: String?,
    onSelectMember: (String?) -> Unit,
    onAddMemberClick: () -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            val isSelected = selectedMemberId == null
            FilterChip(
                selected = isSelected,
                onClick = { onSelectMember(null) },
                label = {
                    Text(
                        text = "الكل",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else AmeenTheme.colors.textPrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmeenTheme.colors.primary,
                    selectedLabelColor = Color.White,
                    containerColor = AmeenTheme.colors.surfaceVariant,
                    labelColor = AmeenTheme.colors.textPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = AmeenTheme.colors.border,
                    selectedBorderColor = AmeenTheme.colors.primary,
                    borderWidth = 1.5.dp
                )
            )
        }
        items(members) { member ->
            val isSelected = selectedMemberId == member.id
            FilterChip(
                selected = isSelected,
                onClick = { onSelectMember(member.id) },
                label = {
                    Text(
                        text = member.name,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else AmeenTheme.colors.textPrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmeenTheme.colors.primary,
                    selectedLabelColor = Color.White,
                    containerColor = AmeenTheme.colors.surfaceVariant,
                    labelColor = AmeenTheme.colors.textPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = AmeenTheme.colors.border,
                    selectedBorderColor = AmeenTheme.colors.primary,
                    borderWidth = 1.5.dp
                )
            )
        }
        item {
            AssistChip(
                onClick = onAddMemberClick,
                label = { Text("+ فرد", fontWeight = FontWeight.Bold, color = AmeenTheme.colors.onPrimaryContainer) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة فرد",
                        modifier = Modifier.size(16.dp),
                        tint = AmeenTheme.colors.onPrimaryContainer
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = AmeenTheme.colors.primaryContainer,
                    labelColor = AmeenTheme.colors.onPrimaryContainer
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = AmeenTheme.colors.primary.copy(alpha = 0.5f),
                    borderWidth = 1.5.dp
                )
            )
        }
    }
}
