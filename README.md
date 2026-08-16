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
```

---

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
