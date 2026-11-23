package com.ultrahacker.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private val backgroundColor = Color.rgb(5, 10, 8)
    private val panelColor = Color.rgb(12, 22, 17)
    private val neonColor = Color.rgb(0, 255, 120)
    private val mutedColor = Color.rgb(150, 180, 160)

    private lateinit var output: TextView
    private lateinit var commandInput: EditText
    private lateinit var outputScroll: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        appendLine("ULTRA HACKER v1.0")
        appendLine("Authorized diagnostics only. Type help.")
        appendLine("----------------------------------------")
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 24, 18, 18)
            setBackgroundColor(backgroundColor)
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "ULTRA HACKER"
            textSize = 24f
            setTextColor(neonColor)
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val languageButton = Button(this).apply {
            text = "عربي / EN"
            setOnClickListener {
                title.text = if (title.text.toString() == "ULTRA HACKER") {
                    "ألترا هاكر"
                } else {
                    "ULTRA HACKER"
                }
            }
        }

        header.addView(title, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(languageButton)
        root.addView(header)

        root.addView(TextView(this).apply {
            text = "ETHICAL CYBERSECURITY TOOLKIT • LOCAL DIAGNOSTICS"
            textSize = 10f
            setTextColor(mutedColor)
            setPadding(0, 4, 0, 18)
        })

        root.addView(TextView(this).apply {
            text = "[ NETWORK ]     [ DNS ]     [ TERMINAL ]"
            textSize = 13f
            setTextColor(neonColor)
            setBackgroundColor(panelColor)
            setPadding(12, 14, 12, 14)
        })

        output = TextView(this).apply {
            textSize = 13f
            setTextColor(neonColor)
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(12, 12, 12, 12)
            setTextIsSelectable(true)
        }

        outputScroll = ScrollView(this).apply {
            setBackgroundColor(panelColor)
            addView(output)
        }

        root.addView(outputScroll, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ).apply {
            topMargin = 14
            bottomMargin = 12
        })

        val inputRow = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        commandInput = EditText(this).apply {
            hint = "Enter command (help)"
            setHintTextColor(mutedColor)
            setTextColor(Color.WHITE)
            textSize = 14f
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEND
            setBackgroundColor(panelColor)
            setPadding(12, 8, 12, 8)
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    runCommand()
                    true
                } else {
                    false
                }
            }
        }

        inputRow.addView(commandInput, LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ))

        inputRow.addView(Button(this).apply {
            text = "RUN"
            setTextColor(neonColor)
            setOnClickListener { runCommand() }
        })

        root.addView(inputRow)
        setContentView(root)
    }

    private fun appendLine(message: String) {
        output.append("$ $message\n")
        outputScroll.post { outputScroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun runCommand() {
        val rawCommand = commandInput.text.toString().trim()
        if (rawCommand.isEmpty()) return

        commandInput.setText("")
        appendLine(rawCommand)

        val parts = rawCommand.split(Regex("\\s+"), limit = 2)
        when (parts[0].lowercase(Locale.ROOT)) {
            "help" -> {
                appendLine("Commands: help, clear, date, whoami, ip, dns <hostname>, ping <hostname>")
                appendLine("Run diagnostics only against systems you own or are authorized to test.")
            }
            "clear" -> output.text = ""
            "date" -> appendLine(
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            "whoami" -> appendLine("ultra-hacker (Android app sandbox)")
            "ip" -> appendLine("Local IPv4: ${getLocalIpv4()}")
            "dns", "ping" -> {
                val host = parts.getOrNull(1)?.trim().orEmpty()
                if (host.isBlank() || host.any { it.isWhitespace() }) {
                    appendLine("Usage: ${parts[0]} <hostname>")
                    return
                }

                appendLine("Checking $host ...")
                thread {
                    val result = try {
                        val address = InetAddress.getByName(host)
                        if (parts[0].equals("dns", ignoreCase = true)) {
                            "DNS: ${address.hostAddress ?: "No address"}"
                        } else {
                            val reachable = address.isReachable(3000)
                            "Resolved: ${address.hostAddress}; reachable=$reachable"
                        }
                    } catch (exception: Exception) {
                        "Error: ${exception.localizedMessage ?: "Network lookup failed"}"
                    }

                    runOnUiThread { appendLine(result) }
                }
            }
            else -> appendLine("Unknown command. Type help.")
        }
    }

    private fun getLocalIpv4(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val addresses = interfaces.nextElement().inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress &&
                        address.hostAddress?.contains(':') == false
                    ) {
                        return address.hostAddress ?: "Unavailable"
                    }
                }
            }
            "Unavailable"
        } catch (_: Exception) {
            "Unavailable"
        }
    }
}
