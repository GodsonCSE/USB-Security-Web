package com.usbsecurity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_devices")
public class TrustedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UsbDevice device;

    @Column(length = 200)
    private String label;

    private LocalDateTime createdAt;

    public TrustedDevice() {}

    public TrustedDevice(UsbDevice device, String label) {
        this.device    = device;
        this.label     = label;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsbDevice getDevice() { return device; }
    public void setDevice(UsbDevice device) { this.device = device; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
