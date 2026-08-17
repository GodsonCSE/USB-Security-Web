<<<<<<< HEAD
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
=======
# 🔐 USB Device Security Monitor

A web-based **Cybersecurity Monitoring System** that detects USB devices connected to a computer, identifies trusted and unknown devices, generates security alerts, and maintains a complete history of USB activity through a web dashboard.

---

## 📌 Project Overview

USB devices such as flash drives, external hard drives, and other removable peripherals are commonly used for data transfer.

However, unauthorized USB devices can introduce security risks such as:

* Malware infection
* Unauthorized data transfer
* Data theft
* Unknown removable devices
* Malicious USB peripherals
* Security policy violations

The **USB Device Security Monitor** provides a centralized web dashboard for monitoring USB device activity and identifying potentially unauthorized devices.

---

## 🎯 Objectives

The main objectives of this project are:

* Detect USB device connection and disconnection events.
* Collect available USB device information.
* Identify trusted and unknown devices.
* Generate security alerts for unknown devices.
* Maintain USB activity logs.
* Provide a real-time web dashboard.
* Allow administrators/users to manage trusted devices.
* Provide security statistics and reports.

---

## ✨ Features

### 🔌 USB Device Monitoring

Automatically detect USB devices when they are connected or disconnected.

The system can collect information such as:

* Device Name
* Manufacturer
* Vendor ID (VID)
* Product ID (PID)
* Serial Number
* Device Type
* Connection Time
* Disconnection Time
* Current Status

---

### 🛡️ Trusted Device Management

Users can maintain a list of trusted USB devices.

Features include:

* Add device to trusted list
* Remove device from trusted list
* View trusted devices
* Automatically compare newly connected devices against the trusted list

---

### 🚨 Security Alerts

When an unknown USB device is detected, the system generates a security alert.

Example:

```text
⚠ UNKNOWN USB DEVICE DETECTED

Device: SanDisk USB Device
VID: XXXX
PID: XXXX

Risk Level: HIGH

Reason:
Device is not present in the trusted-device list.
```

---

### 📊 Security Dashboard

The dashboard provides an overview of the current USB security status.

Example:

```text
┌─────────────────────────────────────────────┐
│       USB DEVICE SECURITY MONITOR           │
├─────────────────────────────────────────────┤
│                                             │
│  Connected     Trusted      Unknown         │
│      3            2            1             │
│                                             │
├─────────────────────────────────────────────┤
│             Recent USB Activity             │
│                                             │
│ Device       Event          Risk            │
│ Kingston     Connected      LOW             │
│ SanDisk      Connected      HIGH            │
│ Kingston     Disconnected   LOW             │
│                                             │
└─────────────────────────────────────────────┘
```

---

### 📋 Security Logs

The system records USB activity including:

* Connection events
* Disconnection events
* Trusted devices
* Unknown devices
* Security alerts
* Risk levels
* Timestamps

Logs can be searched and filtered.

---

### 📈 Risk Classification

The system uses a basic heuristic risk classification.

| Risk      | Description                                                       |
| --------- | ----------------------------------------------------------------- |
| 🟢 LOW    | Trusted device                                                    |
| 🟡 MEDIUM | Unknown device with normal available metadata                     |
| 🔴 HIGH   | Unknown device with suspicious/incomplete identifying information |

> **Note:** A HIGH risk classification does not prove that a USB device is malicious. It indicates that the device requires further investigation.

---

## 🏗️ System Architecture

```text
                    ┌──────────────────────┐
                    │     Web Browser      │
                    │                      │
                    │  Security Dashboard  │
                    └──────────┬───────────┘
                               │
                              HTTP
                               │
                    ┌──────────▼───────────┐
                    │    Spring Boot       │
                    │      Backend         │
                    │                      │
                    │     REST APIs        │
                    └──────────┬───────────┘
                               │
                 ┌─────────────┴──────────────┐
                 │                            │
        ┌────────▼─────────┐       ┌─────────▼────────┐
        │  USB Monitoring   │       │      MySQL       │
        │     Service       │       │     Database     │
        └────────┬─────────┘       └──────────────────┘
                 │
                 ▼
        ┌──────────────────┐
        │ Operating System │
        └────────┬─────────┘
                 │
                 ▼
           USB Devices
```

