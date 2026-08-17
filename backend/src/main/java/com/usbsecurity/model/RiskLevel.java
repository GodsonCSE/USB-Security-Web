package com.usbsecurity.model;

public enum RiskLevel {
    LOW, MEDIUM, HIGH;

    public static RiskLevel fromString(String v) {
        if (v == null) return MEDIUM;
        try { return RiskLevel.valueOf(v.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return MEDIUM; }
    }
}
