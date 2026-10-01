package com.example.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmOrchestrator
import com.example.alarm.AlarmSoundService
import com.example.receiver.MedicationActionReceiver
import com.example.receiver.MedicationAlarmReceiver
import com.example.ui.theme.*

enum class AlarmDisplayStyle(val title: String, val icon: String) {
    FULL_SCREEN("شاشة كاملة", "📱"),
    TOP_DROPDOWN("شريط علوي", "⬇️"),
    CENTER_DIALOG("نافذة بالوسط", "🔲"),
    BOTTOM_PANEL("لوحة سفلية", "📋")
}

/**
 * Incoming alarm screen shown on top of the lockscreen for all medications.
 * Supports 4 customizable display modes:
 * - Full Screen (شاشة كاملة)
 * - Top Dropdown (شريط علوي منسدل)
 * - Center Dialog (نافذة بالوسط)
 * - Bottom Panel (لوحة سفلية)
 * Offers immediate actions to Take, Snooze, or Stop Alarm.
 */
class AlarmFullScreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        turnScreenOnAndShowWhenLocked()

        val medId = intent.getStringExtra(AlarmSoundService.EXTRA_MED_ID) ?: ""
        val medName = intent.getStringExtra(AlarmSoundService.EXTRA_MED_NAME) ?: "الدواء"
        val memberName = intent.getStringExtra(AlarmSoundService.EXTRA_MEMBER_NAME) ?: "أحد أفراد العائلة"
        val memberId = intent.getStringExtra(AlarmSoundService.EXTRA_MEMBER_ID) ?: ""
        val isCritical = intent.getBooleanExtra(AlarmSoundService.EXTRA_IS_CRITICAL, false)
        val scheduledAt = intent.getLongExtra(AlarmSoundService.EXTRA_SCHEDULED_AT, System.currentTimeMillis())
        val scheduleEntryId = intent.getStringExtra(AlarmSoundService.EXTRA_SCHEDULE_ENTRY_ID)

        setContent {
            AmeenTheme(darkTheme = true) {
                MultiModeAlarmScreen(
                    medicationName = medName,
                    memberName = memberName,
                    isCritical = isCritical,
                    onTakeDose = {
                        val takeIntent = Intent(this, MedicationActionReceiver::class.java).apply {
                            action = AlarmOrchestrator.ACTION_TAKE_DOSE
                            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medId)
                            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medName)
                            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
                            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID, scheduleEntryId)
                            putExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID, memberId)
                        }
                        sendBroadcast(takeIntent)
                        AlarmSoundService.stopAlarm(this)
                        finish()
                    },
                    onSnooze = {
                        val snoozeIntent = Intent(this, MedicationActionReceiver::class.java).apply {
                            action = AlarmOrchestrator.ACTION_SNOOZE_15
                            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medId)
                            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_NAME, medName)
                            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULED_AT, scheduledAt)
                            putExtra(MedicationAlarmReceiver.EXTRA_SCHEDULE_ENTRY_ID, scheduleEntryId)
                            putExtra(MedicationAlarmReceiver.EXTRA_FAMILY_MEMBER_ID, memberId)
                            putExtra(MedicationAlarmReceiver.EXTRA_IS_CRITICAL, isCritical)
                        }
                        sendBroadcast(snoozeIntent)
                        AlarmSoundService.stopAlarm(this)
                        finish()
                    },
                    onDismiss = {
                        val dismissIntent = Intent(this, MedicationActionReceiver::class.java).apply {
                            action = AlarmOrchestrator.ACTION_DISMISS
                            putExtra(MedicationAlarmReceiver.EXTRA_MEDICATION_ID, medId)
                        }
                        sendBroadcast(dismissIntent)
                        AlarmSoundService.stopAlarm(this)
                        finish()
                    }
                )
            }
        }
    }

    private fun turnScreenOnAndShowWhenLocked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}

