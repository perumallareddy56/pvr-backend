package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.OrderItemRepository;
import com.pvr.primenaturals.repository.CartItemRepository;
import com.pvr.primenaturals.repository.WishlistRepository;
import com.pvr.primenaturals.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProductService productService;

    @Test
    public void testGetProductById_Success() {
        Product mockProduct = new Product();
        mockProduct.setId(1L);
        mockProduct.setName("Turmeric");
        mockProduct.setPrice(new BigDecimal("150.00"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProduct));

        Product product = productService.getProductById(1L);

        assertNotNull(product);
        assertEquals("Turmeric", product.getName());
        assertEquals(new BigDecimal("150.00"), product.getPrice());
    }

    @Test
    public void testGetProductById_ThrowsExceptionWhenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    public void testAddProduct_Success() {
        Product newProduct = new Product();
        newProduct.setName("Black Pepper");
        newProduct.setPrice(new BigDecimal("299.00"));

        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Product savedProduct = productService.addProduct(newProduct);

        assertNotNull(savedProduct);
        assertEquals("Black Pepper", savedProduct.getName());
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/products/sync"), eq("REFRESH"));
    }

    @Test
    public void testDeleteProduct_SoftDeletesWhenHasOrders() {
        Product activeProduct = new Product();
        activeProduct.setId(1L);
        activeProduct.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(activeProduct));

        productService.archiveProduct(1L);

        assertFalse(activeProduct.isActive());
        verify(productRepository, times(1)).save(activeProduct);
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testDeleteProduct_HardDeletesWhenNoOrders() {
        productService.permanentlyDeleteProduct(2L);

        verify(productRepository, times(1)).deleteById(2L);
        verify(cartItemRepository, times(1)).deleteByProductId(2L);
        verify(wishlistRepository, times(1)).deleteByProductId(2L);
        verify(reviewRepository, times(1)).deleteByProductId(2L);
    }
}
