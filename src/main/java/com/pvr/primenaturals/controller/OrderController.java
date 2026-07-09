package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.request.OrderPlaceRequest;
import com.pvr.primenaturals.dto.response.OrderDTO;
import com.pvr.primenaturals.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.razorpay.RazorpayException;

// Service layer integration
import com.pvr.primenaturals.service.RazorpayService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
 
    @Autowired
    private RazorpayService razorpayService;

    @PostMapping("/place")
    public OrderDTO placeOrder(Authentication authentication, @RequestBody OrderPlaceRequest request) {
        System.out.println("DEBUG: Order placement request for user: " + authentication.getName() + " with method: " + request.getPaymentMethod());
        try {
            OrderDTO order = orderService.placeOrder(authentication.getName(), request);
            System.out.println("DEBUG: Order placed successfully: " + order.getId());
            return order;
        } catch (Exception e) {
            System.err.println("DEBUG: Order placement failed for user " + authentication.getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
 
    @PostMapping("/payment/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPaymentOrder(@RequestBody Map<String, Object> data) {
        System.out.println("DEBUG: Payment order creation requested in OrderController with data: " + data);
        try {
            if (data == null || data.get("amount") == null) {
                return ResponseEntity.badRequest().body("Amount is required");
            }
            BigDecimal amount = new BigDecimal(data.get("amount").toString());
            System.out.println("DEBUG: Creating Razorpay order for amount: " + amount);
            Map<String, Object> orderDetails = razorpayService.createOrder(amount);
            return ResponseEntity.ok(orderDetails);
        } catch (RazorpayException e) {
            System.err.println("RAZORPAY LIVE ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Razorpay API Error: " + e.getMessage());
        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR during payment order creation: " + t.getMessage());
            t.printStackTrace();
            return ResponseEntity.status(500).body("Server Error: " + t.getClass().getSimpleName() + " - " + t.getMessage());
        }
    }

    @GetMapping("/user")
    public List<OrderDTO> getUserOrders(Authentication authentication) {
        return orderService.getUserOrders(authentication.getName());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderDTO updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateOrderStatus(id, status);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public OrderDTO cancelOrder(@PathVariable Long id, Authentication authentication) {
        return orderService.cancelOrder(id, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public org.springframework.http.ResponseEntity<?> deleteOrder(@PathVariable Long id, Authentication authentication) {
        if (authentication == null) {
            return org.springframework.http.ResponseEntity.status(401).body("Unauthorized: Missing valid JWT token");
        }
        try {
            orderService.deleteOrder(id, authentication.getName());
            return org.springframework.http.ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = e.getClass().getName() + ": " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += " | CAUSE: " + e.getCause().getClass().getName() + ": " + e.getCause().getMessage();
            }
            return org.springframework.http.ResponseEntity.status(500).body(errorMsg);
        }
    }
}
