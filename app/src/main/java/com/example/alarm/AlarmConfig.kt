package com.example.alarm

/**
 * Consolidated configuration constants for Ameen Medication Alarms.
 * Contains explicit, non-placeholder parameters for volumes, vibration waveforms,
 * escalation delays, and requestCode calculations.
 */
object AlarmConfig {

    // Safety duration: service automatically stops after 2 minutes to prevent battery drain
    const val RINGING_SAFETY_TIMEOUT_MS: Long = 120_000L // 2 minutes

    // Snooze default duration: 15 minutes
    const val SNOOZE_DELAY_MS: Long = 15 * 60 * 1000L

    // Escalation tier delays
    const val TIER_1_ESCALATION_DELAY_MS: Long = 15 * 60 * 1000L // +15 min
    const val TIER_2_ESCALATION_DELAY_MS: Long = 30 * 60 * 1000L // +30 min (Critical only)

    // Test alarm delay: 10 seconds
    const val TEST_ALARM_DELAY_MS: Long = 10_000L
    const val TEST_ALARM_BASE_REQUEST_CODE: Int = 99990
    const val TEST_ALARM_MED_ID: String = "test_med_preview_id"

    // Dedup requestCode helper: (baseCode * 10) + (escalationLevel % 10)
    fun getRequestCode(baseCode: Int, escalationLevel: Int): Int {
        return (baseCode * 10) + (escalationLevel % 10)
    }

    /**
     * Tier-specific audio volume (0.0f to 1.0f) and vibration waveform configurations.
     */
    data class TierProfile(
        val volume: Float,
        val vibrationTimings: LongArray,
        val vibrationAmplitudes: IntArray,
        val repeatIndex: Int = 0 // repeat from timing 0 during looping
    )

    // Tier 0: Primary alarm (T+0)
    val TIER_0_PRIMARY = TierProfile(
        volume = 0.70f,
        vibrationTimings = longArrayOf(0, 400, 300, 400),
        vibrationAmplitudes = intArrayOf(0, 180, 0, 180)
    )

    // Tier 1: Escalation 1 (T+15m)
    val TIER_1_REMINDER = TierProfile(
        volume = 0.85f,
        vibrationTimings = longArrayOf(0, 600, 250, 600),
        vibrationAmplitudes = intArrayOf(0, 220, 0, 220)
    )

    // Tier 2: Escalation 2 (T+30m Critical)
    val TIER_2_CRITICAL = TierProfile(
        volume = 1.0f,
        vibrationTimings = longArrayOf(0, 800, 200, 800, 200, 1000),
        vibrationAmplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
    )

    fun getTierProfile(escalationLevel: Int): TierProfile {
        return when (escalationLevel) {
            1 -> TIER_1_REMINDER
            2 -> TIER_2_CRITICAL
            else -> TIER_0_PRIMARY
        }
    }
}
