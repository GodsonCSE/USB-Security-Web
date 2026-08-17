#!/usr/bin/env bash
# ============================================================
# USB Device Security Monitor — Linux / macOS launcher
# Zero-Configuration: Runs instantly with embedded database.
# No database setup or password required!
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

JAR="$SCRIPT_DIR/backend/target/usb-security-monitor-1.0.0.jar"

# Always rebuild so any code/UI changes are picked up.
# (Previously this only built the jar if it didn't exist yet, which meant
#  an old jar could keep serving old HTML/CSS/JS even after files changed.)
echo "============================================================"
echo " Building USB Security Monitor backend..."
echo "============================================================"
cd "$SCRIPT_DIR/backend"
mvn package -DskipTests
cd "$SCRIPT_DIR"
echo ""

echo "============================================================"
echo " Starting USB Device Security Monitor..."
echo " Zero Login / Password required!"
echo " Open in browser: http://localhost:8080"
echo "============================================================"
echo ""

java -jar "$JAR"
