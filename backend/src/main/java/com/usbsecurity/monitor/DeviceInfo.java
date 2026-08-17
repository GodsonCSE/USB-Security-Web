package com.usbsecurity.monitor;

import java.util.Objects;

/**
 * Raw snapshot of a USB device as reported by the OS.
 * All fields default to "Unknown" so downstream code never receives null.
 */
public class DeviceInfo {

    public static final String UNKNOWN = "Unknown";

    private String deviceName    = UNKNOWN;
    private String manufacturer  = UNKNOWN;
    private String deviceType    = UNKNOWN;
    private String vendorId      = UNKNOWN;
    private String productId     = UNKNOWN;
    private String serialNumber  = UNKNOWN;
    private String rawIdentifier;

    public String fingerprint() {
        String serial = serialNumber.equals(UNKNOWN)
                        ? "NOSERIAL-" + deviceName
                        : serialNumber;
        return (vendorId + ":" + productId + ":" + serial).toUpperCase();
    }

    private String orUnknown(String v) {
        return (v == null || v.isBlank()) ? UNKNOWN : v.trim();
    }

    public String getDeviceName()    { return deviceName; }
    public void setDeviceName(String v)    { deviceName = orUnknown(v); }
    public String getManufacturer()  { return manufacturer; }
    public void setManufacturer(String v)  { manufacturer = orUnknown(v); }
    public String getDeviceType()    { return deviceType; }
    public void setDeviceType(String v)    { deviceType = orUnknown(v); }
    public String getVendorId()      { return vendorId; }
    public void setVendorId(String v)      { vendorId = orUnknown(v); }
    public String getProductId()     { return productId; }
    public void setProductId(String v)     { productId = orUnknown(v); }
    public String getSerialNumber()  { return serialNumber; }
    public void setSerialNumber(String v)  { serialNumber = orUnknown(v); }
    public String getRawIdentifier() { return rawIdentifier; }
    public void setRawIdentifier(String v) { rawIdentifier = v; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DeviceInfo d)) return false;
        return Objects.equals(fingerprint(), d.fingerprint());
    }

    @Override public int hashCode() { return Objects.hash(fingerprint()); }
    @Override public String toString() {
        return deviceName + " (VID=" + vendorId + " PID=" + productId + ")";
    }
}
