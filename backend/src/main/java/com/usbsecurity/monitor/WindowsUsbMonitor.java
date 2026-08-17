package com.usbsecurity.monitor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

public class WindowsUsbMonitor extends AbstractPollingUsbMonitor {

    private static final Pattern VID = Pattern.compile("VID_([0-9A-Fa-f]{4})");
    private static final Pattern PID = Pattern.compile("PID_([0-9A-Fa-f]{4})");

    public WindowsUsbMonitor(int pollMs) { super(pollMs); }

    @Override
    protected List<DeviceInfo> readDevices() throws Exception {
        String cmd = "Get-PnpDevice -PresentOnly | Where-Object { $_.InstanceId -like 'USB*' } | " +
                     "Select-Object FriendlyName,Manufacturer,InstanceId,Class | ConvertTo-Csv -NoTypeInformation";
        ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", cmd);
        pb.redirectErrorStream(false);
        Process p = pb.start();

        List<DeviceInfo> list = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreEmptyLines(true)
                    .setTrim(true)
                    .build();
            try (CSVParser parser = format.parse(r)) {
                for (CSVRecord record : parser) {
                    DeviceInfo d = parseRecord(record);
                    if (d != null) list.add(d);
                }
            }
        }
        if (!p.waitFor(10, TimeUnit.SECONDS)) { p.destroyForcibly(); throw new RuntimeException("PowerShell timeout"); }
        return list;
    }

    protected DeviceInfo parseRecord(CSVRecord record) {
        if (record.size() < 3) return null;
        String friendlyName = record.get(0);
        String manufacturer = record.get(1);
        String instanceId   = record.get(2);
        String deviceClass  = record.size() > 3 ? record.get(3) : null;

        DeviceInfo d = new DeviceInfo();
        d.setDeviceName(friendlyName);
        d.setManufacturer(manufacturer);
        d.setRawIdentifier(instanceId);
        d.setDeviceType(deviceClass);

        if (instanceId != null) {
            Matcher v = VID.matcher(instanceId);
            if (v.find()) d.setVendorId("0x" + v.group(1).toUpperCase());
            Matcher p2 = PID.matcher(instanceId);
            if (p2.find()) d.setProductId("0x" + p2.group(1).toUpperCase());

            String[] parts = instanceId.split("\\\\");
            if (parts.length >= 3 && !parts[2].contains("&")) d.setSerialNumber(parts[2]);
        }
        return d;
    }
}
