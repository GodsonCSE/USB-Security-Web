package com.usbsecurity;

import com.usbsecurity.monitor.DeviceInfo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeviceInfoTest {

    @Test void nullFields_defaultToUnknown_neverNull() {
        DeviceInfo d = new DeviceInfo();
        assertNotNull(d.getDeviceName()); assertNotNull(d.getVendorId());
        assertEquals(DeviceInfo.UNKNOWN, d.getDeviceName());
    }

    @Test void fingerprintWithSerial_containsSerial() {
        DeviceInfo d = new DeviceInfo();
        d.setVendorId("0x0781"); d.setProductId("0x5581"); d.setSerialNumber("ABC123");
        assertTrue(d.fingerprint().contains("ABC123"));
    }

    @Test void fingerprintWithoutSerial_usesNoSerialSentinel() {
        DeviceInfo d = new DeviceInfo();
        d.setVendorId("0x1234"); d.setProductId("0x5678");
        assertTrue(d.fingerprint().contains("NOSERIAL-"));
    }

    @Test void fingerprintIsCaseInsensitive() {
        DeviceInfo a = new DeviceInfo(); a.setVendorId("0x046d"); a.setProductId("0xC52B"); a.setSerialNumber("sn-abc");
        DeviceInfo b = new DeviceInfo(); b.setVendorId("0x046D"); b.setProductId("0xc52b"); b.setSerialNumber("SN-ABC");
        assertEquals(a.fingerprint(), b.fingerprint());
    }

    @Test void sameFingerprint_equalDevices() {
        DeviceInfo a = new DeviceInfo(); a.setVendorId("0x1234"); a.setProductId("0x5678"); a.setSerialNumber("SN");
        DeviceInfo b = new DeviceInfo(); b.setVendorId("0x1234"); b.setProductId("0x5678"); b.setSerialNumber("SN");
        assertEquals(a, b); assertEquals(a.hashCode(), b.hashCode());
    }
}
