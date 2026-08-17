package com.usbsecurity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usb_devices",
       indexes = {@Index(name = "idx_device_key", columnList = "deviceKey", unique = true)})
public class UsbDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String deviceKey;          // VID:PID:SERIAL fingerprint

    @Column(length = 200)
    private String deviceName;

    @Column(length = 200)
    private String manufacturer;

    @Column(length = 10)
    private String vendorId;

    @Column(length = 10)
    private String productId;

    @Column(length = 200)
    private String serialNumber;

    @Column(length = 60)
    private String deviceType;

    private boolean currentlyConnected;

    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;

    // Transient fields populated by the service layer before returning via API
    @Transient private boolean trusted;
    @Transient private Long trustedId;   // set when trusted=true so UI can call DELETE /trusted/{id}
    @Transient private RiskLevel riskLevel;

    // ------------------------------------------------------------------ //
    // Static helper
    // ------------------------------------------------------------------ //
    public static String buildKey(String vendorId, String productId,
                                  String serialNumber, String deviceName) {
        String vid    = safe(vendorId);
        String pid    = safe(productId);
        String serial = (serialNumber != null && !serialNumber.isBlank()
                         && !serialNumber.equalsIgnoreCase("Unknown"))
                        ? safe(serialNumber)
                        : "NOSERIAL-" + safe(deviceName);
        return (vid + ":" + pid + ":" + serial).toUpperCase();
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "UNKNOWN" : s.trim();
    }

    // ------------------------------------------------------------------ //
    // Getters / setters
    // ------------------------------------------------------------------ //
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDeviceKey() { return deviceKey; }
    public void setDeviceKey(String deviceKey) { this.deviceKey = deviceKey; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public boolean isCurrentlyConnected() { return currentlyConnected; }
    public void setCurrentlyConnected(boolean currentlyConnected) { this.currentlyConnected = currentlyConnected; }
    public LocalDateTime getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(LocalDateTime firstSeenAt) { this.firstSeenAt = firstSeenAt; }
    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public boolean isTrusted() { return trusted; }
    public void setTrusted(boolean trusted) { this.trusted = trusted; }
    public Long getTrustedId() { return trustedId; }
    public void setTrustedId(Long trustedId) { this.trustedId = trustedId; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
}
