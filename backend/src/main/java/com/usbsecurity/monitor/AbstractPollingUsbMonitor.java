package com.usbsecurity.monitor;

import java.util.*;
import java.util.concurrent.*;

public abstract class AbstractPollingUsbMonitor implements UsbMonitor {

    private final int pollIntervalMs;
    private ScheduledExecutorService executor;
    private volatile boolean running = false;
    private Map<String, DeviceInfo> lastSnapshot = new HashMap<>();
    private UsbMonitorListener listener;

    protected AbstractPollingUsbMonitor(int pollIntervalMs) {
        this.pollIntervalMs = pollIntervalMs;
    }

    protected abstract List<DeviceInfo> readDevices() throws Exception;

    @Override
    public synchronized void start(UsbMonitorListener listener) {
        if (running) return;
        this.listener = Objects.requireNonNull(listener);
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "usb-monitor-poll");
            t.setDaemon(true);
            return t;
        });
        running = true;

        try {
            List<DeviceInfo> initial = readDevices();
            lastSnapshot = toMap(initial);
            for (DeviceInfo d : initial) {
                safeCall(() -> listener.onDeviceConnected(d));
            }
        } catch (Exception e) {
            listener.onMonitoringError("Failed to establish USB baseline", e);
            lastSnapshot = new HashMap<>();
        }

        executor.scheduleWithFixedDelay(this::pollOnce,
                pollIntervalMs, pollIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void pollOnce() {
        try {
            Map<String, DeviceInfo> current = toMap(readDevices());

            for (Map.Entry<String, DeviceInfo> entry : current.entrySet()) {
                if (!lastSnapshot.containsKey(entry.getKey())) {
                    safeCall(() -> listener.onDeviceConnected(entry.getValue()));
                }
            }
            for (Map.Entry<String, DeviceInfo> entry : lastSnapshot.entrySet()) {
                if (!current.containsKey(entry.getKey())) {
                    safeCall(() -> listener.onDeviceDisconnected(entry.getValue()));
                }
            }

            lastSnapshot = current;
        } catch (Exception e) {
            if (listener != null)
                listener.onMonitoringError("USB poll cycle error: " + e.getMessage(), e);
        }
    }

    private void safeCall(Runnable r) {
        try { r.run(); }
        catch (Exception e) {
            if (listener != null)
                listener.onMonitoringError("Listener exception: " + e.getMessage(), e);
        }
    }

    private Map<String, DeviceInfo> toMap(List<DeviceInfo> list) {
        Map<String, DeviceInfo> map = new HashMap<>();
        for (DeviceInfo d : list) map.put(d.fingerprint(), d);
        return map;
    }

    @Override
    public synchronized void stop() {
        running = false;
        if (executor != null) { executor.shutdownNow(); executor = null; }
    }

    @Override
    public List<DeviceInfo> scanCurrentDevices() {
        try { return readDevices(); }
        catch (Exception e) { return Collections.emptyList(); }
    }

    @Override
    public boolean isRunning() { return running; }
}
