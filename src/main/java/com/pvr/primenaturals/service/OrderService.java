package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.OrderPlaceRequest;
import com.pvr.primenaturals.dto.response.OrderDTO;
import com.pvr.primenaturals.dto.response.OrderItemDTO;
import com.pvr.primenaturals.entity.*;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.*;
import com.pvr.primenaturals.shipping.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private ShippingService shippingService;

    @Autowired
    private CourierService courierService;

    @Autowired
    private OrderTrackingRepository orderTrackingRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private CouponService couponService;

    @org.springframework.transaction.annotation.Transactional
    public OrderDTO placeOrder(String email, String paymentId, String paymentMethod) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Address address = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElseGet(() -> {
                    List<Address> list = addressRepository.findByUserId(user.getId());
                    if (!list.isEmpty()) return list.get(0);
                    Address fallback = new Address();
                    fallback.setUser(user);
                    fallback.setReceiverName(user.getName());
                    fallback.setStreetAddress(user.getAddress() != null ? user.getAddress() : "Default Address");
                    fallback.setCity("City");
                    fallback.setState("State");
                    fallback.setPincode("560001");
                    fallback.setPhoneNumber(user.getPhoneNumber() != null ? user.getPhoneNumber() : "0000000000");
                    return addressRepository.save(fallback);
                });

        OrderPlaceRequest request = new OrderPlaceRequest();
        request.setAddressId(address.getId());
        request.setPaymentMethod(paymentMethod);
        request.setRazorpayPaymentId(paymentId);
        return placeOrder(email, request);
    }

    @org.springframework.transaction.annotation.Transactional
    public OrderDTO placeOrder(String email, OrderPlaceRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized delivery address selection");
        }

        // Razorpay Online Signature Check
        if ("ONLINE".equalsIgnoreCase(request.getPaymentMethod()) && request.getRazorpaySignature() != null) {
            boolean verified = razorpayService.verifyPaymentSignature(
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );
            if (!verified) {
                throw new IllegalArgumentException("Payment signature verification failed. The payment could not be verified.");
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentId(request.getRazorpayPaymentId());
        order.setPaymentStatus("ONLINE".equalsIgnoreCase(request.getPaymentMethod()) ? "COMPLETED" : "PENDING");

        // Copy Shipping Details
        order.setReceiverName(address.getReceiverName());
        order.setStreetAddress(address.getStreetAddress());
        order.setLandmark(address.getLandmark());
        order.setCity(address.getCity());
        order.setState(address.getState());
        order.setPincode(address.getPincode());
        order.setPhoneNumber(address.getPhoneNumber());

        order.setOrderNotes(request.getOrderNotes());
        order.setDeliveryInstructions(request.getDeliveryInstructions());

        // Calculate delivery charge and estimate delivery date using Shipping Service
        ShippingRequest shippingReq = new ShippingRequest();
        shippingReq.setPincode(address.getPincode());
        shippingReq.setCountry(address.getCountry());
        shippingReq.setProductIds(cart.getCartItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList()));

        ShippingResponse shippingRes = shippingService.checkShipping(shippingReq);
        BigDecimal deliveryCharge = shippingRes.getShippingCharge();

        order.setDeliveryCharge(deliveryCharge);
        order.setShippingCharge(deliveryCharge);
        if (shippingRes.getDeliveryDate() != null) {
            order.setDeliveryDate(LocalDate.parse(shippingRes.getDeliveryDate(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay());
        }
        order.setCountry(address.getCountry());

        // Resolve and assign Warehouse Name
        String warehouseName = "PVR Hyderabad Warehouse";
        List<Warehouse> warehouses = warehouseRepository.findAll();
        if (!warehouses.isEmpty()) {
            warehouseName = warehouses.get(0).getName();
        }
        order.setWarehouseName(warehouseName);

        // Assign Courier and Tracking Number
        courierService.assignCourierAndTracking(order);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            ProductVariant variant = item.getVariant();

            BigDecimal priceAtPurchase;
            if (variant != null) {
                if (variant.getStockQuantity() < item.getQuantity()) {
                    throw new com.pvr.primenaturals.exception.InsufficientStockException(
                            "Insufficient stock for variant " + variant.getWeight() + " of product " + product.getName() 
                            + ". Available: " + variant.getStockQuantity());
                }
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
                productVariantRepository.save(variant);
                priceAtPurchase = variant.getPrice();
            } else {
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new com.pvr.primenaturals.exception.InsufficientStockException(
                            "Insufficient stock for product " + product.getName() 
                            + ". Available: " + product.getStockQuantity());
                }
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                priceAtPurchase = product.getPrice();
            }
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setVariant(variant);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPriceAtPurchase(priceAtPurchase);
            order.getOrderItems().add(orderItem);

            BigDecimal itemTotal = priceAtPurchase.multiply(new BigDecimal(item.getQuantity()));
            subtotal = subtotal.add(itemTotal);

            // WebSocket stock sync
            try {
                messagingTemplate.convertAndSend("/topic/products/stock", 
                    new com.pvr.primenaturals.dto.response.StockUpdateDTO(product.getId(), product.getStockQuantity()));
            } catch (Exception e) {
                System.err.println("Failed to send stock update via WebSocket: " + e.getMessage());
            }
        }

        // Apply coupon discount if provided
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            try {
                Map<String, Object> couponResult = couponService.validateCoupon(request.getCouponCode(), subtotal);
                discountAmount = (BigDecimal) couponResult.get("discountAmount");
                appliedCouponCode = (String) couponResult.get("code");
                couponService.markUsed(appliedCouponCode);
            } catch (Exception e) {
                // Invalid coupon - skip silently (frontend already validated)
                System.err.println("Coupon error on order placement: " + e.getMessage());
            }
        }

        // Subtotal + Delivery Charge - Discount = TotalAmount
        BigDecimal total = subtotal.add(deliveryCharge).subtract(discountAmount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
        order.setTotalAmount(total);
        order.setDiscountAmount(discountAmount);
        if (appliedCouponCode != null) order.setCouponCode(appliedCouponCode);

        Order savedOrder = orderRepository.save(order);

        // Create initial OrderTracking log
        try {
            OrderTracking tracking = OrderTracking.builder()
                    .order(savedOrder)
                    .status("PLACED")
                    .description("Your order has been placed successfully.")
                    .location(savedOrder.getWarehouseName() != null ? savedOrder.getWarehouseName() : "Hyderabad Warehouse")
                    .build();
            orderTrackingRepository.save(tracking);
        } catch (Exception e) {
            System.err.println("Failed to save initial OrderTracking log: " + e.getMessage());
        }

        // 3. Clear the cart
        cart.getCartItems().clear();
        cartRepository.save(cart);

        OrderDTO dto = mapToDTO(savedOrder);
        try {
            messagingTemplate.convertAndSend("/topic/orders/admin", dto);
        } catch (Exception e) {
            System.err.println("Failed to send admin order notification via WebSocket: " + e.getMessage());
        }

        emailService.sendOrderConfirmation(dto, email);

        return dto;
    }

    public List<OrderDTO> getUserOrders(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserId(user.getId()).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: You can only delete your own orders.");
        }
        orderRepository.delete(order);
    }

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        Order savedOrder = orderRepository.save(order);
        
        try {
            OrderTracking tracking = OrderTracking.builder()
                    .order(savedOrder)
                    .status(savedOrder.getStatus().name())
                    .description("Order status updated to: " + savedOrder.getStatus().name())
                    .location(savedOrder.getCity() != null ? savedOrder.getCity() : "In Transit")
                    .build();
            orderTrackingRepository.save(tracking);
        } catch (Exception e) {
            System.err.println("Failed to save OrderTracking log: " + e.getMessage());
        }

        OrderDTO dto = mapToDTO(savedOrder);
        // Notify user about status change via WebSockets
        messagingTemplate.convertAndSend("/topic/orders/" + dto.getUserId(), dto);
        
        // Notify user about status change via Email
        emailService.sendOrderStatusUpdate(dto, savedOrder.getUser().getEmail());
        
        return dto;
    }

    @org.springframework.transaction.annotation.Transactional
    public OrderDTO cancelOrder(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized: You can only cancel your own orders.");
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new RuntimeException("Protocol Violation: Only orders in PLACED status can be aborted.");
        }

        // Restore Stock
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                
                if (item.getVariant() != null) {
                    ProductVariant variant = item.getVariant();
                    variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
                    productVariantRepository.save(variant);
                }

                // Broadcast stock restoration
                try {
                    messagingTemplate.convertAndSend("/topic/products/stock", 
                        new com.pvr.primenaturals.dto.response.StockUpdateDTO(product.getId(), product.getStockQuantity()));
                } catch (Exception e) {}
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        try {
            OrderTracking tracking = OrderTracking.builder()
                    .order(savedOrder)
                    .status("CANCELLED")
                    .description("Your order has been cancelled.")
                    .location(savedOrder.getCity() != null ? savedOrder.getCity() : "Customer Location")
                    .build();
            orderTrackingRepository.save(tracking);
        } catch (Exception e) {
            System.err.println("Failed to save cancel OrderTracking log: " + e.getMessage());
        }

        OrderDTO dto = mapToDTO(savedOrder);
        // Notify Admin & User
        messagingTemplate.convertAndSend("/topic/orders/admin", dto);
        messagingTemplate.convertAndSend("/topic/orders/" + dto.getUserId(), dto);
        emailService.sendOrderStatusUpdate(dto, email);

        return dto;
    }

    private OrderDTO mapToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUserName(order.getUser().getName());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());
        dto.setPaymentId(order.getPaymentId());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setCreatedAt(order.getCreatedAt());

        // Shipping Details mapping
        dto.setReceiverName(order.getReceiverName());
        dto.setStreetAddress(order.getStreetAddress());
        dto.setLandmark(order.getLandmark());
        dto.setCity(order.getCity());
        dto.setState(order.getState());
        dto.setPincode(order.getPincode());
        dto.setPhoneNumber(order.getPhoneNumber());

        dto.setOrderNotes(order.getOrderNotes());
        dto.setDeliveryInstructions(order.getDeliveryInstructions());

        dto.setDeliveryCharge(order.getDeliveryCharge());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setCouponCode(order.getCouponCode());

        // Map Shipping Engine Snapshot fields
        dto.setShippingCharge(order.getShippingCharge());
        if (order.getDeliveryDate() != null) {
            dto.setDeliveryDate(order.getDeliveryDate().toLocalDate().toString());
        }
        dto.setCourierName(order.getCourierName());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setWarehouseName(order.getWarehouseName());
        dto.setCountry(order.getCountry());
        dto.setCurrency(order.getCurrency());

        List<OrderItemDTO> items = order.getOrderItems().stream().map(item -> {
            OrderItemDTO iDto = new OrderItemDTO();
            iDto.setId(item.getId());
            if (item.getProduct() != null) {
                iDto.setProductId(item.getProduct().getId());
                iDto.setProductName(item.getProduct().getName());
                iDto.setProductImageUrl(item.getProduct().getImageUrl());
            } else {
                iDto.setProductId(null);
                iDto.setProductName("Deleted Product");
                iDto.setProductImageUrl(null);
            }
            iDto.setQuantity(item.getQuantity());
            iDto.setPriceAtPurchase(item.getPriceAtPurchase());
            iDto.setSubTotal(item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())));
            
            if (item.getVariant() != null) {
                iDto.setVariantId(item.getVariant().getId());
                iDto.setVariantWeight(item.getVariant().getWeight());
            }
            return iDto;
        }).collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }
}
