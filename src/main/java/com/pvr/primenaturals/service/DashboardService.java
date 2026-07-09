package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.Order;
import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.repository.OrderRepository;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Order> allOrders = orderRepository.findAll();
        stats.put("totalOrders", allOrders.size());
        
        BigDecimal totalSales = allOrders.stream()
                .filter(o -> o.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalSales", totalSales);
        
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProducts", productRepository.count());
        
        return stats;
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.isActive() && p.getStockQuantity() < 10)
                .collect(Collectors.toList());
    }

    public List<Order> getRecentOrders() {
        // Simple implementation: get all and slice. 
        // In production, use Pageable but this is fine for now.
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .limit(5)
                .collect(Collectors.toList());
    }
}
