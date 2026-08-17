package com.usbsecurity.controller;

import com.usbsecurity.model.TrustedDevice;
import com.usbsecurity.model.UsbDevice;
import com.usbsecurity.repository.TrustedDeviceRepository;
import com.usbsecurity.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeviceController {

    private final DeviceService      deviceService;
    private final TrustedDeviceRepository trustedRepo;

    public DeviceController(DeviceService deviceService, TrustedDeviceRepository trustedRepo) {
        this.deviceService = deviceService;
        this.trustedRepo   = trustedRepo;
    }

    // ── Devices ────────────────────────────────────────────────────────

    @GetMapping("/devices")
    public List<UsbDevice> allDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/devices/connected")
    public List<UsbDevice> connected() {
        return deviceService.getConnectedDevices();
    }

    @GetMapping("/devices/trusted")
    public List<TrustedDevice> trusted() {
        return trustedRepo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/devices/unknown")
    public List<UsbDevice> unknown() {
        return deviceService.getUnknownDevices();
    }

    @GetMapping("/devices/{id}")
    public ResponseEntity<UsbDevice> deviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Trust management ───────────────────────────────────────────────

    @PostMapping("/devices/{id}/trust")
    public ResponseEntity<?> trust(@PathVariable Long id,
                                   @RequestBody(required = false) Map<String, String> body) {
        try {
            String label = body != null ? body.get("label") : null;
            TrustedDevice td = deviceService.addTrusted(id, label);
            return ResponseEntity.ok(td);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/devices/trusted/{trustedId}")
    public ResponseEntity<Void> removeTrust(@PathVariable Long trustedId) {
        try {
            deviceService.removeTrusted(trustedId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Statistics ─────────────────────────────────────────────────────

    @GetMapping("/statistics")
    public Map<String, Object> statistics() {
        return deviceService.getStatistics();
    }

    // ── Monitor health ─────────────────────────────────────────────────

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
            "monitoring", deviceService.isMonitoring(),
            "message",    "USB Device Security Monitor is running"
        );
    }
}
