package com.usbsecurity;

import com.usbsecurity.model.EventType;
import com.usbsecurity.model.RiskLevel;
import com.usbsecurity.model.UsbDevice;
import com.usbsecurity.model.UsbEvent;
import com.usbsecurity.service.ReportService;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    @Test
    void testFormulaInjectionSanitization() throws IOException {
        ReportService reportService = new ReportService();

        UsbDevice d = new UsbDevice();
        d.setDeviceName("=SUM(1+1)");
        d.setManufacturer("+calc");
        d.setVendorId("-1234");
        d.setProductId("@cmd");
        d.setSerialNumber("\tmalicious");

        UsbEvent event = new UsbEvent(d, EventType.CONNECTED, RiskLevel.HIGH, "=cmd|' /C calc'!A0");
        event.setTimestamp(LocalDateTime.now());

        byte[] csvBytes = reportService.exportCsv(List.of(event));
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        assertTrue(csv.contains("''=SUM(1+1)") || csv.contains("'=SUM(1+1)"));
        assertTrue(csv.contains("'+calc"));
        assertTrue(csv.contains("'-1234"));
        assertTrue(csv.contains("'@cmd"));
        assertTrue(csv.contains("'\tmalicious"));
        assertTrue(csv.contains("'=cmd|' /C calc'!A0"));
    }
}
