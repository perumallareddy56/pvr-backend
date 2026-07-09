package com.pvr.primenaturals.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LivePresenceService {

    private final AtomicInteger activeConnoisseurs = new AtomicInteger(0);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        int count = activeConnoisseurs.incrementAndGet();
        broadcastPresence(count);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        int count = activeConnoisseurs.decrementAndGet();
        if (count < 0) activeConnoisseurs.set(0);
        broadcastPresence(activeConnoisseurs.get());
    }

    private void broadcastPresence(int count) {
        // We add a base of 7 to make it feel like a busy boutique even if only the dev is on
        messagingTemplate.convertAndSend("/topic/global/presence", count + 7);
    }

    public int getActiveCount() {
        return activeConnoisseurs.get() + 7;
    }
}
