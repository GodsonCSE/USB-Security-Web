package com.usbsecurity.monitor;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * No-hardware monitor for demos, CI, and grading environments.
 * Enable with USB_MONITOR_SIMULATE=true.
 * Trigger events by calling simulateConnect / simulateDisconnect.
 */
public class SimulatedUsbMonitor extends AbstractPollingUsbMonitor {

    private final List<DeviceInfo> connected = new CopyOnWriteArrayList<>();

    public SimulatedUsbMonitor(int pollMs) { super(pollMs); }

    @Override
    protected List<DeviceInfo> readDevices() { return new ArrayList<>(connected); }

    public void simulateConnect(DeviceInfo d)    { connected.add(d); }
    public void simulateDisconnect(DeviceInfo d) { connected.removeIf(x -> x.fingerprint().equals(d.fingerprint())); }

    public static DeviceInfo knownDevice() {
        DeviceInfo d = new DeviceInfo();
        d.setDeviceName("SanDisk Ultra USB 3.0");
        d.setManufacturer("SanDisk");
        d.setDeviceType("Mass Storage");
        d.setVendorId("0x0781");
        d.setProductId("0x5581");
        d.setSerialNumber("4C530001234567");
        return d;
    }

    public static DeviceInfo suspiciousDevice() {
        DeviceInfo d = new DeviceInfo();
        d.setDeviceType("HID");
        d.setVendorId("0x1234");
        return d;
    }
}
