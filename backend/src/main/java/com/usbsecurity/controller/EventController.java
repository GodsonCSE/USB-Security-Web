package com.usbsecurity.controller;

import com.usbsecurity.model.EventType;
import com.usbsecurity.model.RiskLevel;
import com.usbsecurity.model.UsbEvent;
import com.usbsecurity.repository.UsbEventRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final UsbEventRepository eventRepo;

    public EventController(UsbEventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    @GetMapping("/recent")
    public List<UsbEvent> recent() {
        return eventRepo.findTop20ByOrderByTimestampDesc();
    }

    @GetMapping
    public List<UsbEvent> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) RiskLevel risk,
            @RequestParam(required = false) String device) {

        LocalDateTime fromDt = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDt   = to   != null ? to.plusDays(1).atStartOfDay() : null;
        String        nameQ  = (device != null && !device.isBlank()) ? device : null;

        return eventRepo.search(fromDt, toDt, type, risk, nameQ);
    }
}
