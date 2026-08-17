package com.usbsecurity.controller;

import com.usbsecurity.model.EventType;
import com.usbsecurity.model.RiskLevel;
import com.usbsecurity.model.UsbEvent;
import com.usbsecurity.repository.UsbEventRepository;
import com.usbsecurity.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final UsbEventRepository eventRepo;
    private final ReportService      reportService;

    public ExportController(UsbEventRepository eventRepo, ReportService reportService) {
        this.eventRepo     = eventRepo;
        this.reportService = reportService;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) RiskLevel risk,
            @RequestParam(required = false) String device) throws Exception {

        List<UsbEvent> events = fetchEvents(from, to, type, risk, device);
        byte[] csv = reportService.exportCsv(events);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usb_security_log.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) RiskLevel risk,
            @RequestParam(required = false) String device) throws Exception {

        List<UsbEvent> events = fetchEvents(from, to, type, risk, device);
        byte[] pdf = reportService.exportPdf(events);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"usb_security_log.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private List<UsbEvent> fetchEvents(LocalDate from, LocalDate to,
                                        EventType type, RiskLevel risk, String device) {
        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt   = to   != null ? to.plusDays(1).atStartOfDay() : null;
        String nameQ         = (device != null && !device.isBlank()) ? device : null;
        return eventRepo.search(fromDt, toDt, type, risk, nameQ);
    }
}
