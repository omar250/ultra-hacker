# ULTRA HACKER ULTIMATE 6.0

Native Android toolkit for authorized cybersecurity learning, device inventory, troubleshooting and defensive diagnostics.

## Included tools

### Network and web diagnostics
- Local network interface and IPv4 information.
- DNS lookup and reachability checks.
- Bounded discovery of HTTP responders on a small sample of the current private subnet.
- Restricted TCP checks for ports 22, 80, 443 and 8080 on private IPv4 targets.
- HTTPS security-header inspection, TLS certificate details and HTTP response analysis.
- RDAP domain registration lookup and local URL-structure review.

### Android device and app posture
- Read-only Android baseline: manufacturer/model, Android release, API level, vendor security patch, build tags, emulator indicators and this app's debuggable flag.
- Permission report limited to permissions declared and granted by ULTRA HACKER itself.
- No access to screen-lock credentials, protected user data or other apps' private storage.

### Research and utilities
- Static source-code indicator review with SHA-256 and line numbers. Submitted source is never compiled, loaded or executed.
- Harmless behavior simulator with fixed mock events only.
- SHA-256 hashing, Base64 encode/decode and cryptographically secure local password generation.
- Per-app cumulative network traffic counters.
- Persistent local scan history and terminal command history.
- PDF report export.

### Terminal and controls
- Arabic/English smart phrases mapped to a fixed allowlist of diagnostic actions.
- Persistent automatic-stop timer (1–1440 minutes), with cancellation.
- Active diagnostic cancellation from the terminal.
- Commands include: `help`, `clear`, `date`, `whoami`, `devicecheck`, `ip`, `dns`, `ping`, `ports`, `rdap`, `headers`, `ssl`, `http`, `urlcheck`, `sha256`, `b64encode`, `b64decode`, `traffic`, `passgen`, `history`, `report`, `netcheck`, `autostop`, `stopcancel`, `cancel`, `stopnow`.

Example Arabic phrase: `افحص أمان الهاتف` runs the read-only Android baseline.

## Safety and limitations
- This is a diagnostic and research app, not a root shell, exploit framework, lock-screen bypass, credential recovery tool or autonomous hacking agent.
- Test only devices, networks and websites you own or are explicitly authorized to assess.
- Network port checks are limited to private IPv4 addresses and a small fixed port list.
- URL review is heuristic only; it is not malware detection, reputation checking or a safety guarantee.
- Malware research features are offline static analysis and harmless simulation only. No payload execution, persistence installation, credential access, evasion or propagation is implemented.
- Android baseline results are public build metadata indicators, not proof of device integrity. Screen-lock state and protected data are intentionally not inspected.
- Auto-stop requests Android to close the app and interrupts active work; Android still controls process lifecycle.
- ICMP reachability may be blocked by the network. Traffic counters are cumulative UID counters since boot.
- Reports are stored in the app-specific Documents directory.

## Build and validation
GitHub Actions runs JVM unit tests and builds the debug APK using JDK 17 and Gradle 8.9.

After a successful workflow, download the artifact:
`ultra-hacker-ultimate-v6.0-debug-apk`

The debug APK is for testing. A production release should be signed with a private release key kept outside the repository.