---

## 🛠️ Technology Stack

### Backend

* Java
* Spring Boot
* Spring Web
* REST API
* Maven

### Frontend

* HTML5
* CSS3
* JavaScript
* Bootstrap
* Chart.js

### Database

* MySQL

### Development Tools

* IntelliJ IDEA
* Git
* GitHub
* Postman

---

## 📁 Project Structure

```text
USB-Device-Security-Monitor/
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/usbsecurity/
│   │   │   │       ├── controller/
│   │   │   │       ├── service/
│   │   │   │       ├── repository/
│   │   │   │       ├── model/
│   │   │   │       ├── monitor/
│   │   │   │       ├── security/
│   │   │   │       └── config/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/
│   │   │
│   │   └── test/
│   │
│   └── pom.xml
│
├── database/
│   └── schema.sql
│
├── screenshots/
│
├── README.md
└── .gitignore
```

---

## 🗄️ Database Structure

The project uses MySQL.

### `usb_devices`

Stores information about detected USB devices.

```text
id
device_name
manufacturer
vendor_id
product_id
serial_number
device_type
created_at
```

### `trusted_devices`

Stores trusted USB devices.

```text
id
device_id
created_at
```

### `usb_events`

Stores USB connection and disconnection events.

```text
id
device_id
event_type
risk_level
reason
timestamp
```

### `security_alerts`

Stores security alerts.

```text
id
device_id
severity
message
status
created_at
```

---

# 🚀 Installation

## 1. Clone the Repository

```bash
git clone https://github.com/your-username/USB-Device-Security-Monitor.git
```

Move into the project:

```bash
cd USB-Device-Security-Monitor
>>>>>>> 9fb99ce0992b6ed0cb730324ab636d26855533c5
```

---

<<<<<<< HEAD
## License & Security Notice

- **Local Use:** Designed for local hardware monitoring and administration.
- **Risk Assessment:** Risk scores (LOW, MEDIUM, HIGH) are heuristic assessments based on metadata completeness and whitelist status.
=======
## 2. Requirements

Install the following:

* Java 17 or later
* Maven
* MySQL
* Modern web browser
* Git

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

---

## 3. Create Database

Open MySQL and create the database:

```sql
CREATE DATABASE usb_security_monitor;
```

Then execute:

```text
database/schema.sql
```

---

## 4. Configure Database

Update:

```text
backend/src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/usb_security_monitor
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update

server.port=8080
```

Do not commit real database passwords to GitHub.

---

# ▶️ Running the Application

Navigate to the backend:

```bash
cd backend
```

Run:

```bash
mvn spring-boot:run
```

The server will start on:

```text
http://localhost:8080
```

Open the URL in a browser.

The dashboard should open directly.

### No Login Required

This project intentionally does not contain:

* Username
* Password
* Login page
* Registration

Anyone who can access the application can view the dashboard.

---

# 🌐 Web Dashboard

Open:

```text
http://localhost:8080
```

Available pages:

```text
/               → Dashboard
/devices        → USB Devices
/trusted        → Trusted Devices
/alerts         → Security Alerts
/logs           → Security Logs
```

---

# 🔌 USB Monitoring Workflow

When a USB device is connected:

```text
USB Connected
      ↓
Detect Device
      ↓
Collect Device Information
      ↓
Check Trusted List
      ↓
Calculate Risk
      ↓
Generate Event
      ↓
Save to MySQL
      ↓
Update Dashboard
```

When the device is removed:

```text
USB Disconnected
      ↓
Create Event
      ↓
Save Event
      ↓
Update Dashboard
```

---

# 🔑 VID and PID

### Vendor ID (VID)

VID identifies the manufacturer of a USB device.

### Product ID (PID)

