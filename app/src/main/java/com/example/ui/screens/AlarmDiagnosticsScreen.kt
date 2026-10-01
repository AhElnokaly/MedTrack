package com.example.ui.screens

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aistudio.ameen.medication.R
import com.example.alarm.AlarmOrchestrator
import com.example.ui.theme.*

data class DiagnosticItem(
    val title: String,
    val isGranted: Boolean,
    val onFix: () -> Unit
)

/**
 * Requirement 8 & 9:
 * Permissions self-test screen that inspects:
 * 1. POST_NOTIFICATIONS
 * 2. SCHEDULE_EXACT_ALARM (canScheduleExactAlarms)
 * 3. USE_FULL_SCREEN_INTENT (canUseFullScreenIntent on API 34+)
 * 4. Battery Optimization Exemption (isIgnoringBatteryOptimizations)
 * Includes "Test alarm in 10 seconds" button and OEM Autostart Guidance dialog.
 */
@Composable
fun AlarmDiagnosticsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showOemDialog by remember { mutableStateOf(false) }

    // State holders for permissions (re-evaluated on resume)
    var isNotificationGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    var isExactAlarmGranted by remember { mutableStateOf(checkExactAlarmPermission(context)) }
    var isFullScreenGranted by remember { mutableStateOf(checkFullScreenPermission(context)) }
    var isBatteryExempt by remember { mutableStateOf(checkBatteryOptimization(context)) }

    fun refreshStatus() {
        isNotificationGranted = checkNotificationPermission(context)
        isExactAlarmGranted = checkExactAlarmPermission(context)
        isFullScreenGranted = checkFullScreenPermission(context)
        isBatteryExempt = checkBatteryOptimization(context)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationGranted = granted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmeenTheme.colors.background)
    ) {
        // Top Bar
        Surface(
            color = AmeenTheme.colors.surface,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AmeenTheme.colors.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = AmeenTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.diagnostics_screen_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AmeenTheme.colors.textPrimary
                    )
                    Text(
                        text = "فحص المنبه وشاشة القفل",
                        fontSize = 11.sp,
                        color = AmeenTheme.colors.textSecondary
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MintContainer,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = EmeraldDark,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.diagnostics_screen_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldDark,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 1. POST_NOTIFICATIONS
            DiagnosticItemCard(
                title = stringResource(R.string.diag_perm_notifications),
                isGranted = isNotificationGranted,
                description = "لإرسال إشعارات التذكير وتنبيهات مواعيد الجرعات.",
                onFix = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        openAppNotificationSettings(context)
                    }
                }
            )

            // 2. SCHEDULE_EXACT_ALARM
            DiagnosticItemCard(
                title = stringResource(R.string.diag_perm_exact_alarm),
                isGranted = isExactAlarmGranted,
                description = "لضمان رنين المنبه في الدقيقة والثانية المحددة بدون تأخير.",
                onFix = {
                    openExactAlarmSettings(context)
                }
            )

            // 3. USE_FULL_SCREEN_INTENT
            DiagnosticItemCard(
                title = stringResource(R.string.diag_perm_fullscreen),
                isGranted = isFullScreenGranted,
                description = "لإيقاظ شاشة الهاتف وعرض المنبه كاملاً على شاشة القفل عند الرنين.",
                onFix = {
                    openFullScreenIntentSettings(context)
                }
            )

            // 4. Battery Optimization Exemption
            DiagnosticItemCard(
                title = stringResource(R.string.diag_perm_battery),
                isGranted = isBatteryExempt,
                description = "لمنع النظام من إيقاف المنبه في وضع السكون العميق أو توفير الطاقة.",
                onFix = {
                    openBatteryOptimizationSettings(context)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Test Alarm in 10 Seconds Button
            Button(
                onClick = {
                    AlarmOrchestrator.scheduleTestAlarm(context)
                    Toast.makeText(
                        context,
                        context.getString(R.string.diag_test_scheduled_toast),
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.diag_btn_test_10s),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // OEM Autostart Guide Button
            OutlinedButton(
                onClick = { showOemDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.diag_oem_guide_btn),
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark,
                    fontSize = 14.sp
                )
            }
        }
    }

    // OEM Guidance Dialog
    if (showOemDialog) {
        OemGuidanceDialog(
            onDismiss = {
                showOemDialog = false
                refreshStatus()
            },
            onOpenAppSettings = {
                openAppDetailsSettings(context)
            }
        )
    }
}

@Composable
private fun DiagnosticItemCard(
    title: String,
    isGranted: Boolean,
    description: String,
    onFix: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AmeenTheme.colors.surface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isGranted) EmeraldPrimary.copy(alpha = 0.3f) else CoralRed.copy(alpha = 0.4f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = if (isGranted) EmeraldPrimary else CoralRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AmeenTheme.colors.textPrimary
                    )
                }

                Surface(
                    color = if (isGranted) MintContainer else CoralRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(if (isGranted) R.string.diag_status_granted else R.string.diag_status_missing),
                        color = if (isGranted) EmeraldDark else CoralRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = AmeenTheme.colors.textSecondary,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onFix,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralRed,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = stringResource(R.string.diag_btn_fix),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OemGuidanceDialog(
    onDismiss: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    val manufacturer = remember { Build.MANUFACTURER.lowercase() }
    var selectedTab by remember {
        mutableStateOf(
            when {
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") -> 0
                manufacturer.contains("samsung") -> 1
                manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") -> 2
                else -> 3
            }
        )
    }

    val tabTitles = listOf("شاومي", "سامسونج", "أوبو/ريلمي", "أخرى")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = EmeraldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.oem_dialog_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = EmeraldPrimary
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val contentText = when (selectedTab) {
                    0 -> stringResource(R.string.oem_dialog_xiaomi)
                    1 -> stringResource(R.string.oem_dialog_samsung)
                    2 -> stringResource(R.string.oem_dialog_oppo)
                    else -> stringResource(R.string.oem_dialog_generic)
                }

                Text(
                    text = contentText,
                    fontSize = 13.sp,
                    color = AmeenTheme.colors.textPrimary,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOpenAppSettings()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text(stringResource(R.string.oem_dialog_open_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.oem_dialog_close))
            }
        }
    )
}

// Helpers for permission checks
fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}

fun checkExactAlarmPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.canScheduleExactAlarms() ?: true
    } else {
        true
    }
}

fun checkFullScreenPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.canUseFullScreenIntent() ?: true
    } else {
        true
    }
}

fun checkBatteryOptimization(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    } else {
        true
    }
}

// Navigation helpers to Android settings screens
fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppDetailsSettings(context)
        }
    }
}

fun openFullScreenIntentSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        try {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppDetailsSettings(context)
        }
    }
}

fun openBatteryOptimizationSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                context.startActivity(intent)
            } catch (_: Exception) {
                openAppDetailsSettings(context)
            }
        }
    }
}

fun openAppNotificationSettings(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
            context.startActivity(intent)
        } else {
            openAppDetailsSettings(context)
        }
    } catch (_: Exception) {
        openAppDetailsSettings(context)
    }
}

fun openAppDetailsSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {}
}
