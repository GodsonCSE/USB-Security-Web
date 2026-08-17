package com.usbsecurity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usb_events",
       indexes = {
           @Index(name = "idx_event_time",      columnList = "timestamp"),
           @Index(name = "idx_event_type",      columnList = "eventType"),
           @Index(name = "idx_event_risk",      columnList = "riskLevel")
       })
public class UsbEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UsbDevice device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    @Column(length = 500)
    private String reason;

    private LocalDateTime timestamp;

    public UsbEvent() {}

    public UsbEvent(UsbDevice device, EventType type, RiskLevel risk, String reason) {
        this.device    = device;
        this.eventType = type;
        this.riskLevel = risk;
        this.reason    = reason;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsbDevice getDevice() { return device; }
    public void setDevice(UsbDevice device) { this.device = device; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
