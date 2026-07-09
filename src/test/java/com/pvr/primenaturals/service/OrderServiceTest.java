package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.OrderPlaceRequest;
import com.pvr.primenaturals.dto.response.OrderDTO;
import com.pvr.primenaturals.entity.*;
import com.pvr.primenaturals.exception.InsufficientStockException;
import com.pvr.primenaturals.repository.*;
import com.pvr.primenaturals.shipping.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private ShippingService shippingService;

    @Mock
    private CourierService courierService;

    @Mock
    private OrderTrackingRepository orderTrackingRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RazorpayService razorpayService;

    @Mock
    private CouponService couponService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Address address;
    private Product product;
    private Cart cart;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        address = new Address();
        address.setId(10L);
        address.setUser(user);
        address.setReceiverName("Test User");
        address.setStreetAddress("123 Spice Street");
        address.setCity("Hyderabad");
        address.setState("Telangana");
        address.setPincode("500001");
        address.setPhoneNumber("9999999999");
        address.setCountry("IN");

        product = new Product();
        product.setId(20L);
        product.setName("Turmeric Powder");
        product.setPrice(new BigDecimal("150.00"));
        product.setStockQuantity(10);
        product.setActive(true);

        cart = new Cart();
        cart.setId(30L);
        cart.setUser(user);
        
        CartItem cartItem = new CartItem();
        cartItem.setId(40L);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cart.setCartItems(new ArrayList<>(Collections.singletonList(cartItem)));
    }

    @Test
    public void testPlaceOrder_COD_Success() {
        OrderPlaceRequest request = new OrderPlaceRequest();
        request.setAddressId(10L);
        request.setPaymentMethod("COD");
        request.setOrderNotes("Fragile items");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        ShippingResponse shippingResponse = ShippingResponse.builder()
                .serviceable(true)
                .deliveryDate("2026-07-10")
                .shippingCharge(new BigDecimal("40.00"))
                .codAvailable(true)
                .build();
        when(shippingService.checkShipping(any(ShippingRequest.class))).thenReturn(shippingResponse);

        Warehouse warehouse = Warehouse.builder().name("Hyderabad Warehouse").build();
        when(warehouseRepository.findAll()).thenReturn(Collections.singletonList(warehouse));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        OrderDTO response = orderService.placeOrder("test@example.com", request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("COD", response.getPaymentMethod());
        assertEquals(OrderStatus.PLACED.name(), response.getStatus());
        assertEquals(new BigDecimal("340.00"), response.getTotalAmount()); // 150*2 + 40 = 340
        assertEquals(8, product.getStockQuantity()); // 10 - 2 = 8

        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cartRepository, times(1)).save(cart);
        verify(emailService, times(1)).sendOrderConfirmation(any(OrderDTO.class), eq("test@example.com"));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/orders/admin"), any(OrderDTO.class));
    }

    @Test
    public void testPlaceOrder_Online_Success() {
        OrderPlaceRequest request = new OrderPlaceRequest();
        request.setAddressId(10L);
        request.setPaymentMethod("ONLINE");
        request.setRazorpayOrderId("razor_order_123");
        request.setRazorpayPaymentId("razor_pay_123");
        request.setRazorpaySignature("razor_sig_123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        ShippingResponse shippingResponse = ShippingResponse.builder()
                .serviceable(true)
                .deliveryDate("2026-07-10")
                .shippingCharge(new BigDecimal("0.00"))
                .codAvailable(true)
                .build();
        when(shippingService.checkShipping(any(ShippingRequest.class))).thenReturn(shippingResponse);
        when(razorpayService.verifyPaymentSignature("razor_order_123", "razor_pay_123", "razor_sig_123")).thenReturn(true);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(101L);
            order.setCreatedAt(LocalDateTime.now());
            return order;
        });

        OrderDTO response = orderService.placeOrder("test@example.com", request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("ONLINE", response.getPaymentMethod());
        assertEquals("COMPLETED", response.getPaymentStatus());
        assertEquals(OrderStatus.PLACED.name(), response.getStatus());

        verify(razorpayService, times(1)).verifyPaymentSignature("razor_order_123", "razor_pay_123", "razor_sig_123");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testPlaceOrder_InsufficientStock_ThrowsException() {
        OrderPlaceRequest request = new OrderPlaceRequest();
        request.setAddressId(10L);
        request.setPaymentMethod("COD");

        // Request 15 units when stock is 10
        cart.getCartItems().get(0).setQuantity(15);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        ShippingResponse shippingResponse = ShippingResponse.builder()
                .serviceable(true)
                .deliveryDate("2026-07-10")
                .shippingCharge(new BigDecimal("40.00"))
                .codAvailable(true)
                .build();
        when(shippingService.checkShipping(any(ShippingRequest.class))).thenReturn(shippingResponse);

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder("test@example.com", request));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    public void testCancelOrder_Success() {
        Order order = new Order();
        order.setId(100L);
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMethod("COD");
        order.setTotalAmount(new BigDecimal("340.00"));
        order.setCity("Hyderabad");

        OrderItem orderItem = new OrderItem();
        orderItem.setId(110L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPriceAtPurchase(new BigDecimal("150.00"));
        order.setOrderItems(new ArrayList<>(Collections.singletonList(orderItem)));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO response = orderService.cancelOrder(100L, "test@example.com");

        assertNotNull(response);
        assertEquals(OrderStatus.CANCELLED.name(), response.getStatus());
        assertEquals(12, product.getStockQuantity()); // 10 + 2 = 12 (restored stock)

        verify(orderRepository, times(1)).save(order);
        verify(emailService, times(1)).sendOrderStatusUpdate(any(OrderDTO.class), eq("test@example.com"));
    }
}
