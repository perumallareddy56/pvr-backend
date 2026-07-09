package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;


    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return dashboardService.getStats();
    }

    @GetMapping("/low-stock")
    public List<Map<String, Object>> getLowStockProducts() {
        return dashboardService.getLowStockProducts().stream()
                .map(p -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getName());
                    map.put("stockQuantity", p.getStockQuantity());
                    map.put("price", p.getPrice());
                    return map;
                }).collect(Collectors.toList());
    }

    @GetMapping("/recent-orders")
    public List<Map<String, Object>> getRecentOrders() {
        return dashboardService.getRecentOrders().stream()
                .map(order -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", order.getId());
                    map.put("userName", order.getUser() != null ? order.getUser().getName() : "Anonymous");
                    map.put("totalAmount", order.getTotalAmount());
                    map.put("status", order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");
                    map.put("createdAt", order.getCreatedAt());
                    return map;
                }).collect(Collectors.toList());
    }
}
