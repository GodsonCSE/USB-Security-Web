package com.usbsecurity.service;

import com.usbsecurity.model.*;
import com.usbsecurity.monitor.*;
import com.usbsecurity.repository.*;
import com.usbsecurity.security.RiskAnalyzer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    private final UsbDeviceRepository     deviceRepo;
    private final TrustedDeviceRepository trustedRepo;
    private final UsbEventRepository      eventRepo;
    private final SecurityAlertRepository alertRepo;
    private final RiskAnalyzer            riskAnalyzer;
    private final SseService              sseService;

    @Value("${usb.monitor.poll-interval-ms:2000}")
    private int pollIntervalMs;

    @Value("${usb.monitor.simulate:false}")
    private boolean simulate;

    private UsbMonitor monitor;

    public DeviceService(UsbDeviceRepository deviceRepo,
                         TrustedDeviceRepository trustedRepo,
                         UsbEventRepository eventRepo,
                         SecurityAlertRepository alertRepo,
                         RiskAnalyzer riskAnalyzer,
                         SseService sseService) {
        this.deviceRepo  = deviceRepo;
        this.trustedRepo = trustedRepo;
        this.eventRepo   = eventRepo;
        this.alertRepo   = alertRepo;
        this.riskAnalyzer = riskAnalyzer;
        this.sseService   = sseService;
    }

    // ------------------------------------------------------------------ //
    // Lifecycle
    // ------------------------------------------------------------------ //

    @PostConstruct
    public void startMonitoring() {
        monitor = UsbMonitorFactory.create(pollIntervalMs, simulate);

        // Reconcile database state with current hardware state
        List<DeviceInfo> initialScan = monitor.scanCurrentDevices();
        reconcileSavedDevices(initialScan);

        monitor.start(new UsbMonitorListener() {
            @Override public void onDeviceConnected(DeviceInfo d)    { handleConnected(d); }
            @Override public void onDeviceDisconnected(DeviceInfo d) { handleDisconnected(d); }
            @Override public void onMonitoringError(String msg, Throwable c) {
                log.warn("[Monitor] {}", msg);
            }
        });
        log.info("USB monitoring started (simulate={}, pollMs={})", simulate, pollIntervalMs);
    }

    @Transactional
    public void reconcileSavedDevices(List<DeviceInfo> currentScan) {
        Set<String> currentKeys = new HashSet<>();
        if (currentScan != null) {
            for (DeviceInfo d : currentScan) {
                currentKeys.add(d.fingerprint());
            }
        }
        List<UsbDevice> currentlyMarked = deviceRepo.findByCurrentlyConnectedTrue();
        for (UsbDevice d : currentlyMarked) {
            if (!currentKeys.contains(d.getDeviceKey())) {
                d.setCurrentlyConnected(false);
                deviceRepo.save(d);
                log.info("Reconciled disconnected USB device on startup: {}", d.getDeviceName());
            }
        }
    }

    @PreDestroy
    public void stopMonitoring() {
        if (monitor != null) monitor.stop();
        log.info("USB monitoring stopped.");
    }

    // ------------------------------------------------------------------ //
    // USB event handlers (called from monitor background thread)
    // ------------------------------------------------------------------ //

    @Transactional
    public void handleConnected(DeviceInfo info) {
        try {
            UsbDevice device = resolveOrCreate(info);
            device.setCurrentlyConnected(true);
            device.setLastSeenAt(LocalDateTime.now());
            deviceRepo.save(device);

            boolean trusted = trustedRepo.existsByDevice(device);
            RiskAnalyzer.RiskResult risk = riskAnalyzer.analyze(info, trusted);

            UsbEvent event = new UsbEvent(device, EventType.CONNECTED, risk.level(), risk.reason());
            eventRepo.save(event);

            SecurityAlert alert = null;
            if (!trusted) {
                // Don't create duplicate OPEN alerts for the same device if one is already open
                boolean alreadyHasOpenAlert = alertRepo.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN)
                        .stream().anyMatch(a -> a.getDevice() != null && a.getDevice().getId().equals(device.getId()));
                if (!alreadyHasOpenAlert) {
                    alert = new SecurityAlert(device, risk.level(),
                        "Unknown USB device detected. " + risk.reason());
                    alertRepo.save(alert);
                }
            }

            // Broadcast to all browsers via SSE
            Map<String, Object> payload = buildPayload(device, event, alert, trusted, risk.level());
            sseService.broadcast("DEVICE_CONNECTED", payload);
            sseService.broadcast("STATS_UPDATE", buildStats());

        } catch (Exception e) {
            log.error("Error handling CONNECTED event for {}: {}", info, e.getMessage(), e);
        }
    }

    @Transactional
    public void handleDisconnected(DeviceInfo info) {
        try {
            String key = info.fingerprint();
            deviceRepo.findByDeviceKey(key).ifPresent(device -> {
                device.setCurrentlyConnected(false);
                device.setLastSeenAt(LocalDateTime.now());
                deviceRepo.save(device);

                UsbEvent event = new UsbEvent(device, EventType.DISCONNECTED, RiskLevel.LOW, "Device disconnected.");
                eventRepo.save(event);

                Map<String, Object> payload = new HashMap<>();
                payload.put("device", enrichDevice(device));
                payload.put("event", event);
                sseService.broadcast("DEVICE_DISCONNECTED", payload);
                sseService.broadcast("STATS_UPDATE", buildStats());
            });
        } catch (Exception e) {
            log.error("Error handling DISCONNECTED event: {}", e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ //
    // Trust management
    // ------------------------------------------------------------------ //

    @Transactional
    public TrustedDevice addTrusted(Long deviceId, String label) {
        UsbDevice device = deviceRepo.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + deviceId));

        if (trustedRepo.existsByDevice(device))
            throw new IllegalStateException("Device is already trusted.");

        TrustedDevice td = new TrustedDevice(device,
                label != null && !label.isBlank() ? label : device.getDeviceName());
        trustedRepo.save(td);

        // Log the trust action
        UsbEvent event = new UsbEvent(device, EventType.TRUSTED, RiskLevel.LOW, "Device added to trusted list.");
        eventRepo.save(event);

        // Close any open alerts for this device
        alertRepo.findByStatusOrderByCreatedAtDesc(AlertStatus.OPEN).stream()
                .filter(a -> a.getDevice().getId().equals(deviceId))
                .forEach(a -> {
                    a.setStatus(AlertStatus.TRUSTED);
                    a.setResolvedAt(LocalDateTime.now());
                    alertRepo.save(a);
                });

        sseService.broadcast("DEVICE_TRUSTED", enrichDevice(device));
        sseService.broadcast("STATS_UPDATE", buildStats());
        return td;
    }

    @Transactional
    public void removeTrusted(Long trustedId) {
        TrustedDevice td = trustedRepo.findById(trustedId)
                .orElseThrow(() -> new IllegalArgumentException("Trusted entry not found: " + trustedId));
        UsbDevice device = td.getDevice();
        trustedRepo.delete(td);

        UsbEvent event = new UsbEvent(device, EventType.UNTRUSTED, RiskLevel.MEDIUM,
                "Device removed from trusted list.");
        eventRepo.save(event);

        sseService.broadcast("DEVICE_UNTRUSTED", enrichDevice(device));
        sseService.broadcast("STATS_UPDATE", buildStats());
    }

    // ------------------------------------------------------------------ //
    // Alert actions
    // ------------------------------------------------------------------ //

    @Transactional
    public SecurityAlert resolveAlert(Long alertId, AlertStatus newStatus) {
        SecurityAlert alert = alertRepo.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        alert.setStatus(newStatus);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepo.save(alert);

        if (newStatus == AlertStatus.BLOCK_SIMULATED) {
            UsbEvent ev = new UsbEvent(alert.getDevice(), EventType.BLOCK_SIMULATED, alert.getSeverity(),
                    "Device would be blocked (simulation only - no actual blocking performed).");
            eventRepo.save(ev);
        }

        sseService.broadcast("ALERT_UPDATED", alert);
        return alert;
    }

    // ------------------------------------------------------------------ //
    // Query helpers
    // ------------------------------------------------------------------ //

    @Transactional(readOnly = true)
    public List<UsbDevice> getAllDevices() {
        return deviceRepo.findAllByOrderByLastSeenAtDesc().stream()
                .map(this::enrichDevice).toList();
    }

    @Transactional(readOnly = true)
    public List<UsbDevice> getConnectedDevices() {
        return deviceRepo.findByCurrentlyConnectedTrue().stream()
                .map(this::enrichDevice).toList();
    }

    @Transactional(readOnly = true)
    public List<UsbDevice> getUnknownDevices() {
        Set<Long> trustedIds = getTrustedDeviceIds();
        return deviceRepo.findByCurrentlyConnectedTrue().stream()
                .filter(d -> !trustedIds.contains(d.getId()))
                .map(this::enrichDevice).toList();
    }

    @Transactional(readOnly = true)
    public Optional<UsbDevice> getDeviceById(Long id) {
        return deviceRepo.findById(id).map(this::enrichDevice);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() { return buildStats(); }

    // ------------------------------------------------------------------ //
    // Internal helpers
    // ------------------------------------------------------------------ //

    private UsbDevice resolveOrCreate(DeviceInfo info) {
        String key = info.fingerprint();
        return deviceRepo.findByDeviceKey(key).orElseGet(() -> {
            UsbDevice d = new UsbDevice();
            d.setDeviceKey(key);
            d.setDeviceName(info.getDeviceName());
            d.setManufacturer(info.getManufacturer());
            d.setDeviceType(info.getDeviceType());
            d.setVendorId(info.getVendorId());
            d.setProductId(info.getProductId());
            d.setSerialNumber(info.getSerialNumber());
            d.setFirstSeenAt(LocalDateTime.now());
            return deviceRepo.save(d);
        });
    }

    private UsbDevice enrichDevice(UsbDevice device) {
        var td = trustedRepo.findByDevice(device);
        device.setTrusted(td.isPresent());
        td.ifPresent(t -> device.setTrustedId(t.getId()));
        RiskAnalyzer.RiskResult risk = riskAnalyzer.analyze(infoFrom(device), td.isPresent());
        device.setRiskLevel(risk.level());
        return device;
    }

    private DeviceInfo infoFrom(UsbDevice d) {
        DeviceInfo info = new DeviceInfo();
        info.setDeviceName(d.getDeviceName());
        info.setManufacturer(d.getManufacturer());
        info.setVendorId(d.getVendorId());
        info.setProductId(d.getProductId());
        info.setSerialNumber(d.getSerialNumber());
        info.setDeviceType(d.getDeviceType());
        return info;
    }

    private Set<Long> getTrustedDeviceIds() {
        Set<Long> ids = new HashSet<>();
        trustedRepo.findAll().forEach(t -> ids.add(t.getDevice().getId()));
        return ids;
    }

    private Map<String, Object> buildPayload(UsbDevice device, UsbEvent event,
                                              SecurityAlert alert, boolean trusted, RiskLevel risk) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("device", enrichDevice(device));
        p.put("event", event);
        p.put("alert", alert);
        p.put("trusted", trusted);
        p.put("riskLevel", risk);
        return p;
    }

    private Map<String, Object> buildStats() {
        List<UsbDevice> connectedDevices = deviceRepo.findByCurrentlyConnectedTrue();
        Set<Long> trustedIds = getTrustedDeviceIds();

        long connected = connectedDevices.size();
        long trustedCount = trustedRepo.count();
        long unknownCount = connectedDevices.stream().filter(d -> !trustedIds.contains(d.getId())).count();
        long openAlerts = alertRepo.countByStatus(AlertStatus.OPEN);
        long total      = deviceRepo.count();

        Map<String, Object> s = new LinkedHashMap<>();
        s.put("connectedCount", connected);
        s.put("trustedCount",   trustedCount);
        s.put("unknownCount",   unknownCount);
        s.put("openAlertCount", openAlerts);
        s.put("totalDeviceCount", total);
        return s;
    }

    public boolean isMonitoring() { return monitor != null && monitor.isRunning(); }
}