@Composable
fun MultiModeAlarmScreen(
    medicationName: String,
    memberName: String,
    isCritical: Boolean,
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ameen_alarm_prefs", Context.MODE_PRIVATE) }
    var currentStyle by remember {
        val savedName = prefs.getString("alarm_display_style", AlarmDisplayStyle.FULL_SCREEN.name)
        mutableStateOf(
            try {
                AlarmDisplayStyle.valueOf(savedName ?: AlarmDisplayStyle.FULL_SCREEN.name)
            } catch (_: Exception) {
                AlarmDisplayStyle.FULL_SCREEN
            }
        )
    }

    fun updateStyle(style: AlarmDisplayStyle) {
        currentStyle = style
        prefs.edit().putString("alarm_display_style", style.name).apply()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (currentStyle == AlarmDisplayStyle.FULL_SCREEN) 1f else 0.85f))
    ) {
        when (currentStyle) {
            AlarmDisplayStyle.FULL_SCREEN -> {
                FullScreenLayout(
                    medicationName = medicationName,
                    memberName = memberName,
                    isCritical = isCritical,
                    currentStyle = currentStyle,
                    onStyleSelected = ::updateStyle,
                    onTakeDose = onTakeDose,
                    onSnooze = onSnooze,
                    onDismiss = onDismiss
                )
            }
            AlarmDisplayStyle.TOP_DROPDOWN -> {
                TopDropdownLayout(
                    medicationName = medicationName,
                    memberName = memberName,
                    isCritical = isCritical,
                    currentStyle = currentStyle,
                    onStyleSelected = ::updateStyle,
                    onTakeDose = onTakeDose,
                    onSnooze = onSnooze,
                    onDismiss = onDismiss
                )
            }
            AlarmDisplayStyle.CENTER_DIALOG -> {
                CenterDialogLayout(
                    medicationName = medicationName,
                    memberName = memberName,
                    isCritical = isCritical,
                    currentStyle = currentStyle,
                    onStyleSelected = ::updateStyle,
                    onTakeDose = onTakeDose,
                    onSnooze = onSnooze,
                    onDismiss = onDismiss
                )
            }
            AlarmDisplayStyle.BOTTOM_PANEL -> {
                BottomPanelLayout(
                    medicationName = medicationName,
                    memberName = memberName,
                    isCritical = isCritical,
                    currentStyle = currentStyle,
                    onStyleSelected = ::updateStyle,
                    onTakeDose = onTakeDose,
                    onSnooze = onSnooze,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

/**
 * Style Switcher Chip Row allowing instant toggling between the 4 requested alarm presentations
 */
@Composable
fun AlarmStyleSwitcherRow(
    currentStyle: AlarmDisplayStyle,
    onStyleSelected: (AlarmDisplayStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlarmDisplayStyle.values().forEach { style ->
                val isSelected = style == currentStyle
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) EmeraldPrimary else Color.Transparent,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    TextButton(
                        onClick = { onStyleSelected(style) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "${style.icon} ${style.title}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 1. Full Screen Layout (شاشة كاملة)
 */
@Composable
private fun FullScreenLayout(
    medicationName: String,
    memberName: String,
    isCritical: Boolean,
    currentStyle: AlarmDisplayStyle,
    onStyleSelected: (AlarmDisplayStyle) -> Unit,
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top area: Style Switcher + Badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            AlarmStyleSwitcherRow(
                currentStyle = currentStyle,
                onStyleSelected = onStyleSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isCritical) CriticalRed.copy(alpha = 0.25f) else EmeraldPrimary.copy(alpha = 0.25f),
                border = BorderStroke(1.5.dp, if (isCritical) CriticalRed else EmeraldPrimary)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isCritical) CriticalRed else MintContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCritical) "🚨 منبه دواء حرج للغاية" else "⏰ حان موعد تناول الدواء",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Center: Pulsing Icon & Drug Details
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                if (isCritical) CriticalRed.copy(alpha = 0.5f) else EmeraldPrimary.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        2.5.dp,
                        if (isCritical) CriticalRed else EmeraldPrimary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = medicationName,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "المريض: $memberName",
                color = MintContainer,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "يرجى تناول الجرعة المحددة الآن للحفاظ على صحتك.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        // Bottom Actions
        AlarmActionButtonsGroup(
            onTakeDose = onTakeDose,
            onSnooze = onSnooze,
            onDismiss = onDismiss
        )
    }
}

/**
 * 2. Top Dropdown Layout (شريط علوي منسدل)
 */
@Composable
private fun TopDropdownLayout(
    medicationName: String,
    memberName: String,
    isCritical: Boolean,
    currentStyle: AlarmDisplayStyle,
    onStyleSelected: (AlarmDisplayStyle) -> Unit,
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating Top Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            color = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, if (isCritical) CriticalRed else EmeraldPrimary),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AlarmStyleSwitcherRow(
                    currentStyle = currentStyle,
                    onStyleSelected = onStyleSelected
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isCritical) CriticalRed.copy(alpha = 0.2f) else MintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (isCritical) CriticalRed else EmeraldDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCritical) "🚨 منبه حرج: $medicationName" else "⏰ موعد دواء: $medicationName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "المريض: $memberName",
                            fontSize = 14.sp,
                            color = MintContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onTakeDose,
                        modifier = Modifier.weight(1.2f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_take_dose), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_snooze_15), color = Color.White, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.9f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_skip_dose), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Empty bottom space to allow seeing background
        Spacer(modifier = Modifier.height(10.dp))
    }
}

/**
 * 3. Center Dialog Layout (نافذة بالوسط)
 */
@Composable
private fun CenterDialogLayout(
    medicationName: String,
    memberName: String,
    isCritical: Boolean,
    currentStyle: AlarmDisplayStyle,
    onStyleSelected: (AlarmDisplayStyle) -> Unit,
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlarmStyleSwitcherRow(
            currentStyle = currentStyle,
            onStyleSelected = onStyleSelected,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, if (isCritical) CriticalRed else EmeraldPrimary),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isCritical) CriticalRed.copy(alpha = 0.2f) else MintContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isCritical) CriticalRed else EmeraldDark,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = medicationName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "للمريض: $memberName",
                    fontSize = 16.sp,
                    color = MintContainer,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onTakeDose,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_take_dose), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_snooze_15), color = Color.White, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_skip_dose), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * 4. Bottom Panel Layout (لوحة سفلية)
 */
@Composable
private fun BottomPanelLayout(
    medicationName: String,
    memberName: String,
    isCritical: Boolean,
    currentStyle: AlarmDisplayStyle,
    onStyleSelected: (AlarmDisplayStyle) -> Unit,
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlarmStyleSwitcherRow(
            currentStyle = currentStyle,
            onStyleSelected = onStyleSelected,
            modifier = Modifier.padding(top = 16.dp)
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            color = SurfaceDark,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, if (isCritical) CriticalRed else EmeraldPrimary),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (isCritical) CriticalRed.copy(alpha = 0.2f) else MintContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = if (isCritical) CriticalRed else EmeraldDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = medicationName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "لـ $memberName • حان موعد التناول",
                            fontSize = 13.sp,
                            color = MintContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onTakeDose,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_take_dose), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_snooze_15), color = Color.White, fontSize = 13.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralRed)
                    ) {
                        Text(androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_skip_dose), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Common Action Buttons for FullScreen
 */
@Composable
private fun AlarmActionButtonsGroup(
    onTakeDose: () -> Unit,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onTakeDose,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_take_dose),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onSnooze,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Snooze,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_snooze_15),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralRed,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.aistudio.ameen.medication.R.string.action_skip_dose),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
