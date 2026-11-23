# ULTRA HACKER ULTIMATE 5.0

Native Android toolkit for authorized cybersecurity learning, troubleshooting and defensive diagnostics.

## Included capabilities
- Dark mobile dashboard with HOME, TOOLS and TERMINAL.
- DNS resolution, reachability checks, local interface details and bounded discovery on the user's local private subnet.
- Restricted TCP checks for ports 22, 80, 443 and 8080 on private IPv4 addresses only.
- HTTPS security-header inspection, TLS certificate details and HTTP response analysis.
- RDAP domain registration lookup.
- Local URL structure review for basic deception indicators (not a malware/reputation scanner).
- SHA-256 hashing, Base64 encode/decode and cryptographically secure local password generation.
- Per-app cumulative network traffic counters.
- Persistent local scan history and terminal command history.
- PDF report export.
- Arabic/English smart phrases mapped to a fixed allowlist of diagnostic actions. No arbitrary shell execution or remote command execution.
- Automatic stop timer (1–1440 minutes), persisted across Activity recreation, with cancellation.
- Active diagnostic task cancellation from the terminal CANCEL button or the `cancel` command.
- Commands: `help`, `clear`, `date`, `whoami`, `ip`, `dns`, `ping`, `ports`, `rdap`, `headers`, `ssl`, `http`, `urlcheck`, `sha256`, `b64encode`, `b64decode`, `traffic`, `passgen`, `history`, `report`, `netcheck`, `autostop`, `stopcancel`, `cancel`, `stopnow`.

## Smart phrase examples
- `اعرض الشبكة` → `ip`
- `افحص DNS example.com` → `dns example.com`
- `اختبر الاتصال example.com` → `ping example.com`
- `افحص الموقع example.com` → `headers example.com`
- `حلل أمان الرابط https://example.com` → `urlcheck`
- `أوقف التطبيق بعد 10 دقائق` → schedule automatic stop
- `أوقف الإيقاف التلقائي` → cancel timer
- `أوقف المهمة` → request cancellation of the active diagnostic
- `أوقف التطبيق الآن` → close the app

## Safety and limitations
- This is an Android diagnostics app, not a root shell, full Linux terminal, exploit framework or autonomous hacking agent.
- Network checks are intended for devices, networks and websites you own or are explicitly authorized to assess.
- Port checks are restricted to private IPv4 targets and a small fixed port list.
- URL review is heuristic only and cannot guarantee a site is safe.
- Auto-stop requests Android to close the app and interrupts the active task; Android may still control process lifecycle.
- ICMP reachability may be blocked by the network.
- Traffic counters are cumulative UID counters since boot, not live throughput.
- Reports are stored in the app-specific Documents directory.

## Build
Push to `main` or manually run **Build Android APK** in GitHub Actions. Download the artifact `ultra-hacker-ultimate-v5.0-debug-apk`.
