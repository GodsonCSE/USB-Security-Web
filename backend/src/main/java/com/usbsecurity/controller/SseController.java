package com.usbsecurity.controller;

import com.usbsecurity.service.SseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseController {

    private final SseService sseService;

    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

    /**
     * Browser connects here to receive live USB events.
     * Uses the browser's built-in EventSource API — no polling needed.
     * The browser auto-reconnects if the connection drops.
     */
    @GetMapping("/sse/events")
    public SseEmitter subscribe() {
        return sseService.subscribe();
    }
}
