package com.usbsecurity.monitor;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class LinuxUsbMonitor extends AbstractPollingUsbMonitor {

    private static final Path USB_ROOT = Path.of("/sys/bus/usb/devices");

    public LinuxUsbMonitor(int pollMs) { super(pollMs); }

    @Override
    protected List<DeviceInfo> readDevices() throws Exception {
        if (!Files.isDirectory(USB_ROOT))
            throw new RuntimeException("sysfs USB path not available: " + USB_ROOT);

        List<DeviceInfo> list = new ArrayList<>();
        File[] entries = USB_ROOT.toFile().listFiles();
        if (entries == null) return list;

        for (File e : entries) {
            String name = e.getName();
            if (name.contains(":") || name.matches("usb\\d+")) continue;
            try {
                DeviceInfo d = readOne(e);
                if (d != null) list.add(d);
            } catch (Exception ignored) { /* skip unreadable device, keep going */ }
        }
        return list;
    }

    private DeviceInfo readOne(File dir) {
        String vid = read(dir, "idVendor");
        if (vid == null) return null;
        DeviceInfo d = new DeviceInfo();
        d.setRawIdentifier(dir.getName());
        d.setVendorId("0x" + vid.toUpperCase());
        d.setProductId(read(dir, "idProduct") != null ? "0x" + read(dir, "idProduct").toUpperCase() : null);
        d.setManufacturer(read(dir, "manufacturer"));
        d.setDeviceName(read(dir, "product"));
        d.setSerialNumber(read(dir, "serial"));
        d.setDeviceType(classify(read(dir, "bDeviceClass")));
        return d;
    }

    private String read(File dir, String file) {
        try {
            Path p = dir.toPath().resolve(file);
            if (!Files.isReadable(p)) return null;
            String v = Files.readString(p, StandardCharsets.UTF_8).trim();
            return v.isBlank() ? null : v;
        } catch (Exception e) { return null; }
    }

    private String classify(String cls) {
        if (cls == null) return DeviceInfo.UNKNOWN;
        return switch (cls) {
            case "08" -> "Mass Storage";
            case "03" -> "HID";
            case "02" -> "Communications";
            case "09" -> "Hub";
            case "e0" -> "Wireless";
            default   -> "Class " + cls;
        };
    }
}
