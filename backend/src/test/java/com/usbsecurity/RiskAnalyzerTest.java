package com.usbsecurity;

import com.usbsecurity.monitor.DeviceInfo;
import com.usbsecurity.model.RiskLevel;
import com.usbsecurity.security.RiskAnalyzer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RiskAnalyzerTest {

    private final RiskAnalyzer analyzer = new RiskAnalyzer();

    @Test void trustedDevice_isAlwaysLow() {
        assertEquals(RiskLevel.LOW, analyzer.analyze(new DeviceInfo(), true).level());
    }

    @Test void completeMetadata_untrusted_isMedium() {
        DeviceInfo d = new DeviceInfo();
        d.setDeviceName("Logitech Mouse"); d.setManufacturer("Logitech");
        d.setVendorId("0x046D"); d.setProductId("0xC52B"); d.setSerialNumber("SN001");
        assertEquals(RiskLevel.MEDIUM, analyzer.analyze(d, false).level());
    }

    @Test void missingThreeFields_isHigh() {
        DeviceInfo d = new DeviceInfo(); // all defaults = Unknown
        assertEquals(RiskLevel.HIGH, analyzer.analyze(d, false).level());
    }

    @Test void reasonIsNeverBlank() {
        assertFalse(analyzer.analyze(new DeviceInfo(), false).reason().isBlank());
        assertFalse(analyzer.analyze(new DeviceInfo(), true).reason().isBlank());
    }

    @Test void missingOneField_isMedium() {
        DeviceInfo d = new DeviceInfo();
        d.setDeviceName("Flash Drive"); d.setManufacturer("Generic");
        d.setVendorId("0x1234"); d.setProductId("0x5678");
        // serial stays Unknown
        assertEquals(RiskLevel.MEDIUM, analyzer.analyze(d, false).level());
    }
}
