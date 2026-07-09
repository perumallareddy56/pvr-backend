package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.CartRequest;
import com.pvr.primenaturals.dto.response.CartDTO;
import com.pvr.primenaturals.entity.*;
import com.pvr.primenaturals.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private ProductVariant variant;
    private Cart cart;

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

        variant = ProductVariant.builder()
                .id(25L)
                .product(product)
                .weight("250g")
                .price(new BigDecimal("140.00"))
                .mrp(new BigDecimal("160.00"))
                .stockQuantity(5)
                .active(true)
                .build();

        cart = new Cart();
        cart.setId(30L);
        cart.setUser(user);
        cart.setCartItems(new ArrayList<>());
    }

    @Test
    public void testGetCartForUser_CreatesNewCartWhenNoneExists() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        CartDTO result = cartService.getCartForUser("test@example.com");

        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testAddToCart_ProductWithoutVariant_Success() {
        CartRequest request = new CartRequest();
        request.setProductId(20L);
        request.setQuantity(2);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.addToCart("test@example.com", request);

        assertNotNull(result);
        assertEquals(1, cart.getCartItems().size());
        assertEquals(2, cart.getCartItems().get(0).getQuantity());
        assertEquals(20L, cart.getCartItems().get(0).getProduct().getId());
    }

    @Test
    public void testAddToCart_ProductWithVariant_Success() {
        CartRequest request = new CartRequest();
        request.setProductId(20L);
        request.setVariantId(25L);
        request.setQuantity(3);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findById(25L)).thenReturn(Optional.of(variant));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.addToCart("test@example.com", request);

        assertNotNull(result);
        assertEquals(1, cart.getCartItems().size());
        assertEquals(3, cart.getCartItems().get(0).getQuantity());
        assertEquals(25L, cart.getCartItems().get(0).getVariant().getId());
    }

    @Test
    public void testAddToCart_InsufficientStock_ThrowsException() {
        CartRequest request = new CartRequest();
        request.setProductId(20L);
        request.setQuantity(15); // limit is 10

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart("test@example.com", request));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    public void testRemoveFromCart_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(40L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setCart(cart);
        cart.getCartItems().add(cartItem);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartDTO result = cartService.removeFromCart("test@example.com", 40L);

        assertNotNull(result);
        assertTrue(cart.getCartItems().isEmpty());
    }

    @Test
    public void testMergeCart_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartRequest mergeItem = new CartRequest();
        mergeItem.setProductId(20L);
        mergeItem.setQuantity(2);

        CartDTO result = cartService.mergeCart("test@example.com", Collections.singletonList(mergeItem));

        assertNotNull(result);
        assertEquals(1, cart.getCartItems().size());
        assertEquals(2, cart.getCartItems().get(0).getQuantity());
    }
}
