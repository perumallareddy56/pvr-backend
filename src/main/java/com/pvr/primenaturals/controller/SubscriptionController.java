package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.request.SubscriptionRequest;
import com.pvr.primenaturals.entity.Subscription;
import com.pvr.primenaturals.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Subscription> getMySubscriptions(Authentication auth) {
        return subscriptionService.getUserSubscriptions(auth.getName());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Subscription> create(Authentication auth, @RequestBody SubscriptionRequest req) {
        return ResponseEntity.ok(subscriptionService.createSubscription(auth.getName(), req));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Subscription> togglePause(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.pauseResume(auth.getName(), id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> cancel(Authentication auth, @PathVariable Long id) {
        subscriptionService.cancel(auth.getName(), id);
        return ResponseEntity.ok("Subscription cancelled");
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Subscription> getAllActive() {
        return subscriptionService.getAllActive();
    }
}
