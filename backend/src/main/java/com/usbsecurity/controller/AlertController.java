package com.usbsecurity.controller;

import com.usbsecurity.model.AlertStatus;
import com.usbsecurity.model.SecurityAlert;
import com.usbsecurity.repository.SecurityAlertRepository;
import com.usbsecurity.service.DeviceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final SecurityAlertRepository alertRepo;
    private final DeviceService           deviceService;

    public AlertController(SecurityAlertRepository alertRepo, DeviceService deviceService) {
        this.alertRepo     = alertRepo;
        this.deviceService = deviceService;
    }

    @GetMapping
    public List<SecurityAlert> all() {
        return alertRepo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/open")
    public List<SecurityAlert> open() {
        return alertRepo.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN);
    }

    /** Resolve an alert: status = ALLOWED | TRUSTED | BLOCK_SIMULATED | DISMISSED */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<?> resolve(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        String statusStr = body.getOrDefault("status", "DISMISSED");
        try {
            AlertStatus status = AlertStatus.valueOf(statusStr.toUpperCase());
            SecurityAlert updated = deviceService.resolveAlert(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + statusStr));
        }
    }
}
