package com.usbsecurity.monitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacOsUsbMonitor extends AbstractPollingUsbMonitor {

    private static final Pattern HEX_ID_PATTERN = Pattern.compile("0[xX]([0-9a-fA-F]+)");
    private final ObjectMapper mapper = new ObjectMapper();

    public MacOsUsbMonitor(int pollMs) { super(pollMs); }

    @Override
    protected List<DeviceInfo> readDevices() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("system_profiler", "SPUSBDataType", "-json", "-detailLevel", "basic");
        pb.redirectErrorStream(false);
        Process p = pb.start();

        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        }
        if (!p.waitFor(15, TimeUnit.SECONDS)) { p.destroyForcibly(); throw new RuntimeException("system_profiler timeout"); }
        return parse(sb.toString());
    }

    protected List<DeviceInfo> parse(String json) {
        List<DeviceInfo> list = new ArrayList<>();
        if (json == null || json.isBlank()) return list;

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode usbData = root.path("SPUSBDataType");
            if (usbData.isArray()) {
                for (JsonNode bus : usbData) {
                    collectDevices(bus, list);
                }
            }
        } catch (Exception e) {
            // fallback if JSON parsing error
        }
        return list;
    }

    private void collectDevices(JsonNode node, List<DeviceInfo> list) {
        if (node == null || node.isMissingNode()) return;

        if (node.has("_name")) {
            String name = node.path("_name").asText(null);
            String vidRaw = node.path("vendor_id").asText(null);
            String pidRaw = node.path("product_id").asText(null);
            String serial = node.path("serial_num").asText(null);
            String manufacturer = node.path("manufacturer").asText(null);

            // If it has vendor_id or product_id, it is a detected USB device
            if (vidRaw != null || pidRaw != null) {
                DeviceInfo d = new DeviceInfo();
                d.setDeviceName(name);
                d.setSerialNumber(serial);
                d.setManufacturer(manufacturer);

                if (vidRaw != null) {
                    Matcher m = HEX_ID_PATTERN.matcher(vidRaw);
                    d.setVendorId(m.find() ? "0x" + m.group(1).toUpperCase() : vidRaw.trim().toUpperCase());
                }

                if (pidRaw != null) {
                    Matcher m = HEX_ID_PATTERN.matcher(pidRaw);
                    d.setProductId(m.find() ? "0x" + m.group(1).toUpperCase() : pidRaw.trim().toUpperCase());
                }

                list.add(d);
            }
        }

        // Recursively inspect any nested child items / hubs
        JsonNode items = node.path("_items");
        if (items.isArray()) {
            for (JsonNode child : items) {
                collectDevices(child, list);
            }
        }
    }
}
