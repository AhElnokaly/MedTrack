package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

/**
 * Sound selection component for Real Alarm System:
 * - TONE: System ringtone / alarm picker
 * - TTS: Arabic Text-To-Speech pronunciation of drug and member
 * - CUSTOM_FILE: Custom audio file imported and stored privately
 * - RECORDED: In-app recorded voice reminder
 */
@Composable
fun SoundTypeSelectorSection(
    context: Context,
    selectedSoundType: String,
    soundUri: String?,
    onSoundTypeSelected: (String) -> Unit,
    onSoundUriChanged: (String?) -> Unit
) {
    var isPlayingPreview by remember { mutableStateOf(false) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Ringtone picker for system alarm tones
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val pickedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (pickedUri != null) {
                onSoundUriChanged(pickedUri.toString())
                Toast.makeText(context, "تم اختيار نغمة المنبه بنجاح ✓", Toast.LENGTH_SHORT).show()
            } else {
                onSoundUriChanged(null)
            }
        }
    }

    val ringtoneTitle = remember(soundUri) {
        if (!soundUri.isNullOrEmpty()) {
            try {
                val r = RingtoneManager.getRingtone(context, Uri.parse(soundUri))
                r?.getTitle(context) ?: "نغمة مخصصة"
            } catch (_: Exception) {
                "نغمة مخصصة"
            }
        } else {
            "نغمة المنبه الافتراضية للنظام"
        }
    }

    // File picker for custom audio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val alarmsDir = File(context.filesDir, "custom_alarms")
                if (!alarmsDir.exists()) alarmsDir.mkdirs()
                val targetFile = File(alarmsDir, "custom_${System.currentTimeMillis()}.mp3")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                onSoundUriChanged(targetFile.absolutePath)
                Toast.makeText(context, "تم حفظ الملف الصوتي بنجاح ✓", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "فشل حفظ الملف الصوتي: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                previewPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "صوت ونوع المنبه (نظام المنبه الحقيقي)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sound Options (2 Rows of chips)
            val soundOptionsRow1 = listOf(
                Triple("TONE", "نغمة المنبه", Icons.Default.NotificationsActive),
                Triple("TTS", "نطق ذكي (TTS)", Icons.Default.RecordVoiceOver),
                Triple("RECORDED", "تسجيل صوتك", Icons.Default.Mic)
            )
            val soundOptionsRow2 = listOf(
                Triple("CUSTOM_FILE", "ملف صوتي", Icons.Default.AudioFile),
                Triple("VIBRATE", "اهتزاز فقط", Icons.Default.Vibration),
                Triple("SILENT", "تنبيه صامت", Icons.Default.NotificationsOff)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    soundOptionsRow1.forEach { (type, label, icon) ->
                        val isSelected = selectedSoundType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSoundTypeSelected(type) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else BorderLight
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    soundOptionsRow2.forEach { (type, label, icon) ->
                        val isSelected = selectedSoundType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSoundTypeSelected(type) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) EmeraldPrimary else SurfaceVariantLight,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldPrimary else BorderLight
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contextual details based on selected type
            when (selectedSoundType) {
                "TONE" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MintContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "النغمة: $ringtoneTitle",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "سيتم تشغيل النغمة بتكرار مستمر في قناة USAGE_ALARM المخصصة للمنبهات حتى الاستجابة.",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "اختر نغمة منبه الدواء")
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                        val existingUri = soundUri?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
                                    }
                                    ringtonePickerLauncher.launch(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("اختر من الجهاز", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isPlayingPreview) {
                                        previewPlayer?.stop()
                                        previewPlayer?.release()
                                        previewPlayer = null
                                        isPlayingPreview = false
                                    } else {
                                        try {
                                            val alarmUri = if (!soundUri.isNullOrEmpty()) {
                                                Uri.parse(soundUri)
                                            } else {
                                                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                                                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                                            }
                                            previewPlayer?.release()
                                            previewPlayer = MediaPlayer().apply {
                                                setDataSource(context, alarmUri)
                                                prepare()
                                                start()
                                                setOnCompletionListener {
                                                    isPlayingPreview = false
                                                }
                                            }
                                            isPlayingPreview = true
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "تعذر تشغيل المعاينة", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EmeraldPrimary)
                            ) {
                                Icon(
                                    if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPlayingPreview) "إيقاف" else "معاينة", color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                "TTS" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MintContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سينطق المنبه اسم الدواء واسم المريض باللغة العربية كل 6 ثوانٍ بصوت واضح.",
                                fontSize = 12.sp,
                                color = EmeraldDark,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                "CUSTOM_FILE" -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MintSecondary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اختيار ملف صوتي من الجهاز", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (!soundUri.isNullOrBlank()) {
                            Text(
                                text = "✓ تم اختيار وحفظ الملف: ${File(soundUri).name}",
                                fontSize = 12.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                "RECORDED" -> {
                    RecordedAlarmSection(
                        context = context,
                        soundUri = soundUri,
                        onSoundUriChanged = onSoundUriChanged
                    )
                }

                "VIBRATE" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MintContainer,
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "📳 نمط الاهتزاز المتقطع",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                                Text(
                                    text = "سيعمل المنبه بنبضات اهتزاز متقطعة ومستمرة لتنبيهك دون إزعاج المحيطين بالأصوات.",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                "SILENT" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceVariantLight,
                        border = BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "🔕 تنبيه صامت خفيف",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "سيظهر إشعار مرئي وشاشة المنبه بالكامل بدون رنين أو اهتزاز (مناسب للاجتماعات والهدوء).",
                                    fontSize = 11.5.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
