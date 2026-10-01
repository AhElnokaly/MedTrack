## fix-state: Phase 1 — Reliable Lock-Screen Alarm Experience
| # | المهمة | Status | QA Gate | Notes |
|---|--------|--------|---------|-------|
| 1 | تأسيس الثوابت والنصوص الرسمية (AlarmConfig + strings.xml) | done | PASS — compile_applet clean | AlarmConfig + all Arabic strings extracted |
| 2 | تحديث الجدولة والـ Receivers والخصوصية الطبية (setAlarmClock + setPublicVersion + Boot/Time receivers) | done | PASS — compile_applet clean | setAlarmClock, TIMEZONE_CHANGED, TIME_SET, VISIBILITY_PRIVATE |
| 3 | ترقية خدمة الرنين (WakeLock + VibrationEffect + أزرار أخدت/أجّل/تخطّى) | done | PASS — compile_applet clean | WakeLock 2min timeout, VibrationEffect waveform, canonical actions |
| 4 | شاشة الفحص الذاتي وإرشادات الـ OEM والـ Fallback Banner | done | PASS — compile_applet clean | AlarmDiagnosticsScreen, canUseFullScreenIntent check, test in 10s |
