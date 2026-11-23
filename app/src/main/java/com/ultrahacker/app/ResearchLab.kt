package com.ultrahacker.app

import java.security.MessageDigest
import java.util.Locale

/**
 * Defensive research helpers. Source is inspected as text and is never compiled,
 * loaded, or executed. The simulator emits fixed illustrative events only.
 */
object ResearchLab {
    private data class Indicator(val label: String, val pattern: Regex, val severity: String)

    private val indicators = listOf(
        Indicator("Process execution API", Regex("""Runtime\\.getRuntime\\s*\\(\\s*\\)\\s*\\.exec|ProcessBuilder\\s*\\("""), "HIGH"),
        Indicator("Dynamic code loading", Regex("""DexClassLoader|PathClassLoader|InMemoryDexClassLoader"""), "HIGH"),
        Indicator("Accessibility service usage", Regex("""AccessibilityService|performGlobalAction"""), "HIGH"),
        Indicator("Device administrator API", Regex("""DeviceAdminReceiver|DevicePolicyManager"""), "MEDIUM"),
        Indicator("SMS capability", Regex("""SmsManager|sendTextMessage"""), "HIGH"),
        Indicator("Telephony identifier access", Regex("""getDeviceId|getImei|getSubscriberId"""), "HIGH"),
        Indicator("Socket/network communication", Regex("""Socket\\s*\\(|DatagramSocket|HttpURLConnection|OkHttpClient"""), "MEDIUM"),
        Indicator("Encoded payload handling", Regex("""Base64\\.decode|Base64\\.getDecoder"""), "LOW"),
        Indicator("Cryptographic transformation", Regex("""Cipher\\.getInstance|SecretKeySpec"""), "LOW"),
        Indicator("Native library loading", Regex("""System\\.loadLibrary|System\\.load\\s*\\("""), "MEDIUM"),
        Indicator("Root command reference", Regex("""(?:^|[\\s\"'])su(?:\\s|$|[\"'])"""), "HIGH"),
        Indicator("Boot persistence receiver", Regex("""BOOT_COMPLETED|RECEIVE_BOOT_COMPLETED"""), "MEDIUM")
    )

    fun analyzeSource(source: String): String {
        require(source.length <= 200_000) { "Input is too large; maximum is 200 KB." }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(source.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        val lines = source.lines()
        val findings = indicators.mapNotNull { indicator ->
            val hits = lines.mapIndexedNotNull { index, line ->
                if (indicator.pattern.containsMatchIn(line)) index + 1 else null
            }
            if (hits.isEmpty()) null else Triple(indicator, hits, hits.size)
        }
        return buildString {
            append("STATIC SOURCE REVIEW — NO EXECUTION\\n")
            append("SHA-256: ").append(digest).append("\\n")
            append("Lines: ").append(lines.size).append("\\n")
            append("Indicators: ").append(findings.size).append("\\n\\n")
            if (findings.isEmpty()) {
                append("No configured indicators were matched. This does not prove the source is safe.")
            } else {
                findings.forEach { (indicator, locations, count) ->
                    append("[").append(indicator.severity).append("] ")
                        .append(indicator.label).append(" — ")
                        .append(count).append(" match(es), line(s) ")
                        .append(locations.joinToString(", ")).append("\\n")
                }
            }
            append("\\nLIMITATION: Heuristic text matching only. Results require expert review and can contain false positives/negatives.")
            append("\\nThe submitted source was not run, compiled, downloaded, or transmitted.")
        }
    }

    fun simulateBenignBehavior(): String = """
        SAFE BEHAVIOR SIMULATION — MOCK DATA ONLY
        1. Sample requests access to a protected resource.
        2. Permission gate denies access until an explicit grant exists.
        3. Mock network event is recorded in memory; no packet is sent.
        4. Mock persistence request is flagged for analyst review.
        5. Emergency stop cancels the simulated task.
        
        RESULT: Completed safely. No files changed, no network traffic generated,
        no permissions requested, and no background persistence installed.
    """.trimIndent()
}
