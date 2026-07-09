package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.service.LivePresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
public class LiveActivityController {

    @Autowired
    private LivePresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/presence")
    public Map<String, Integer> getPresence() {
        Map<String, Integer> response = new HashMap<>();
        response.put("count", presenceService.getActiveCount());
        return response;
    }

    // Helper to broadcast global activity pulses
    public void broadcastPulse(String message, String type) {
        Map<String, String> pulse = new HashMap<>();
        pulse.put("message", message);
        pulse.put("type", type);
        pulse.put("timestamp", String.valueOf(System.currentTimeMillis()));
        messagingTemplate.convertAndSend("/topic/global/activity", pulse);
    }

    @PostMapping("/pulse")
    public void postPulse(@RequestBody Map<String, String> pulse) {
        broadcastPulse(pulse.get("message"), pulse.get("type"));
    }

    @MessageMapping("/pulse")
    @SendTo("/topic/global/activity")
    public Map<String, String> handlePulse(Map<String, String> pulse) {
        return pulse;
    }
}
