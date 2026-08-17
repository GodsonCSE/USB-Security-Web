package com.usbsecurity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages all active browser SSE connections.
 * When the backend detects a USB event it calls broadcast() which pushes
 * a named event to every connected browser tab simultaneously.
 */
@Service
public class SseService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // no timeout — browser reconnects automatically
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(()    -> emitters.remove(emitter));
        emitter.onError(e ->      emitters.remove(emitter));

        // Send a heartbeat so the browser knows the connection is alive
        try {
            emitter.send(SseEmitter.event().name("CONNECTED").data("ready", MediaType.TEXT_PLAIN));
        } catch (Exception ignored) {}

        return emitter;
    }

    public void broadcast(String eventName, Object payload) {
        List<SseEmitter> dead = new ArrayList<>();
        String json;
        try {
            json = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(json, MediaType.APPLICATION_JSON));
            } catch (Exception e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    public int getActiveConnectionCount() { return emitters.size(); }
}
