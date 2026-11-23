package com.ultrahacker.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.*
import java.net.*
import javax.net.ssl.HttpsURLConnection
import java.io.BufferedReader
import java.io.InputStreamReader
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildShell()
        showHome()
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
        header.addView(label("  /  v2.0", 11f, cyan), LinearLayout.LayoutParams(0, -2, 1f))
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
        tab = "TERMINAL"; render(); append(message)
        thread {
            val result = try { task() } catch (e: Exception) {
                "ERROR: " + (e.localizedMessage ?: e.javaClass.simpleName)
            }
            runOnUiThread { append(result) }
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
            hint = "command…"; setTextColor(Color.WHITE); setHintTextColor(muted)
            setSingleLine(true); inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEND; setBackgroundColor(panel)
            setOnEditorActionListener { _, id, _ ->
                if (id == EditorInfo.IME_ACTION_SEND) { executeCommand(); true } else false
            }
        }
        row.addView(terminalInput, LinearLayout.LayoutParams(0, dp(52), 1f))
        row.addView(Button(this).apply { text = "RUN"; setTextColor(neon); setOnClickListener { executeCommand() } })
        content.addView(row)
        append("ULTRA HACKER v2.0 ready.")
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
        terminalInput.setText(""); append(raw)
        val p = raw.split(Regex("\\s+"), limit = 2)
        val arg = p.getOrNull(1)?.trim().orEmpty()
        when (p[0].lowercase(Locale.ROOT)) {
            "help" -> append("help | clear | date | whoami | ip | dns <host> | ping <host> | rdap <domain> | headers <domain> | ssl <host> | http <url> | sha256 <text> | b64encode <text> | b64decode <text> | traffic | passgen | netcheck")
            "clear" -> terminalOutput.text = ""
            "date" -> append(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            "whoami" -> append("ULTRA HACKER Android sandbox; no root access.")
            "ip" -> append(localNetworkInfo())
            "dns" -> if (arg.isBlank()) append("Usage: dns <hostname>") else runTask("DNS $arg") { dnsLookup(arg) }
            "ping" -> if (arg.isBlank()) append("Usage: ping <hostname>") else runTask("PING $arg") { pingHost(arg) }
            "rdap" -> if (arg.isBlank()) append("Usage: rdap <domain>") else runTask("RDAP $arg") { rdapLookup(arg) }
            "headers" -> if (arg.isBlank()) append("Usage: headers <domain>") else runTask("HTTPS headers $arg") { inspectHeaders(arg) }
            "ssl" -> if (arg.isBlank()) append("Usage: ssl <hostname>") else runTask("SSL certificate") { sslCertificate(arg) }
            "http" -> if (arg.isBlank()) append("Usage: http <https-url>") else runTask("HTTP analysis") { httpAnalysis(arg) }
            "sha256" -> if (arg.isBlank()) append("Usage: sha256 <text>") else append(sha256(arg))
            "b64encode" -> if (arg.isBlank()) append("Usage: b64encode <text>") else append(base64Encode(arg))
            "b64decode" -> if (arg.isBlank()) append("Usage: b64decode <base64>") else append(base64Decode(arg))
            "traffic" -> append(trafficStats())
            "passgen" -> append(generatePassword(20))
            "netcheck" -> runTask("Internet status") { internetStatus() }
            else -> append("Unknown command. Type help.")
        }
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

}
