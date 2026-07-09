package com.pvr.primenaturals.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real-time Collaborative Editing Controller for the Admin Product Management module.
 *
 * <p>Tracks which admin sessions are actively editing which products and broadcasts
 * presence updates to all connected admin clients via STOMP over WebSocket.</p>
 *
 * <p>Topics used:
 * <ul>
 *   <li>{@code /app/product/collab/start}  — admin opened edit modal</li>
 *   <li>{@code /app/product/collab/stop}   — admin closed edit modal</li>
 *   <li>{@code /topic/product/collab}      — broadcast channel for all clients</li>
 * </ul>
 * </p>
 */
@Controller
public class ProductCollabController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Maps a WebSocket session ID → the product ID that session is currently editing.
     * Allows efficient cleanup when a session disconnects unexpectedly.
     */
    private final ConcurrentHashMap<String, Long> sessionToProductMap = new ConcurrentHashMap<>();

    /**
     * Maps a WebSocket session ID → the editor name (username/email) for that session.
     * Required to properly remove the editor name on unexpected disconnect.
     */
    private final ConcurrentHashMap<String, String> sessionToUsernameMap = new ConcurrentHashMap<>();

    /**
     * Maps a product ID → a set of editor display names (username/email) currently editing it.
     * Used to build the presence payload broadcast to all clients.
     */
    private final ConcurrentHashMap<Long, Set<String>> productToEditorsMap = new ConcurrentHashMap<>();

    // ─────────────────────────────────────────────────────────────────────────────
    //  STOMP Message Handlers
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Invoked when an admin opens the Edit modal for a product.
     * Payload: {@code { "productId": <Long>, "editorName": "<String>" }}
     */
    @MessageMapping("/product/collab/start")
    public void handleCollabStart(Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Long productId = toLong(payload.get("productId"));
        String editorName = (String) payload.getOrDefault("editorName", "An admin");

        if (productId == null || sessionId == null) return;

        // If this session was already editing something else, clean it up first
        cleanupSession(sessionId);

        // Register the new editing state
        sessionToProductMap.put(sessionId, productId);
        sessionToUsernameMap.put(sessionId, editorName);
        productToEditorsMap.computeIfAbsent(productId, k -> ConcurrentHashMap.newKeySet()).add(editorName);

        broadcastCollabState(productId);
    }

    /**
     * Invoked when an admin closes the Edit modal (cancel, save, or ESC).
     * Payload: {@code { "productId": <Long>, "editorName": "<String>" }}
     */
    @MessageMapping("/product/collab/stop")
    public void handleCollabStop(Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Long productId = toLong(payload.get("productId"));
        String editorName = (String) payload.getOrDefault("editorName", "An admin");

        if (productId == null || sessionId == null) return;

        sessionToProductMap.remove(sessionId);
        sessionToUsernameMap.remove(sessionId);
        Set<String> editors = productToEditorsMap.get(productId);
        if (editors != null) {
            editors.remove(editorName);
            if (editors.isEmpty()) {
                productToEditorsMap.remove(productId);
            }
        }

        broadcastCollabState(productId);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Disconnect Cleanup — fires when a browser tab closes or network drops
    // ─────────────────────────────────────────────────────────────────────────────

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long productId = sessionToProductMap.remove(sessionId);
        String username = sessionToUsernameMap.remove(sessionId);
        if (productId != null && username != null) {
            Set<String> editors = productToEditorsMap.get(productId);
            if (editors != null) {
                editors.remove(username);
                if (editors.isEmpty()) {
                    productToEditorsMap.remove(productId);
                }
            }
            broadcastCollabState(productId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    //  Internal Helpers
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Cleans up a session's previous edit-lock if it opened a different product earlier
     * without properly sending a /stop message.
     */
    private void cleanupSession(String sessionId) {
        Long previousProductId = sessionToProductMap.remove(sessionId);
        String username = sessionToUsernameMap.remove(sessionId);
        if (previousProductId != null && username != null) {
            Set<String> editors = productToEditorsMap.get(previousProductId);
            if (editors != null) {
                editors.remove(username);
                if (editors.isEmpty()) {
                    productToEditorsMap.remove(previousProductId);
                }
            }
            broadcastCollabState(previousProductId);
        }
    }

    /**
     * Builds and broadcasts the current presence state for a product to all subscribers.
     * Payload format:
     * <pre>
     * {
     *   "productId": 42,
     *   "editors": ["admin@pvr.com", "jane@pvr.com"]
     * }
     * </pre>
     */
    private void broadcastCollabState(Long productId) {
        Set<String> editors = productToEditorsMap.getOrDefault(productId, Collections.emptySet());
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", productId);
        payload.put("editors", new ArrayList<>(editors));
        messagingTemplate.convertAndSend("/topic/product/collab", payload);
    }

    /**
     * Safely converts an Object (which may be Integer or Long from JSON deserialization) to Long.
     */
    private Long toLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        try { return Long.parseLong(obj.toString()); } catch (NumberFormatException e) { return null; }
    }
}
