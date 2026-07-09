package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.ReturnRequestDTO;
import com.pvr.primenaturals.entity.*;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.OrderRepository;
import com.pvr.primenaturals.repository.ReturnRequestRepository;
import com.pvr.primenaturals.repository.UserRepository;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReturnRequestService {

    @Autowired private ReturnRequestRepository returnRequestRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository productVariantRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    /** Customer: submit a return request for a DELIVERED order */
    @Transactional
    public ReturnRequest submitRequest(String email, ReturnRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized: not your order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Only delivered orders can be returned");
        }

        // Mark order as return-requested
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        ReturnRequest rr = ReturnRequest.builder()
                .order(order)
                .user(user)
                .reason(dto.getReason())
                .description(dto.getDescription())
                .build();
        return returnRequestRepository.save(rr);
    }

    /** Customer: view their own return requests */
    public List<ReturnRequest> getMyRequests(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return returnRequestRepository.findByUserId(user.getId());
    }

    /** Admin: view all return requests */
    public List<ReturnRequest> getAllRequests() {
        return returnRequestRepository.findAll();
    }

    /** Admin: approve a return request */
    @Transactional
    public ReturnRequest approveRequest(Long id, String adminNote) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        rr.setStatus(ReturnRequest.ReturnStatus.APPROVED);
        rr.setAdminNote(adminNote);
        
        Order order = rr.getOrder();
        order.setStatus(OrderStatus.RETURNED);
        orderRepository.save(order);

        // Restore stock for returned items
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);

                if (item.getVariant() != null) {
                    ProductVariant variant = item.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    productVariantRepository.save(variant);
                }

                // Broadcast stock updates
                try {
                    messagingTemplate.convertAndSend("/topic/products/stock",
                        new com.pvr.primenaturals.dto.response.StockUpdateDTO(product.getId(), product.getStockQuantity()));
                } catch (Exception e) {
                    System.err.println("Failed to send return stock update: " + e.getMessage());
                }
            }
        }

        return returnRequestRepository.save(rr);
    }

    /** Admin: reject a return request */
    @Transactional
    public ReturnRequest rejectRequest(Long id, String adminNote) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        rr.setStatus(ReturnRequest.ReturnStatus.REJECTED);
        rr.setAdminNote(adminNote);
        rr.setResolvedAt(LocalDateTime.now());
        // Revert order to DELIVERED
        rr.getOrder().setStatus(OrderStatus.DELIVERED);
        orderRepository.save(rr.getOrder());
        return returnRequestRepository.save(rr);
    }

    /** Admin: mark refund as initiated */
    @Transactional
    public ReturnRequest initiateRefund(Long id) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        rr.setStatus(ReturnRequest.ReturnStatus.REFUND_INITIATED);
        rr.getOrder().setStatus(OrderStatus.REFUND_INITIATED);
        rr.getOrder().setPaymentStatus("REFUND_INITIATED");
        orderRepository.save(rr.getOrder());
        return returnRequestRepository.save(rr);
    }

    /** Admin: mark refund as completed */
    @Transactional
    public ReturnRequest completeRefund(Long id) {
        ReturnRequest rr = returnRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));
        rr.setStatus(ReturnRequest.ReturnStatus.REFUNDED);
        rr.setResolvedAt(LocalDateTime.now());
        rr.getOrder().setStatus(OrderStatus.REFUNDED);
        rr.getOrder().setPaymentStatus("REFUNDED");
        orderRepository.save(rr.getOrder());
        return returnRequestRepository.save(rr);
    }
}
