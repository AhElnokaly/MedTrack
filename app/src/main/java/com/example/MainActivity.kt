package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarm.AlarmOrchestrator
import com.example.alarm.AlarmSoundService
import com.example.alarm.ActiveAlarmState
import com.example.receiver.MedicationActionReceiver
import com.example.receiver.MedicationAlarmReceiver
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MedicationViewModel

enum class AmeenScreen(val title: String) {
    HOME("الرئيسية"),
    MEDICATIONS("الأدوية"),
    FAMILY("العائلة"),
    SHOPPING_REPORTS("المخزون والتقارير"),
    DIAGNOSTICS("جاهزية المنبه")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MedicationViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            AmeenTheme(darkTheme = isDarkMode) {
                AmeenApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AmeenApp(
    viewModel: MedicationViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AmeenScreen.HOME) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }

    val members by viewModel.allMembers.collectAsStateWithLifecycle()
    val activeAlarm by AlarmSoundService.currentActiveAlarm.collectAsStateWithLifecycle()

    // System Back Gesture/Button Handler
    BackHandler(enabled = currentScreen != AmeenScreen.HOME) {
        currentScreen = AmeenScreen.HOME
    }

    // Exact Alarm & Notification Permission Handlers
    var showExactAlarmPermissionDialog by remember { mutableStateOf(false) }
    var isFullScreenIntentMissing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as? android.app.AlarmManager
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog = true
            }
        }

        // Requirement 4: check NotificationManager.canUseFullScreenIntent() on API 34+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
            if (notificationManager != null && !notificationManager.canUseFullScreenIntent()) {
                isFullScreenIntentMissing = true
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AmeenTheme.colors.background,
        bottomBar = {
            NavigationBar(
                containerColor = AmeenTheme.colors.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navBarItemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AmeenTheme.colors.primary,
                    selectedTextColor = AmeenTheme.colors.primary,
                    indicatorColor = AmeenTheme.colors.primaryContainer,
                    unselectedIconColor = AmeenTheme.colors.textSecondary,
                    unselectedTextColor = AmeenTheme.colors.textSecondary
                )

                // 1. Home
                NavigationBarItem(
                    selected = currentScreen == AmeenScreen.HOME,
                    onClick = { currentScreen = AmeenScreen.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AmeenScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = AmeenScreen.HOME.title
                        )
                    },
                    label = { Text(AmeenScreen.HOME.title, fontWeight = FontWeight.Bold) },
                    colors = navBarItemColors
                )

                // 2. Medications
                NavigationBarItem(
                    selected = currentScreen == AmeenScreen.MEDICATIONS,
                    onClick = { currentScreen = AmeenScreen.MEDICATIONS },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AmeenScreen.MEDICATIONS) Icons.Filled.Medication else Icons.Outlined.Medication,
                            contentDescription = AmeenScreen.MEDICATIONS.title
                        )
                    },
                    label = { Text(AmeenScreen.MEDICATIONS.title, fontWeight = FontWeight.Bold) },
                    colors = navBarItemColors
                )

                // 3. Family
                NavigationBarItem(
                    selected = currentScreen == AmeenScreen.FAMILY,
                    onClick = { currentScreen = AmeenScreen.FAMILY },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AmeenScreen.FAMILY) Icons.Filled.FamilyRestroom else Icons.Outlined.FamilyRestroom,
                            contentDescription = AmeenScreen.FAMILY.title
                        )
                    },
                    label = { Text(AmeenScreen.FAMILY.title, fontWeight = FontWeight.Bold) },
                    colors = navBarItemColors
                )

                // 4. Shopping & Reports
                NavigationBarItem(
                    selected = currentScreen == AmeenScreen.SHOPPING_REPORTS,
                    onClick = { currentScreen = AmeenScreen.SHOPPING_REPORTS },
                    icon = {
                        Icon(
                            imageVector = if (currentScreen == AmeenScreen.SHOPPING_REPORTS) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                            contentDescription = AmeenScreen.SHOPPING_REPORTS.title
                        )
                    },
                    label = { Text("التقارير", fontWeight = FontWeight.Bold) },
                    colors = navBarItemColors
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High-Priority Active Alarm Sticky Banner when alarm is actively ringing
            AnimatedVisibility(
                visible = activeAlarm != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activeAlarm?.let { alarm ->
                    ActiveAlarmStickyBanner(
                        alarm = alarm,
                        onTakeDose = {
                            val takeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
                                action = AlarmOrchestrator.ACTION_TAKE_DOSE
                                putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, alarm.medicationId)
                                putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, alarm.medicationName)
                                putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT, alarm.scheduledAt)
                                putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID, alarm.scheduleEntryId)
                                putExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID, alarm.memberId)
                            }
                            context.sendBroadcast(takeIntent)
                            AlarmSoundService.stopAlarm(context)
                        },
                        onSnooze = {
                            val snoozeIntent = Intent(context, MedicationActionReceiver::class.java).apply {
                                action = AlarmOrchestrator.ACTION_SNOOZE_15
                                putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, alarm.medicationId)
                                putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, alarm.medicationName)
                                putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT, alarm.scheduledAt)
                                putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID, alarm.scheduleEntryId)
                                putExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID, alarm.memberId)
                                putExtra(MedicationAlarmReceiver.EXTRA_IS_CRITICAL, alarm.isCritical)
                            }
                            context.sendBroadcast(snoozeIntent)
                            AlarmSoundService.stopAlarm(context)
                        },
                        onStopAlarm = {
                            val dismissIntent = Intent(context, MedicationActionReceiver::class.java).apply {
                                action = AlarmOrchestrator.ACTION_DISMISS
                                putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, alarm.medicationId)
                            }
                            context.sendBroadcast(dismissIntent)
                            AlarmSoundService.stopAlarm(context)
                        }
                    )
                }
            }

            // Requirement 4: Fallback banner when full-screen intent is missing on API 34+
            if (isFullScreenIntentMissing) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    color = WarningAmberLight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, WarningAmber)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningAmberDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.banner_fullscreen_permission_title),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = WarningAmberDark
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.banner_fullscreen_permission_desc),
                                fontSize = 11.sp,
                                color = AmeenTheme.colors.textPrimary,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    try {
                                        val intent = Intent(
                                            android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                                            android.net.Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val intent = Intent(
                                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            android.net.Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmberDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.banner_fullscreen_permission_btn),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Top Navigation Banner with Back Button when not on Home Screen
            if (currentScreen != AmeenScreen.HOME) {
                Surface(
                    color = AmeenTheme.colors.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { currentScreen = AmeenScreen.HOME },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(AmeenTheme.colors.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع للرئيسية",
                                    tint = AmeenTheme.colors.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = currentScreen.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmeenTheme.colors.textPrimary
                            )
                        }

                        // Shield icon button to open Diagnostics directly
                        IconButton(
                            onClick = { currentScreen = AmeenScreen.DIAGNOSTICS },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MintContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HealthAndSafety,
                                contentDescription = "فحص جاهزية المنبه",
                                tint = EmeraldDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    AmeenScreen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToMedications = { currentScreen = AmeenScreen.MEDICATIONS },
                        onNavigateToFamily = { currentScreen = AmeenScreen.FAMILY },
                        onNavigateToShopping = { currentScreen = AmeenScreen.SHOPPING_REPORTS },
                        onOpenAddMedication = { showAddMedicationDialog = true },
                        onNavigateToDiagnostics = { currentScreen = AmeenScreen.DIAGNOSTICS }
                    )

                    AmeenScreen.MEDICATIONS -> MedicationsScreen(
                        viewModel = viewModel,
                        onOpenAddMedication = { showAddMedicationDialog = true }
                    )

                    AmeenScreen.FAMILY -> FamilyScreen(
                        viewModel = viewModel
                    )

                    AmeenScreen.SHOPPING_REPORTS -> ShoppingAndReportsScreen(
                        viewModel = viewModel
                    )

                    AmeenScreen.DIAGNOSTICS -> AlarmDiagnosticsScreen(
                        onNavigateBack = { currentScreen = AmeenScreen.HOME }
                    )
                }
            }
        }

        // Add Medication Dialog
        if (showAddMedicationDialog) {
            AddMedicationDialog(
                viewModel = viewModel,
                members = members,
                onDismiss = { showAddMedicationDialog = false }
            )
        }

        // Exact Alarm Permission Guidance Dialog
        if (showExactAlarmPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showExactAlarmPermissionDialog = false },
                containerColor = AmeenTheme.colors.surface,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⏰", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "صلاحية التنبيهات الدقيقة",
                            fontWeight = FontWeight.Bold,
                            color = AmeenTheme.colors.textPrimary
                        )
                    }
                },
                text = {
                    Text(
                        text = "يحتاج تطبيق أمين إلى إذن ضبط التنبيهات الدقيقة (Exact Alarms) لضمان رنين مواعيد الأدوية في ثوانيها المحددة والتصاعد التلقائي حتى لو كان الهاتف في وضع السكون.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmeenTheme.colors.textSecondary,
                        lineHeight = 22.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExactAlarmPermissionDialog = false
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                try {
                                    val intent = Intent(
                                        android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                        android.net.Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmeenTheme.colors.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تفعيل من الإعدادات", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showExactAlarmPermissionDialog = false },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("لاحقاً", color = AmeenTheme.colors.textSecondary)
                    }
                }
            )
        }
    }
}

