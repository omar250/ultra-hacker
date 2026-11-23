package com.ultrahacker.app

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build

/**
 * Read-only Android security baseline for the device running this app.
 * It reports public build metadata only; it does not inspect protected data,
 * lock-screen credentials, other apps, or alter device settings.
 */
object DevicePosture {
    fun report(context: Context): String {
        val appInfo = context.applicationInfo
        val appDebuggable = (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val fingerprint = Build.FINGERPRINT.orEmpty()
        val emulator = fingerprint.startsWith("generic", ignoreCase = true) ||
            fingerprint.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("sdk", ignoreCase = true)
        val testKeys = Build.TAGS.orEmpty().contains("test-keys", ignoreCase = true)
        val patch = Build.VERSION.SECURITY_PATCH.ifBlank { "Not reported by this build" }

        return buildString {
            append("ANDROID SECURITY BASELINE — READ ONLY\n")
            append("Manufacturer: ").append(Build.MANUFACTURER).append("\n")
            append("Brand / model: ").append(Build.BRAND).append(" / ").append(Build.MODEL).append("\n")
            append("Android release: ").append(Build.VERSION.RELEASE).append("\n")
            append("API level: ").append(Build.VERSION.SDK_INT).append("\n")
            append("Security patch: ").append(patch).append("\n")
            append("Build tags: ").append(Build.TAGS ?: "Not reported").append("\n")
            append("Test-keys build: ").append(if (testKeys) "YES — verify firmware source" else "Not detected").append("\n")
            append("Emulator indicators: ").append(if (emulator) "YES" else "Not detected").append("\n")
            append("This app debuggable: ").append(if (appDebuggable) "YES" else "NO").append("\n")
            append("Supported ABIs: ").append(Build.SUPPORTED_ABIS.joinToString(", ")).append("\n\n")
            append("RECOMMENDATIONS\n")
            append("• Install Android and vendor security updates.\n")
            append("• Keep Google Play Protect enabled where available.\n")
            append("• Review app permissions and remove apps you do not trust.\n")
            append("• Use a strong screen lock and keep device encryption enabled.\n\n")
            append("LIMITATIONS\n")
            append("Read-only metadata checks are indicators, not proof of device integrity. ")
            append("This tool cannot verify the screen lock, decrypt user data, inspect other apps, ")
            append("or determine whether the device is compromised.")
        }
    }
}