PID identifies a product/model associated with the manufacturer.

For example:

```text
VID = 0781
PID = 5567
```

VID and PID are useful for device identification, but they should not be treated as a unique identity for a physical device by themselves.

When available, the serial number can provide stronger device-level identification.

---

# ⚠️ Browser Security Limitation

A normal website cannot freely access all USB information from every visitor's computer.

This project therefore uses:

```text
Browser
   ↓
Web Dashboard
   ↓
Spring Boot Backend
   ↓
USB Monitoring Service
   ↓
Operating System
   ↓
USB Device
```

The USB monitoring service runs on the computer being monitored.

For the normal college demonstration, the complete application can run locally:

```text
http://localhost:8080
```

---

# 🔐 Security Considerations

This project is designed as a defensive cybersecurity monitoring system.

It does NOT:

* Read personal files unnecessarily
* Copy files from USB devices
* Steal credentials
* Capture keyboard input
* Deploy malware
* Delete USB files
* Modify USB contents
* Perform unauthorized data collection

The system primarily monitors:

* USB metadata
* Connection events
* Device identity information
* Security alerts

---

# 🧪 Testing

The following test cases should be performed:

| Test                  | Expected Result                |
| --------------------- | ------------------------------ |
| Connect trusted USB   | Device marked LOW/TRUSTED      |
| Connect unknown USB   | Security alert generated       |
| Disconnect USB        | Disconnect event recorded      |
| Connect multiple USBs | All devices detected           |
| Missing serial number | Application continues normally |
| Database unavailable  | Graceful error                 |
| Add trusted device    | Device added to whitelist      |
| Remove trusted device | Device removed from whitelist  |
| Filter logs           | Matching events displayed      |
| Export CSV            | CSV generated successfully     |

---

# 🚧 Limitations

Current limitations include:

1. USB detection depends on operating-system capabilities.
2. Some USB devices may not expose complete metadata.
3. VID/PID alone cannot uniquely identify every physical USB device.
4. An unknown USB device is not necessarily malicious.
5. The system is not a replacement for antivirus software.
6. Some operating-system operations may require administrator privileges.
7. The current risk classification is heuristic.

---

# 🔮 Future Enhancements

Possible future improvements:

* 🤖 Machine-learning-based USB anomaly detection
* 📧 Email security notifications
* 📱 Mobile notifications
* 🌐 Centralized monitoring for multiple computers
* 🔍 Advanced device fingerprinting
* 🛡️ Enterprise USB security policies
* 📊 Advanced security analytics
* 🔗 SIEM integration
* 🔐 Digital signature verification
* 🚨 Automated incident response
* 📈 Historical security analytics

---

# 🎓 Cybersecurity Concepts Demonstrated

This project demonstrates knowledge of:

* Endpoint security
* USB security
* Device identification
* Access control concepts
* Security monitoring
* Event logging
* Risk assessment
* Security alerts
* Database security
* REST API security
* Secure coding
* SQL injection prevention
* Defensive cybersecurity
* Operating-system device monitoring

---

# 💼 Interview Explanation

### Short Explanation

> "USB Device Security Monitor is a web-based cybersecurity project that monitors USB devices connected to a computer. When a device is connected, the system collects its available metadata such as VID, PID, serial number and manufacturer information. It compares the device against a trusted-device list. If the device is unknown, the system generates a security alert and records the event in MySQL. The information is then displayed through a real-time web dashboard built using Java, Spring Boot, HTML, CSS and JavaScript."

---

# 📜 License

This project is intended for educational and cybersecurity learning purposes.

---

# 👨‍💻 Author

**Your Name**

Cybersecurity / Java / Data Science Student

GitHub:

```text
https://github.com/your-username
```

---

## ⭐ Project Goal

The goal of this project is to demonstrate how a defensive cybersecurity application can monitor removable devices, identify potentially unauthorized devices, generate security alerts, and maintain an auditable history of USB activity through a web-based interface.
>>>>>>> 9fb99ce0992b6ed0cb730324ab636d26855533c5
