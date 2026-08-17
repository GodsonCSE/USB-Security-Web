# USB Device Security Monitor — Web Edition

A lightweight, zero-configuration Spring Boot + vanilla HTML/JS web application that monitors USB devices connected to your computer and displays a live security dashboard in any browser.

**No login ID or password required — opens directly in your browser.**

---

## Quick Start (Zero Setup Required)

### Option 1: Live Application with USB Monitoring (Recommended)
Simply double-click `run.bat` (Windows) or run `./run.sh` (Linux/macOS):

```bash
# Windows
run.bat

# Linux / macOS
chmod +x run.sh
./run.sh
```
The application will automatically build, start with an embedded database, and open `http://localhost:8080` in your browser.

### Option 2: Instant UI Preview (No Java / Build Needed)
Double-click `view_in_browser.bat` or open `backend/src/main/resources/static/index.html` in any web browser to view and interact with the dashboard directly.

---

## Project Overview

```
Browser (http://localhost:8080 or standalone HTML)
        │  REST API + Server-Sent Events (SSE)
        ▼
Spring Boot Backend (Java 17)
        │
   ┌────┴───────────────────────────┐
   │                                │
USB Monitor                     Database
(PowerShell / sysfs / profiler) (Embedded H2 / optional MySQL)
   │
OS → Real USB Devices
```

---

## Features

| Feature | Detail |
|---------|--------|
| **Instant Access** | No credentials, login prompts, or passwords needed |
| **Live Dashboard** | Real-time connection stats, doughnut chart, live activity feed via SSE |
| **Device Inventory** | Complete history of detected USB devices with vendor, model, serial, and risk status |
| **Trusted Whitelist** | Whitelist authorized devices with custom labels |
| **Security Alerts** | Immediate alerts for unknown or high-risk USB devices (Allow, Trust, Simulate Block, Dismiss) |
| **Security Audit Logs** | Comprehensive event history with multi-parameter filtering and sorting |
| **Data Export** | Export filtered audit logs to CSV format |
| **Zero-Config Database** | Embedded file-based H2 database active by default (with optional MySQL support) |
| **Native OS Backends** | Automatic hardware detection on Windows (PowerShell), Linux (`sysfs`), and macOS (`system_profiler`) |

---

## Technology Stack

- **Backend:** Java 17, Spring Boot 3.2 (Spring Web, Spring Data JPA)
- **Database:** Embedded H2 Database (Default zero-config), MySQL 8 (Optional)
- **Frontend:** HTML5, CSS3, Vanilla JavaScript, Chart.js, Font Awesome 6
- **Real-Time Updates:** Server-Sent Events (SSE)

---

## Configuration (Optional)

By default, the application runs with zero configuration using the embedded database. If you wish to connect to an external MySQL instance, set the following environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `USB_MONITOR_DB_URL` | Embedded H2 (`jdbc:h2:file:./data/usb_security_db`) | JDBC Connection URL |
| `USB_MONITOR_DB_DRIVER` | `org.h2.Driver` | JDBC Driver Class Name |
| `USB_MONITOR_DB_USER` | `sa` | Database user name |
| `USB_MONITOR_DB_PASSWORD` | *(empty)* | Database password |
| `USB_MONITOR_POLL_MS` | `2000` | USB poll interval in milliseconds |
| `USB_MONITOR_SIMULATE` | `false` | Set `true` to run simulation without hardware polling |

---

## Adding this Project to GitHub

To push this project to your GitHub account, follow these steps:

### 1. Create a New Repository on GitHub
1. Go to [github.com/new](https://github.com/new).
2. Name your repository (e.g., `usb-security-web`).
3. Leave **"Initialize this repository with a README" unchecked** (since we already have one).
4. Click **Create repository**.

### 2. Push Your Local Repository to GitHub
Run the following commands in PowerShell or Terminal from this project directory:

```bash
# Ensure branch is named main
git branch -M main

# Add your GitHub repository as the origin remote (replace with your GitHub repo URL)
git remote add origin https://github.com/<YOUR_USERNAME>/<YOUR_REPO_NAME>.git

# Push the code to GitHub
git push -u origin main
```

---

## License & Security Notice

- **Local Use:** Designed for local hardware monitoring and administration.
- **Risk Assessment:** Risk scores (LOW, MEDIUM, HIGH) are heuristic assessments based on metadata completeness and whitelist status.