/**
 * Sticky banner shown across all screens when an alarm is actively ringing.
 * Ensures the alarm can be immediately stopped or acknowledged directly from inside the app.
 */
@Composable
fun ActiveAlarmStickyBanner(
    alarm: ActiveAlarmState,
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onStopAlarm: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = if (alarm.isCritical) CriticalRed else MintContainer,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, if (alarm.isCritical) Color.White else EmeraldPrimary),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (alarm.isCritical) Color.White.copy(alpha = 0.2f) else EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (alarm.isCritical) Color.White else EmeraldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (alarm.isCritical) "🚨 منبه دواء حرج يرن الآن!" else "⏰ منبه دواء يرن الآن!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (alarm.isCritical) Color.White else EmeraldDark
                        )
                        Text(
                            text = "${alarm.medicationName} لـ ${alarm.memberName}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (alarm.isCritical) Color.White else TextPrimary
                        )
                    }
                }

                // Immediate 1-tap Stop Button
                Button(
                    onClick = onStopAlarm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alarm.isCritical) Color.White else CoralRed,
                        contentColor = if (alarm.isCritical) CriticalRed else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إيقاف 🛑", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTakeDose,
                    modifier = Modifier.weight(1.3f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("أخذت الجرعة ✓", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (alarm.isCritical) Color.White.copy(alpha = 0.6f) else EmeraldDark.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (alarm.isCritical) Color.White else EmeraldDark
                    )
                ) {
                    Text("تأجيل 15د ⏰", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
