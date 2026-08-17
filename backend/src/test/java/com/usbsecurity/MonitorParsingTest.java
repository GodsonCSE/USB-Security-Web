package com.usbsecurity;

import com.usbsecurity.monitor.DeviceInfo;
import com.usbsecurity.monitor.MacOsUsbMonitor;
import com.usbsecurity.monitor.WindowsUsbMonitor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MonitorParsingTest {

    static class TestableMacOsUsbMonitor extends MacOsUsbMonitor {
        public TestableMacOsUsbMonitor() { super(1000); }
        public List<DeviceInfo> parseJson(String json) { return parse(json); }
    }

    static class TestableWindowsUsbMonitor extends WindowsUsbMonitor {
        public TestableWindowsUsbMonitor() { super(1000); }
        public DeviceInfo parseLine(String line) throws Exception {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            String csv = "\"FriendlyName\",\"Manufacturer\",\"InstanceId\",\"Class\"\n" + line;
            try (CSVParser parser = format.parse(new StringReader(csv))) {
                for (CSVRecord rec : parser) {
                    return parseRecord(rec);
                }
            }
            return null;
        }
    }

    @Test
    void testMacOsParserWithAppleVendorStringAndNesting() {
        String json = """
        {
          "SPUSBDataType": [
            {
              "_name": "USB 3.1 Bus",
              "host_controller": "AppleUSBXHCISPT",
              "_items": [
                {
                  "_name": "Apple Internal Keyboard / Trackpad",
                  "vendor_id": "0x05ac (Apple Inc.)",
                  "product_id": "0x0274",
                  "serial_num": "FM671234ABC",
                  "manufacturer": "Apple Inc."
                },
                {
                  "_name": "USB3.0 Hub",
                  "vendor_id": "0x0bda (Realtek Semiconductor Corp.)",
                  "product_id": "0x0411",
                  "_items": [
                    {
                      "_name": "SanDisk Ultra",
                      "vendor_id": "0x0781",
                      "product_id": "0x5583",
                      "serial_num": "SANDISK12345",
                      "manufacturer": "SanDisk"
                    }
                  ]
                }
              ]
            }
          ]
        }
        """;

        TestableMacOsUsbMonitor monitor = new TestableMacOsUsbMonitor();
        List<DeviceInfo> devices = monitor.parseJson(json);

        assertEquals(3, devices.size());

        DeviceInfo kbd = devices.get(0);
        assertEquals("Apple Internal Keyboard / Trackpad", kbd.getDeviceName());
        assertEquals("0x05AC", kbd.getVendorId());
        assertEquals("0x0274", kbd.getProductId());
        assertEquals("FM671234ABC", kbd.getSerialNumber());

        DeviceInfo disk = devices.get(2);
        assertEquals("SanDisk Ultra", disk.getDeviceName());
        assertEquals("0x0781", disk.getVendorId());
        assertEquals("0x5583", disk.getProductId());
    }

    @Test
    void testWindowsCsvParserWithQuotesAndCommas() throws Exception {
        TestableWindowsUsbMonitor monitor = new TestableWindowsUsbMonitor();
        String line = "\"USB Optical Mouse, \"\"Special Edition\"\"\",\"Logitech, Inc.\",\"USB\\VID_046D&PID_C077\\5&1A2B3C&0&1\",\"Mouse\"";

        DeviceInfo d = monitor.parseLine(line);
        assertNotNull(d);
        assertEquals("USB Optical Mouse, \"Special Edition\"", d.getDeviceName());
        assertEquals("Logitech, Inc.", d.getManufacturer());
        assertEquals("0x046D", d.getVendorId());
        assertEquals("0xC077", d.getProductId());
    }
}
