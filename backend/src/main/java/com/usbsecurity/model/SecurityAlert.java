package com.usbsecurity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_alerts",
       indexes = {@Index(name = "idx_alert_status", columnList = "status")})
public class SecurityAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UsbDevice device;

    @Enumerated(EnumType.STRING)
    private RiskLevel severity;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertStatus status = AlertStatus.OPEN;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public SecurityAlert() {}

    public SecurityAlert(UsbDevice device, RiskLevel severity, String message) {
        this.device    = device;
        this.severity  = severity;
        this.message   = message;
        this.status    = AlertStatus.OPEN;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsbDevice getDevice() { return device; }
    public void setDevice(UsbDevice device) { this.device = device; }
    public RiskLevel getSeverity() { return severity; }
    public void setSeverity(RiskLevel severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
