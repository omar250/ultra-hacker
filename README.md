# ULTRA HACKER 3.5

Native Android toolkit for authorized cybersecurity learning and diagnostics.

## Features
- Dark dashboard with HOME, TOOLS and TERMINAL
- DNS lookup, reachability checks, local interface information and bounded LAN discovery
- Restricted common-port checks for private IPv4 addresses only
- HTTPS security-header inspection and public RDAP lookup
- SSL/TLS certificate details and HTTP response analysis
- SHA-256 hashing, Base64 encode/decode and local secure password generation
- Per-app cumulative network traffic counters
- Local scan history (up to 50 results) and terminal command history (up to 100 commands)
- PDF export of saved scan results and recent terminal commands
- Terminal commands: help, clear, date, whoami, ip, dns, ping, rdap, headers, ssl, http, sha256, b64encode, b64decode, traffic, passgen, history, report, netcheck

## Build
Push to main or manually run **Build Android APK** in GitHub Actions. Download the artifact named `ultra-hacker-v3-debug-apk`.

## Notes
- The app is not a root shell or full Linux terminal.
- Reports are saved in the app-specific Documents directory.
- Traffic counters are cumulative Android UID counters since boot; they are not live throughput measurements.
- ICMP reachability may be blocked by Android or the network.
- Use assessment tools only on systems you own or are explicitly authorized to test.
