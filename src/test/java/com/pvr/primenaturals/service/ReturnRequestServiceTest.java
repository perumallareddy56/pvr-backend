package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.ReturnRequestDTO;
import com.pvr.primenaturals.entity.*;
import com.pvr.primenaturals.repository.OrderRepository;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.ProductVariantRepository;
import com.pvr.primenaturals.repository.ReturnRequestRepository;
import com.pvr.primenaturals.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReturnRequestServiceTest {

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ReturnRequestService returnRequestService;

    private User user;
    private Order order;
    private Product product;
    private ReturnRequest returnRequest;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        product = new Product();
        product.setId(20L);
        product.setName("Turmeric Powder");
        product.setPrice(new BigDecimal("150.00"));
        product.setStockQuantity(10);
        product.setActive(true);

        order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.DELIVERED);

        OrderItem orderItem = new OrderItem();
        orderItem.setId(110L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPriceAtPurchase(new BigDecimal("150.00"));
        order.setOrderItems(new ArrayList<>(Collections.singletonList(orderItem)));

        returnRequest = new ReturnRequest();
        returnRequest.setId(200L);
        returnRequest.setUser(user);
        returnRequest.setOrder(order);
        returnRequest.setStatus(ReturnRequest.ReturnStatus.PENDING);
        returnRequest.setReason("Item damaged");
    }

    @Test
    public void testSubmitRequest_Success() {
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setOrderId(100L);
        dto.setReason("Item damaged");
        dto.setDescription("Box was opened");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        ReturnRequest result = returnRequestService.submitRequest("test@example.com", dto);

        assertNotNull(result);
        assertEquals(OrderStatus.RETURN_REQUESTED, order.getStatus());
        assertEquals("Item damaged", result.getReason());
        verify(orderRepository, times(1)).save(order);
        verify(returnRequestRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    public void testSubmitRequest_NotDelivered_ThrowsException() {
        order.setStatus(OrderStatus.PLACED); // not delivered
        ReturnRequestDTO dto = new ReturnRequestDTO();
        dto.setOrderId(100L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        assertThrows(RuntimeException.class, () -> returnRequestService.submitRequest("test@example.com", dto));
    }

    @Test
    public void testApproveRequest_SuccessRestoresStock() {
        when(returnRequestRepository.findById(200L)).thenReturn(Optional.of(returnRequest));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);

        ReturnRequest result = returnRequestService.approveRequest(200L, "Approved Damaged Refund");

        assertNotNull(result);
        assertEquals(ReturnRequest.ReturnStatus.APPROVED, result.getStatus());
        assertEquals("Approved Damaged Refund", result.getAdminNote());
        assertEquals(OrderStatus.RETURNED, order.getStatus());
        assertEquals(12, product.getStockQuantity()); // 10 + 2 = 12 restored

        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(order);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/products/stock"), any(Object.class));
    }

    @Test
    public void testRejectRequest_SuccessRevertsStatus() {
        when(returnRequestRepository.findById(200L)).thenReturn(Optional.of(returnRequest));
        when(returnRequestRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);

        ReturnRequest result = returnRequestService.rejectRequest(200L, "Rejected: invalid reason");

        assertNotNull(result);
        assertEquals(ReturnRequest.ReturnStatus.REJECTED, result.getStatus());
        assertEquals(OrderStatus.DELIVERED, order.getStatus()); // reverted to DELIVERED
        assertEquals(10, product.getStockQuantity()); // no stock restoration

        verify(productRepository, never()).save(product);
        verify(orderRepository, times(1)).save(order);
    }
}
