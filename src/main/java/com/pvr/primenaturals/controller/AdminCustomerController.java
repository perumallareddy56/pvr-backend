package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.entity.Order;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.OrderRepository;
import com.pvr.primenaturals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;

    /** List all customers (non-admin users) */
    @GetMapping
    public List<Map<String, Object>> getAllCustomers(
            @RequestParam(required = false) String search) {

        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getRole().name().equals("USER"))
                .collect(Collectors.toList());

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(q)
                            || u.getEmail().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        return users.stream().map(this::toSummary).collect(Collectors.toList());
    }

    /** Get full profile of a single customer */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomer(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        List<Order> orders = orderRepository.findByUserId(id);

        BigDecimal totalSpent = orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = toSummary(user);
        result.put("totalOrders", orders.size());
        result.put("totalSpent", totalSpent);
        result.put("orders", orders.stream().map(o -> Map.of(
                "id", o.getId(),
                "status", o.getStatus(),
                "totalAmount", o.getTotalAmount(),
                "createdAt", o.getCreatedAt()
        )).collect(Collectors.toList()));
        return ResponseEntity.ok(result);
    }

    @Autowired private com.pvr.primenaturals.service.AuditLogService auditLogService;

    /** Block a customer */
    @PatchMapping("/{id}/block")
    public ResponseEntity<?> blockCustomer(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        user.setBlocked(true);
        userRepository.save(user);
        
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("BLOCK_USER", operator, "Blocked customer: " + user.getEmail(), req.getRemoteAddr());

        return ResponseEntity.ok(Map.of("message", "Customer blocked", "userId", id));
    }

    /** Unblock a customer */
    @PatchMapping("/{id}/unblock")
    public ResponseEntity<?> unblockCustomer(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        user.setBlocked(false);
        userRepository.save(user);

        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UNBLOCK_USER", operator, "Unblocked customer: " + user.getEmail(), req.getRemoteAddr());

        return ResponseEntity.ok(Map.of("message", "Customer unblocked", "userId", id));
    }

    private Map<String, Object> toSummary(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("email", user.getEmail());
        map.put("phoneNumber", user.getPhoneNumber());
        map.put("blocked", user.isBlocked());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }
}
