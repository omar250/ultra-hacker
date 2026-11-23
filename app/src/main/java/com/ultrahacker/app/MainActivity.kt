package com.ultrahacker.app

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.*
import java.net.*
import javax.net.ssl.HttpsURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 9, 13)
    private val panel = Color.rgb(13, 21, 29)
    private val neon = Color.rgb(0, 245, 145)
    private val cyan = Color.rgb(0, 205, 220)
    private val muted = Color.rgb(145, 165, 177)
    private lateinit var content: LinearLayout
    private lateinit var terminalOutput: TextView
    private lateinit var terminalInput: EditText
    private lateinit var terminalScroll: ScrollView
    private var tab = "HOME"
    private val stopHandler = Handler(Looper.getMainLooper())
    @Volatile private var activeTask: Thread? = null
    @Volatile private var stopping = false
    private var autoStopRunnable: Runnable? = null
    private var autoStopAtMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildShell()
        showHome()
        restoreAutoStop()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun label(s: String, size: Float = 14f, color: Int = Color.WHITE, bold: Boolean = false) =
        TextView(this).apply {
            text = s; textSize = size; setTextColor(color)
            if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun buildShell() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(20), dp(16), dp(12))
            setBackgroundColor(bg)
        }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        header.addView(label("ULTRA", 25f, neon, true))
        header.addView(label(" HACKER", 25f, Color.WHITE, true))
        header.addView(label("  /  v4.0", 11f, cyan), LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(label("● READY", 10f, neon, true))
        root.addView(header)
        root.addView(label("ETHICAL SECURITY • NETWORK INTELLIGENCE", 10f, muted).apply {
            setPadding(0, dp(4), 0, dp(14))
        })
        val nav = LinearLayout(this).apply { setBackgroundColor(panel) }
        listOf("HOME", "TOOLS", "TERMINAL").forEach { name ->
            nav.addView(Button(this).apply {
                text = name; textSize = 10f; setTextColor(neon)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { tab = name; render() }
            }, LinearLayout.LayoutParams(0, dp(44), 1f))
        }
        root.addView(nav)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, 0)
        }
        val contentScroll = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }
        root.addView(contentScroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(label("AUTHORIZED TESTING ONLY • ULTRA HACKER", 9f, muted).apply {
            gravity = Gravity.CENTER; setPadding(0, dp(10), 0, 0)
        })
        setContentView(root)
    }

    private fun render() {
        content.removeAllViews()
        when (tab) { "HOME" -> showHome(); "TOOLS" -> showTools(); else -> showTerminal() }
    }

    private fun card(title: String, detail: String, action: () -> Unit) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
            setBackgroundColor(panel)
            setOnClickListener { action() }
        }
        box.addView(label(title, 15f, neon, true))
        box.addView(label(detail, 11f, muted).apply { setPadding(0, dp(4), 0, 0) })
        content.addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
    }

    private fun showHome() {
        content.addView(label("SECURITY CONSOLE", 21f, Color.WHITE, true))
        content.addView(label("Mobile toolkit for authorized diagnostics.", 12f, muted).apply {
            setPadding(0, dp(5), 0, dp(15))
        })
        val status = LinearLayout(this).apply {
            setBackgroundColor(panel); setPadding(dp(12), dp(14), dp(12), dp(14))
        }
        status.addView(label("● DEVICE READY", 11f, neon, true), LinearLayout.LayoutParams(0, -2, 1f))
        status.addView(label("ANDROID " + android.os.Build.VERSION.RELEASE, 11f, cyan, true))
        content.addView(status)
        content.addView(label("QUICK ACCESS", 12f, muted, true).apply {
            setPadding(0, dp(16), 0, dp(9))
        })
        card("⏱  AUTO STOP", autoStopDescription()) { showAutoStopDialog() }
        card("✦  SMART PHRASES", "Use Arabic or English phrases instead of command syntax") { tab = "TERMINAL"; render(); append("Try: اعرض الشبكة | افحص DNS example.com | اختبر الاتصال example.com | أوقف التطبيق بعد 10 دقائق") }
        card("⌘  ADVANCED TERMINAL", "DNS, ping, IP, RDAP and HTTPS header commands") {
            tab = "TERMINAL"; render()
        }
        card("◈  NETWORK TOOLS", "Local network details, device discovery and private port checks") {
            tab = "TOOLS"; render()
        }
        card("◎  WEBSITE SECURITY", "Inspect HTTPS security headers on a site you own") {
            tab = "TOOLS"; render()
        }
        card("◇  DNS & RDAP", "Resolve hostnames and retrieve public registration data") {
            tab = "TOOLS"; render()
        }
        card("◎  URL SAFETY REVIEW", "Review URL structure and common deception signals locally") {
            tab = "TOOLS"; render()
        }
    }

    private fun showTools() {
        content.addView(label("DIAGNOSTIC TOOLKIT", 20f, Color.WHITE, true))
        content.addView(label("Only test your own devices, networks and websites.", 11f, muted).apply {
            setPadding(0, dp(4), 0, dp(12))
        })
        card("LOCAL NETWORK", "Show active interfaces and local IPv4 addresses") {
            runTask("Reading network interfaces…") { localNetworkInfo() }
        }
        card("DNS LOOKUP", "Resolve a domain to IP addresses") {
            ask("DNS LOOKUP", "Hostname, e.g. example.com") { host -> runTask("Resolving $host…") { dnsLookup(host) } }
        }
        card("PING / REACHABILITY", "Test host reachability (ICMP may be blocked)") {
            ask("PING", "Hostname or IP address") { host -> runTask("Testing $host…") { pingHost(host) } }
        }
        card("LAN DEVICE DISCOVERY", "Probe a small sample of your current private /24 subnet") {
            runTask("Discovering local HTTP devices…") { discoverLan() }
        }
        card("PRIVATE PORT CHECK", "Check ports 22, 80, 443 and 8080 on a private IPv4 address") {
            ask("PRIVATE PORT CHECK", "Private IP, e.g. 192.168.1.10") { host ->
                runTask("Checking $host…") { checkPrivatePorts(host) }
            }
        }
        card("URL SAFETY REVIEW", "Inspect URL structure, HTTPS use and common deception signals locally") {
            ask("URL SAFETY REVIEW", "Paste a URL, e.g. https://example.com") { value ->
                runTask("Reviewing URL structure…") { urlSafetyReview(value) }
            }
        }
        card("HTTPS SECURITY HEADERS", "Review common browser security headers; no exploitation") {
            ask("HTTPS HEADER CHECK", "Domain, e.g. example.com") { host ->
                runTask("Inspecting HTTPS headers…") { inspectHeaders(host) }
            }
        }
        card("RDAP / WHOIS", "Query public domain registration data") {
            ask("RDAP LOOKUP", "Domain, e.g. example.com") { host ->
                runTask("Querying RDAP…") { rdapLookup(host) }
            }
        }
        card("INTERNET STATUS", "Check outbound HTTPS connectivity") {
            runTask("Checking internet…") { internetStatus() }
        }
        card("SSL / TLS CERTIFICATE", "Inspect certificate issuer, validity dates and hostname connection") {
            ask("SSL CERTIFICATE", "Domain, e.g. example.com") { host ->
                runTask("Inspecting TLS certificate…") { sslCertificate(host) }
            }
        }
        card("HTTP RESPONSE ANALYZER", "Check status code, content type and redirect behavior") {
            ask("HTTP ANALYZER", "HTTPS URL, e.g. https://example.com") { url ->
                runTask("Checking HTTP response…") { httpAnalysis(url) }
            }
        }
        card("SHA-256 HASH", "Generate a SHA-256 digest from text") {
            ask("SHA-256", "Enter text to hash") { value -> runTask("Generating SHA-256…") { sha256(value) } }
        }
        card("BASE64 TOOL", "Encode text to Base64") {
            ask("BASE64 ENCODE", "Enter text") { value -> runTask("Encoding Base64…") { base64Encode(value) } }
        }
        card("PASSWORD GENERATOR", "Generate a random 20-character password locally") {
            runTask("Generating secure password…") { generatePassword(20) }
        }
        card("NETWORK TRAFFIC", "Read this app's received and transmitted byte counters") {
            runTask("Reading app traffic counters…") { trafficStats() }
        }
        card("SCAN HISTORY", "Review the latest saved diagnostic results") {
            showScanHistory()
        }
        card("PDF REPORT", "Export saved scan results and recent terminal commands") {
            runTask("Creating PDF report…") { exportPdfReport() }
        }
    }

    private fun ask(title: String, hintText: String, action: (String) -> Unit) {
        val input = EditText(this).apply {
            hint = hintText; setTextColor(Color.WHITE); setHintTextColor(muted)
            setSingleLine(true); setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(panel)
        }
        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(14), dp(18), dp(8))
            addView(label(title, 17f, neon, true)); addView(input)
        }
        android.app.AlertDialog.Builder(this).setView(wrap)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("RUN") { _, _ -> input.text.toString().trim().takeIf { it.isNotEmpty() }?.let(action) }
            .show()
    }

    private fun runTask(message: String, task: () -> String) {
        if (stopping) return
        tab = "TERMINAL"; render(); append(message)
        activeTask?.interrupt()
        activeTask = thread(start = true, name = "ultra-diagnostic") {
            val result = try { task() } catch (e: Exception) {
                if (Thread.currentThread().isInterrupted) "Operation cancelled." else
                    "ERROR: " + (e.localizedMessage ?: e.javaClass.simpleName)
            }
            runOnUiThread {
                if (!isFinishing && !stopping) {
                    saveScanResult(message, result)
                    append(result)
                }
            }
        }
    }

    private fun showTerminal() {
        content.addView(label("COMMAND TERMINAL", 19f, Color.WHITE, true))
        content.addView(label("Type help to list supported commands.", 11f, muted).apply {
            setPadding(0, dp(4), 0, dp(10))
        })
        terminalOutput = label("", 12f, neon).apply {
            typeface = Typeface.MONOSPACE; setPadding(dp(10), dp(10), dp(10), dp(10))
            setTextIsSelectable(true)
        }
        terminalScroll = ScrollView(this).apply { setBackgroundColor(panel); addView(terminalOutput) }
        content.addView(terminalScroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        terminalInput = EditText(this).apply {
            hint = "اكتب مصطلحاً أو أمراً…"; setTextColor(Color.WHITE); setHintTextColor(muted)
            setSingleLine(true); inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEND; setBackgroundColor(panel)
            setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_SEND) { executeCommand(); true } else false
            }
        }
        row.addView(terminalInput, LinearLayout.LayoutParams(0, dp(52), 1f))
        row.addView(Button(this).apply { text = "RUN"; setTextColor(neon); setOnClickListener { executeCommand() } })
        row.addView(Button(this).apply { text = "CANCEL"; setTextColor(Color.YELLOW); setOnClickListener { cancelCurrentTask() } })
        content.addView(row)
        append("ULTRA HACKER ULTIMATE v5.0 ready.")
        append("Authorized diagnostics only. Type help.")
    }

    private fun append(line: String) {
        if (!::terminalOutput.isInitialized) return
        terminalOutput.append("› $line\n")
        if (::terminalScroll.isInitialized) terminalScroll.post { terminalScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun executeCommand() {
        val raw = terminalInput.text.toString().trim()
        if (raw.isEmpty()) return
        terminalInput.setText("")
        val normalized = normalizePhrase(raw)
        append(if (normalized == raw) raw else raw + "  →  " + normalized)
        saveTerminalCommand(raw)
        if (normalized.startsWith("autostop ")) {
            val minutes = normalized.substringAfter("autostop ").toLongOrNull()
            if (minutes == null || minutes !in 1..1440) append("استخدم: أوقف التطبيق بعد 10 دقائق (من 1 إلى 1440).")
            else scheduleAutoStop(minutes)
            return
        }
        if (normalized == "stopcancel") { cancelAutoStop(); append("تم إلغاء الإيقاف التلقائي."); return }
        val p = normalized.split(Regex("\\s+"), limit = 2)
        val arg = p.getOrNull(1)?.trim().orEmpty()
        when (p[0].lowercase(Locale.ROOT)) {
            "help" -> append("help | clear | date | whoami | ip | dns <host> | ping <host> | ports <private-ip> | rdap <domain> | headers <domain> | ssl <host> | http <url> | urlcheck <url> | sha256 <text> | b64encode <text> | b64decode <text> | traffic | passgen | history | report | netcheck | autostop <minutes> | stopcancel")
            "clear" -> terminalOutput.text = ""
            "date" -> append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            "whoami" -> append("ULTRA HACKER Android sandbox; no root access.")
            "ip" -> append(localNetworkInfo())
            "dns" -> if (arg.isBlank()) append("Usage: dns <hostname>") else runTask("DNS $arg") { dnsLookup(arg) }
            "ping" -> if (arg.isBlank()) append("Usage: ping <hostname>") else runTask("PING $arg") { pingHost(arg) }
            "ports" -> if (arg.isBlank()) append("Usage: ports <private-ip>") else runTask("PRIVATE PORT CHECK $arg") { checkPrivatePorts(arg) }
            "rdap" -> if (arg.isBlank()) append("Usage: rdap <domain>") else runTask("RDAP $arg") { rdapLookup(arg) }
            "headers" -> if (arg.isBlank()) append("Usage: headers <domain>") else runTask("HTTPS headers $arg") { inspectHeaders(arg) }
            "ssl" -> if (arg.isBlank()) append("Usage: ssl <hostname>") else runTask("SSL certificate") { sslCertificate(arg) }
            "http" -> if (arg.isBlank()) append("Usage: http <https-url>") else runTask("HTTP analysis") { httpAnalysis(arg) }
            "urlcheck" -> if (arg.isBlank()) append("Usage: urlcheck <url>") else runTask("URL safety review") { urlSafetyReview(arg) }
            "sha256" -> if (arg.isBlank()) append("Usage: sha256 <text>") else append(sha256(arg))
            "b64encode" -> if (arg.isBlank()) append("Usage: b64encode <text>") else append(base64Encode(arg))
            "b64decode" -> if (arg.isBlank()) append("Usage: b64decode <base64>") else append(base64Decode(arg))
            "traffic" -> append(trafficStats())
            "passgen" -> append(generatePassword(20))
            "history" -> append(terminalHistory())
            "report" -> append(exportPdfReport())
            "netcheck" -> runTask("Internet status") { internetStatus() }
            "autostop" -> { val minutes = arg.toLongOrNull(); if (minutes == null || minutes !in 1..1440) append("Usage: autostop <1..1440 minutes>") else scheduleAutoStop(minutes) }
            "stopcancel" -> { cancelAutoStop(); append("Automatic stop cancelled.") }
            "cancel" -> cancelCurrentTask()
            "stopnow", "exit" -> { cancelCurrentTask(); cancelAutoStop(); stopping = true; finishAndRemoveTask() }
            else -> append("Unknown phrase or command. Type help.")
        }
    }

    private fun normalizePhrase(input: String): String {
        val value = input.trim().replace(Regex("""\s+"""), " ")
        val lower = value.lowercase(Locale.ROOT)
        val patterns = listOf(
            Regex("""^(?:افحص dns|تحقق من dns|اعرف dns)\s+(.+)$""", RegexOption.IGNORE_CASE) to "dns",
            Regex("""^(?:اختبر الاتصال|افحص الاتصال|تحقق من الوصول)\s+(.+)$""", RegexOption.IGNORE_CASE) to "ping",
            Regex("""^(?:افحص الموقع|افحص ترويسات الموقع|تحقق من حماية الموقع)\s+(.+)$""", RegexOption.IGNORE_CASE) to "headers",
            Regex("""^(?:شهادة الموقع|افحص شهادة ssl|تحقق من الشهادة)\s+(.+)$""", RegexOption.IGNORE_CASE) to "ssl",
            Regex("""^(?:بيانات النطاق|معلومات النطاق|rdap)\s+(.+)$""", RegexOption.IGNORE_CASE) to "rdap",
            Regex("""^(?:افحص منافذ الجهاز الخاص|افحص المنافذ الخاصة)\s+(.+)$""", RegexOption.IGNORE_CASE) to "ports",
            Regex("""^(?:حلل الرابط|افحص استجابة الرابط)\s+(.+)$""", RegexOption.IGNORE_CASE) to "http",
            Regex("""^(?:احسب بصمة sha ?256|بصمة sha ?256)\s+(.+)$""", RegexOption.IGNORE_CASE) to "sha256",
            Regex("""^(?:شفّر base64|ترميز base64)\s+(.+)$""", RegexOption.IGNORE_CASE) to "b64encode",
            Regex("""^(?:فك base64|فك ترميز base64)\s+(.+)$""", RegexOption.IGNORE_CASE) to "b64decode",
            Regex("""^(?:أوقف التطبيق بعد|اوقف التطبيق بعد|إيقاف تلقائي بعد)\s+(\d+)\s*(?:دقيقة|دقائق|min|minutes?)$""", RegexOption.IGNORE_CASE) to "autostop",
            Regex("""^(?:حلل أمان الرابط|افحص أمان الرابط|تحقق من الرابط)\s+(.+)$""", RegexOption.IGNORE_CASE) to "urlcheck"
        )
        for ((regex, command) in patterns) {
            val match = regex.matchEntire(value)
            if (match != null) return command + " " + match.groupValues[1].trim()
        }
        return when (lower) {
            "اعرض الشبكة", "معلومات الشبكة", "عنوان ip", "اعرض ip", "الشبكة المحلية" -> "ip"
            "افحص الانترنت", "افحص الإنترنت", "تحقق من الانترنت", "حالة الانترنت", "حالة الإنترنت" -> "netcheck"
            "أنشئ كلمة مرور", "انشئ كلمة مرور", "ولّد كلمة مرور", "ولد كلمة مرور" -> "passgen"
            "اعرض السجل", "سجل الفحوصات", "التاريخ" -> "history"
            "أنشئ تقرير", "انشئ تقرير", "تقرير pdf", "تصدير التقرير" -> "report"
            "حركة البيانات", "استهلاك الشبكة" -> "traffic"
            "امسح الشاشة", "نظف الطرفية" -> "clear"
            "أوقف الإيقاف التلقائي", "الغ الإيقاف التلقائي", "إلغاء الإيقاف التلقائي" -> "stopcancel"
            else -> value
        }
    }

    private fun autoStopDescription(): String = if (autoStopAtMillis > 0L) {
        val remaining = ((autoStopAtMillis - System.currentTimeMillis()).coerceAtLeast(0L) / 60000L) + 1L
        "Scheduled to close in about " + remaining + " minute(s). Tap to change or cancel."
    } else "Choose a countdown to stop active work and close the app automatically."

    private fun showAutoStopDialog() {
        val options = arrayOf("إيقاف بعد 5 دقائق", "إيقاف بعد 10 دقائق", "إيقاف بعد 15 دقيقة", "إيقاف بعد 30 دقيقة", "إيقاف بعد ساعة", "إيقاف بعد ساعتين", "إلغاء الإيقاف التلقائي")
        val minutes = listOf(5L, 10L, 15L, 30L, 60L, 120L)
        android.app.AlertDialog.Builder(this).setTitle("الإيقاف التلقائي")
            .setItems(options) { _, which ->
                if (which == options.lastIndex) { cancelAutoStop(); Toast.makeText(this, "تم إلغاء المؤقت", Toast.LENGTH_SHORT).show() }
                else scheduleAutoStop(minutes[which])
            }.setNegativeButton("إغلاق", null).show()
    }

    private fun restoreAutoStop() {
        val deadline = prefs().getLong("auto_stop_deadline", 0L)
        if (deadline <= 0L) return
        val remaining = deadline - System.currentTimeMillis()
        if (remaining <= 0L) {
            prefs().edit().remove("auto_stop_deadline").apply()
            stopping = true
            activeTask?.interrupt()
            finishAndRemoveTask()
            return
        }
        autoStopAtMillis = deadline
        val task = Runnable {
            stopping = true
            prefs().edit().remove("auto_stop_deadline").apply()
            activeTask?.interrupt()
            Toast.makeText(this, "انتهى المؤقت؛ يتم إيقاف ULTRA HACKER", Toast.LENGTH_SHORT).show()
            finishAndRemoveTask()
        }
        autoStopRunnable = task
        stopHandler.postDelayed(task, remaining)
    }

    private fun scheduleAutoStop(minutes: Long) {
        require(minutes in 1..1440) { "Choose 1 to 1440 minutes." }
        cancelAutoStop()
        stopping = false
        autoStopAtMillis = System.currentTimeMillis() + minutes * 60_000L
        prefs().edit().putLong("auto_stop_deadline", autoStopAtMillis).apply()
        val task = Runnable {
            stopping = true
            prefs().edit().remove("auto_stop_deadline").apply()
            activeTask?.interrupt()
            Toast.makeText(this, "انتهى المؤقت؛ يتم إيقاف ULTRA HACKER", Toast.LENGTH_SHORT).show()
            finishAndRemoveTask()
        }
        autoStopRunnable = task
        stopHandler.postDelayed(task, minutes * 60_000L)
        Toast.makeText(this, "سيُغلق التطبيق بعد " + minutes + " دقيقة", Toast.LENGTH_LONG).show()
    }

    private fun cancelCurrentTask() {
        val task = activeTask
        if (task != null && task.isAlive) {
            task.interrupt()
            activeTask = null
            append("Cancellation requested for the active diagnostic task.")
        } else append("No active task to cancel.")
    }

    private fun cancelAutoStop() {
        autoStopRunnable?.let { stopHandler.removeCallbacks(it) }
        autoStopRunnable = null
        autoStopAtMillis = 0L
        prefs().edit().remove("auto_stop_deadline").apply()
        stopping = false
    }

    override fun onDestroy() {
        autoStopRunnable?.let { stopHandler.removeCallbacks(it) }
        activeTask?.interrupt()
        super.onDestroy()
    }
    private fun localNetworkInfo(): String {
        val out = StringBuilder()
        val all = NetworkInterface.getNetworkInterfaces() ?: return "No interfaces found."
        while (all.hasMoreElements()) {
            val ni = all.nextElement()
            if (!ni.isUp || ni.isLoopback) continue
            out.append("\n" + ni.name + " (" + ni.displayName + ")\n")
            val addresses = ni.inetAddresses
            while (addresses.hasMoreElements()) out.append("  " + addresses.nextElement().hostAddress + "\n")
        }
        return out.toString().ifBlank { "No active network address found." }
    }

    private fun validHost(host: String) =
        host.length in 1..253 && host.matches(Regex("[A-Za-z0-9.-]+")) &&
            !host.startsWith(".") && !host.endsWith(".") && !host.contains("..")

    private fun dnsLookup(host: String): String {
        require(validHost(host)) { "Enter a valid hostname." }
        return InetAddress.getAllByName(host).joinToString("\n") { it.hostAddress }
    }

    private fun pingHost(host: String): String {
        require(validHost(host)) { "Enter a valid hostname or IP." }
        val address = InetAddress.getByName(host)
        val start = System.currentTimeMillis()
        val ok = address.isReachable(3000)
        return "Target: " + address.hostAddress + "\nReachable: $ok\nElapsed: " +
            (System.currentTimeMillis() - start) + " ms\nICMP may be blocked by some networks."
    }

    private fun privateIpv4(host: String): Boolean {
        val a = InetAddress.getByName(host)
        if (a !is Inet4Address) return false
        val b = a.address.map { it.toInt() and 255 }
        return b[0] == 10 || (b[0] == 172 && b[1] in 16..31) ||
            (b[0] == 192 && b[1] == 168) || b[0] == 127
    }

    private fun checkPrivatePorts(host: String): String {
        require(privateIpv4(host)) { "Only private IPv4 addresses are allowed." }
        val out = StringBuilder("Private host $host\n")
        for (port in listOf(22, 80, 443, 8080)) {
            val open = try {
                Socket().use { it.connect(InetSocketAddress(host, port), 650); true }
            } catch (_: Exception) { false }
            out.append("$port: " + if (open) "OPEN\n" else "closed / filtered\n")
        }
        return out.toString()
    }

    private fun discoverLan(): String {
        val local = NetworkInterface.getNetworkInterfaces().toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { it is Inet4Address && it.isSiteLocalAddress }
            ?: return "No private IPv4 address found."
        val b = local.address.map { it.toInt() and 255 }
        val prefix = "${b[0]}.${b[1]}.${b[2]}."
        val found = mutableListOf<String>()
        for (i in 1..30) {
            val host = prefix + i
            if (host == local.hostAddress) continue
            try { Socket().use { it.connect(InetSocketAddress(host, 80), 180) }; found.add(host) }
            catch (_: Exception) { }
        }
        return "Local device: " + local.hostAddress +
            "\nHTTP responders in sampled .1-.30 range:\n" +
            found.joinToString("\n").ifBlank { "No HTTP responders found." }
    }

    private fun inspectHeaders(domain: String): String {
        val host = domain.removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
        require(validHost(host)) { "Enter a valid domain." }
        val c = URL("https://$host").openConnection() as HttpURLConnection
        c.requestMethod = "HEAD"; c.connectTimeout = 5000; c.readTimeout = 5000
        return try {
            c.connect()
            val names = listOf("Strict-Transport-Security", "Content-Security-Policy",
                "X-Content-Type-Options", "X-Frame-Options", "Referrer-Policy", "Permissions-Policy")
            val out = StringBuilder("HTTPS status: ${c.responseCode}\n")
            for (name in names) out.append("$name: " + (c.getHeaderField(name) ?: "MISSING") + "\n")
            out.toString()
        } finally { c.disconnect() }
    }

    private fun rdapLookup(domain: String): String {
        val host = domain.trim().lowercase(Locale.ROOT)
        require(validHost(host)) { "Enter a valid domain." }
        val c = URL("https://rdap.org/domain/$host").openConnection() as HttpURLConnection
        c.connectTimeout = 6000; c.readTimeout = 6000
        c.setRequestProperty("Accept", "application/rdap+json")
        return try {
            val stream = if (c.responseCode in 200..299) c.inputStream else c.errorStream
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }
            "RDAP HTTP ${c.responseCode}\n" + body.take(3000)
        } finally { c.disconnect() }
    }

    private fun internetStatus(): String {
        val c = URL("https://www.google.com/generate_204").openConnection() as HttpURLConnection
        c.connectTimeout = 4000; c.readTimeout = 4000
        return try { c.connect(); "HTTPS status: ${c.responseCode}\nConnected: ${c.responseCode in 200..399}" }
        finally { c.disconnect() }
    }
    private fun sslCertificate(domain: String): String {
        val host = domain.removePrefix("https://").removePrefix("http://").substringBefore('/').trim()
        require(validHost(host)) { "Enter a valid hostname." }
        val c = URL("https://" + host).openConnection() as HttpsURLConnection
        c.connectTimeout = 6000
        c.readTimeout = 6000
        return try {
            c.connect()
            val cert = c.serverCertificates.firstOrNull() as? java.security.cert.X509Certificate
                ?: return "The server did not provide an X.509 certificate."
            val now = Date()
            val valid = try { cert.checkValidity(now); true } catch (_: Exception) { false }
            "TLS connection: OK\nSubject: " + cert.subjectX500Principal.name +
                "\nIssuer: " + cert.issuerX500Principal.name +
                "\nValid from: " + cert.notBefore +
                "\nValid until: " + cert.notAfter +
                "\nCurrently valid: " + valid +
                "\nNegotiated cipher suite: " + c.cipherSuite
        } finally { c.disconnect() }
    }

    private fun httpAnalysis(rawUrl: String): String {
        val value = if (rawUrl.startsWith("https://") || rawUrl.startsWith("http://")) rawUrl else "https://" + rawUrl
        val u = URL(value)
        require(u.protocol == "https" || u.protocol == "http") { "Only HTTP or HTTPS URLs are supported." }
        val c = u.openConnection() as HttpURLConnection
        c.requestMethod = "HEAD"
        c.connectTimeout = 6000
        c.readTimeout = 6000
        c.instanceFollowRedirects = false
        return try {
            c.connect()
            val names = listOf("Strict-Transport-Security", "Content-Security-Policy",
                "X-Content-Type-Options", "X-Frame-Options", "Referrer-Policy", "Permissions-Policy")
            val out = StringBuilder()
            out.append("URL: ").append(value).append("\nHTTP status: ").append(c.responseCode)
            out.append("\nContent-Type: ").append(c.contentType ?: "not provided")
            out.append("\nRedirect: ").append(c.getHeaderField("Location") ?: "none")
            for (name in names) out.append("\n").append(name).append(": ").append(c.getHeaderField(name) ?: "MISSING")
            out.toString()
        } finally { c.disconnect() }
    }

    private fun urlSafetyReview(rawUrl: String): String {
        val value = rawUrl.trim()
        require(value.isNotEmpty() && !value.any { it.isWhitespace() }) { "Enter a URL without spaces." }
        val candidate = if (value.contains("://")) value else "https://$value"
        val uri = try { URI(candidate) } catch (_: Exception) {
            throw IllegalArgumentException("The URL format is invalid.")
        }
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        require(scheme == "https" || scheme == "http") { "Only HTTP and HTTPS URLs are supported." }
        val host = uri.host ?: throw IllegalArgumentException("A valid hostname is required.")
        val signals = mutableListOf<String>()
        if (scheme != "https") signals.add("WARNING: HTTP does not encrypt the connection.")
        else signals.add("HTTPS scheme is present; certificate validity was not tested.")
        if (uri.rawUserInfo != null) signals.add("WARNING: URL contains user-info before the host; this can disguise the destination.")
        if (host.split(".").any { it.startsWith("xn--", true) }) {
            signals.add("NOTICE: Internationalized/punycode hostname; verify the spelling carefully.")
        }
        if (host.matches(Regex("""\d{1,3}(?:\.\d{1,3}){3}""")) || host.startsWith("[") || host.contains(":")) {
            signals.add("NOTICE: URL uses an IP-literal host instead of a conventional domain name.")
        }
        if (uri.port != -1 && uri.port !in listOf(80, 443)) signals.add("NOTICE: Non-standard port ${uri.port} is specified.")
        if (uri.rawFragment != null) signals.add("INFO: URL contains a fragment; fragments are normally handled by the browser.")
        if (signals.isEmpty()) signals.add("No basic structural warning was detected.")
        return "URL: $candidate\nHost: $host\nScheme: $scheme\n\nSTRUCTURAL REVIEW\n" +
            signals.joinToString("\n") +
            "\n\nLIMITATION: Local heuristic only; not malware detection, reputation checking or a safety guarantee."
    }

    private fun sha256(value: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun base64Encode(value: String): String =
        android.util.Base64.encodeToString(value.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

    private fun base64Decode(value: String): String =
        String(android.util.Base64.decode(value, android.util.Base64.DEFAULT), Charsets.UTF_8)

    private fun generatePassword(length: Int): String {
        require(length in 12..64) { "Password length must be between 12 and 64." }
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#%+=_-"
        val random = java.security.SecureRandom()
        return (1..length).map { chars[random.nextInt(chars.length)] }.joinToString("")
    }

    private fun trafficStats(): String {
        val uid = android.os.Process.myUid()
        val rx = android.net.TrafficStats.getUidRxBytes(uid)
        val tx = android.net.TrafficStats.getUidTxBytes(uid)
        fun format(bytes: Long): String = if (bytes < 0) "Unavailable" else
            String.format(Locale.US, "%.2f MB (%d bytes)", bytes / 1048576.0, bytes)
        return "App UID: " + uid + "\nReceived: " + format(rx) + "\nTransmitted: " + format(tx) +
            "\nNote: Android reports cumulative app traffic since boot."
    }

    private fun prefs() = getSharedPreferences("ultra_hacker_v35", MODE_PRIVATE)

    private fun saveScanResult(title: String, result: String) {
        val old = prefs().getString("scan_history", "").orEmpty()
        val encoded = android.util.Base64.encodeToString(
            (title + "\n" + result).toByteArray(Charsets.UTF_8),
            android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE
        )
        val rows = (listOf(System.currentTimeMillis().toString() + "|" + encoded) +
            old.lines().filter { it.contains("|") }).take(50)
        prefs().edit().putString("scan_history", rows.joinToString("\n")).apply()
    }

    private fun scanHistoryEntries(): List<Pair<Long, String>> =
        prefs().getString("scan_history", "").orEmpty().lines().mapNotNull { line ->
            val parts = line.split("|", limit = 2)
            if (parts.size != 2) null else try {
                val timestamp = parts[0].toLong()
                val decoded = String(
                    android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP or android.util.Base64.URL_SAFE),
                    Charsets.UTF_8
                )
                timestamp to decoded
            } catch (_: Exception) { null }
        }

    private fun showScanHistory() {
        tab = "TERMINAL"
        render()
        append("SAVED SCAN HISTORY (latest 50)")
        val entries = scanHistoryEntries()
        if (entries.isEmpty()) {
            append("No saved scans yet. Run a tool first.")
            return
        }
        entries.take(20).forEach { (time, detail) ->
            val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(time))
            append("[$stamp] $detail")
        }
    }

    private fun saveTerminalCommand(command: String) {
        val old = prefs().getString("terminal_history", "").orEmpty().lines().filter { it.isNotBlank() }
        prefs().edit().putString("terminal_history", (old + command).takeLast(100).joinToString("\n")).apply()
    }

    private fun terminalHistory(): String {
        val entries = prefs().getString("terminal_history", "").orEmpty().lines().filter { it.isNotBlank() }
        return if (entries.isEmpty()) "No terminal commands saved."
        else "RECENT TERMINAL COMMANDS\n" + entries.takeLast(30).joinToString("\n")
    }

    private fun exportPdfReport(): String {
        val scans = scanHistoryEntries()
        val commands = prefs().getString("terminal_history", "").orEmpty().lines().filter { it.isNotBlank() }.takeLast(30)
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            isAntiAlias = true
        }
        val titlePaint = android.graphics.Paint(paint).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = 42f
        fun newPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = 42f
        }
        fun line(text: String, bold: Boolean = false) {
            val p = if (bold) titlePaint else paint
            val maxChars = if (bold) 42 else 88
            text.chunked(maxChars).forEach { chunk ->
                if (y > pageHeight - 45) newPage()
                canvas.drawText(chunk, 36f, y, p)
                y += if (bold) 28f else 15f
            }
        }
        line("ULTRA HACKER ULTIMATE 5.0 SECURITY REPORT", true)
        line("Generated: " + Date().toString())
        line("For authorized diagnostic use only.")
        y += 12f
        line("SAVED SCAN RESULTS", true)
        if (scans.isEmpty()) line("No scan results have been saved.")
        scans.forEach { (time, detail) ->
            line(SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(time)))
            detail.lines().take(12).forEach { line(it.take(160)) }
            y += 8f
        }
        y += 12f
        line("RECENT TERMINAL COMMANDS", true)
        if (commands.isEmpty()) line("No commands have been saved.")
        commands.forEach { line(it) }
        document.finishPage(page)
        val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: filesDir
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "ultra-hacker-report-" + System.currentTimeMillis() + ".pdf")
        try {
            FileOutputStream(file).use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return "PDF report created successfully.\nSaved to: " + file.absolutePath +
            "\nScans included: " + scans.size + "\nTerminal commands included: " + commands.size
    }

}
