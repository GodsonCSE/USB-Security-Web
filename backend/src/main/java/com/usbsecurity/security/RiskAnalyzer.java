package com.usbsecurity.security;

import com.usbsecurity.model.RiskLevel;
import com.usbsecurity.monitor.DeviceInfo;
import org.springframework.stereotype.Component;

/**
 * Heuristic risk scoring.
 * NOTE: This is metadata-based only — it does NOT prove a device is malicious.
 */
@Component
public class RiskAnalyzer {

    public record RiskResult(RiskLevel level, String reason) {}

    public RiskResult analyze(DeviceInfo info, boolean trusted) {
        if (trusted)
            return new RiskResult(RiskLevel.LOW, "Device is on the trusted whitelist.");

        int missing = 0;
        StringBuilder fields = new StringBuilder();

        if (blank(info.getVendorId()))    { missing++; append(fields, "vendor ID"); }
        if (blank(info.getProductId()))   { missing++; append(fields, "product ID"); }
        if (blank(info.getSerialNumber())) { missing++; append(fields, "serial number"); }
        if (blank(info.getManufacturer())) { missing++; append(fields, "manufacturer"); }
        if (blank(info.getDeviceName()))  { missing++; append(fields, "device name"); }

        if (missing >= 3)
            return new RiskResult(RiskLevel.HIGH,
                "Unknown device with incomplete metadata (missing: " + fields + ").");
        if (missing >= 1)
            return new RiskResult(RiskLevel.MEDIUM,
                "Unknown device with some missing metadata (missing: " + fields + ").");

        return new RiskResult(RiskLevel.MEDIUM,
            "Unknown device not on the whitelist, but metadata appears complete.");
    }

    private boolean blank(String s) {
        return s == null || s.isBlank() || s.equalsIgnoreCase("Unknown");
    }
    private void append(StringBuilder sb, String f) {
        if (sb.length() > 0) sb.append(", ");
        sb.append(f);
    }
}
