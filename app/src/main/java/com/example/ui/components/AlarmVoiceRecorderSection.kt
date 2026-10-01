package com.example.ui.components

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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

@Composable
fun RecordedAlarmSection(
    context: Context,
    soundUri: String?,
    onSoundUriChanged: (String?) -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentRecordedFile by remember { mutableStateOf<File?>(null) }
    var isPlayingPreview by remember { mutableStateOf(false) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
                previewPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "سجل تذكيراً بصوتك (مثال: 'يا أمي خدي حبة الضغط دلوقتي')",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isRecording) {
                Button(
                    onClick = {
                        try {
                            val dir = File(context.filesDir, "recorded_alarms")
                            if (!dir.exists()) dir.mkdirs()
                            val recFile = File(dir, "voice_${System.currentTimeMillis()}.m4a")
                            currentRecordedFile = recFile

                            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                MediaRecorder(context)
                            } else {
                                @Suppress("DEPRECATION")
                                MediaRecorder()
                            }
                            recorder.apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                                setOutputFile(recFile.absolutePath)
                                prepare()
                                start()
                            }
                            mediaRecorder = recorder
                            isRecording = true
                            Toast.makeText(context, "جاري التسجيل الآن...", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "فشل بدء التسجيل: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "بدء التسجيل الصوتي", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("بدء التسجيل الصوتي", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        try {
                            mediaRecorder?.stop()
                            mediaRecorder?.release()
                            mediaRecorder = null
                            isRecording = false
                            currentRecordedFile?.let {
                                onSoundUriChanged(it.absolutePath)
                                Toast.makeText(context, "تم حفظ التسجيل الصوتي ✓", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            isRecording = false
                            Toast.makeText(context, "خطأ في حفظ التسجيل: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "إيقاف وحفظ التسجيل", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إيقاف وحفظ التسجيل", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (!soundUri.isNullOrBlank() && !isRecording) {
                IconButton(
                    onClick = {
                        if (isPlayingPreview) {
                            previewPlayer?.stop()
                            previewPlayer?.release()
                            previewPlayer = null
                            isPlayingPreview = false
                        } else {
                            try {
                                previewPlayer = MediaPlayer().apply {
                                    setDataSource(soundUri)
                                    prepare()
                                    start()
                                    setOnCompletionListener {
                                        isPlayingPreview = false
                                    }
                                }
                                isPlayingPreview = true
                            } catch (e: Exception) {
                                Toast.makeText(context, "تعذر تشغيل المعاينة: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MintContainer)
                ) {
                    Icon(
                        imageVector = if (isPlayingPreview) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "معاينة التسجيل",
                        tint = EmeraldPrimary
                    )
                }
            }
        }

        if (!soundUri.isNullOrBlank()) {
            Text(
                text = "✓ التسجيل جاهز للمنبه",
                fontSize = 12.sp,
                color = SuccessGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
