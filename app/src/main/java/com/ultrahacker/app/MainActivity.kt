package com.ultrahacker.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.*
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 10, 8)
    private val panel = Color.rgb(12, 22, 17)
    private val neon = Color.rgb(0, 255, 120)
    private val muted = Color.rgb(150, 180, 160)
    private lateinit var output: TextView
    private lateinit var command: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        printLine("ULTRA HACKER v1.0")
        printLine("Authorized diagnostics only. Type help.")
        printLine("----------------------------------------")
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 24, 18, 18)
            setBackgroundColor(bg)
        }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "ULTRA HACKER"
            textSize = 24f
            setTextColor(neon)
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val lang = Button(this).apply {
            text = "عربي / EN"
            setOnClickListener {
                title.text = if (title.text == "ULTRA HACKER") "ألترا هاكر" else "ULTRA HACKER"
            }
        }
        top.addView(title, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(lang)
        root.addView(top)
        root.addView(TextView(this).apply {
            text = "ETHICAL CYBERSECURITY TOOLKIT • LOCAL DIAGNOSTICS"
            textSize = 10f
            setTextColor(muted)
            setPadding(0, 4, 0, 18)
        })
        root.addView(TextView(this).apply {
            text = "[ NETWORK ]     [ DNS ]     [ TERMINAL ]"
            textSize = 13f
            setTextColor(neon)
            setBackgroundColor(panel)
            setPadding(12, 14, 12, 14)
        })
        output = TextView(this).apply {
            textSize = 13f
            setTextColor(neon)
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(12, 12, 12, 12)
            setTextIsSelectable(true)
        }
        val scroll = ScrollView(this).apply {
            setBackgroundColor(panel)
            addView(output)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = 14
            bottomMargin = 12
        })
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        command = EditText(this).apply {
            hint = "Enter command (help)"
            setHintTextColor(muted)
            setTextColor(Color.WHITE)
            textSize = 14f
            singleLine = true
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEND
            setBackgroundColor(panel)
            setPadding(12, 8, 12, 8)
            setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_SEND) { runCommand(); true } else false
            }
        }
        row.addView(command, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(Button(this).apply {
            text = "RUN"
            setTextColor(neon)
            setOnClickListener { runCommand() }
        })
        root.addView(row)
        setContentView(root)
    }

    private fun printLine(s: String) { output.append("$ $s\n") }

    private fun runCommand() {
        val raw = command.text.toString().trim()
        if (raw.isEmpty()) return
        command.setText("")
        printLine(raw)
        val parts = raw.split(Regex("\\s+"), limit = 2)
        when (parts[0].lowercase(Locale.ROOT)) {
            "help" -> {
                printLine("Available: help, clear, date, whoami, ip, dns <hostname>, ping <hostname>")
                printLine("Use only on systems you own or are authorized to test.")
            }
            "clear" -> output.text = ""
            "date" -> printLine(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            "whoami" -> printLine("ultra-hacker (Android app sandbox)")
            "ip" -> printLine("Local IPv4: ${localIp()}")
            "dns", "ping" -> {
                val host = parts.getOrNull(1)?.trim().orEmpty()
                if (host.isBlank() || host.contains(" ")) printLine("Usage: ${parts[0]} <hostname>")
                else {
                    printLine("Working on $host ...")
                    thread {
                        val result = try {
                            val address = InetAddress.getByName(host)
                            if (parts[0].equals("dns", true)) "DNS: ${address.hostAddress}"
                            else "Resolved ${address.hostAddress}; reachable=${address.isReachable(3000)}"
                        } catch (e: Exception) { "Error: ${e.localizedMessage ?: "lookup failed"}" }
                        runOnUiThread { printLine(result) }
                    }
                }
            }
            else -> printLine("Unknown command. Type help.")
        }
    }

    private fun localIp(): String = try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val addresses = interfaces.nextElement().inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address.hostAddress?.contains(':') == false)
                    return address.hostAddress ?: "Unavailable"
            }
        }
        "Unavailable"
    } catch (_: Exception) { "Unavailable" }
}
