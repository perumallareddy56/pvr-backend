package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.CartRequest;
import com.pvr.primenaturals.dto.response.CartDTO;
import com.pvr.primenaturals.dto.response.CartItemDTO;
import com.pvr.primenaturals.entity.*;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    public CartDTO getCartForUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
        return mapToDTO(cart);
    }

    public CartDTO addToCart(String email, CartRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = productVariantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("Variant does not belong to specified product");
            }
        }

        // Validate stock
        int requestedQty = request.getQuantity();
        final ProductVariant finalVariant = variant;

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()) &&
                        ((item.getVariant() == null && finalVariant == null) ||
                         (item.getVariant() != null && finalVariant != null && item.getVariant().getId().equals(finalVariant.getId()))))
                .findFirst();

        if (existingItem.isPresent()) {
            requestedQty += existingItem.get().getQuantity();
        }

        if (variant != null) {
            if (variant.getStockQuantity() < requestedQty) {
                throw new IllegalArgumentException("Insufficient stock. Only " + variant.getStockQuantity() + " items available.");
            }
        } else {
            if (product.getStockQuantity() < requestedQty) {
                throw new IllegalArgumentException("Insufficient stock. Only " + product.getStockQuantity() + " items available.");
            }
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(requestedQty);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setVariant(variant);
            newItem.setQuantity(request.getQuantity());
            cart.getCartItems().add(newItem);
        }

        Cart updatedCart = cartRepository.save(cart);
        return mapToDTO(updatedCart);
    }

    public CartDTO removeFromCart(String email, Long cartItemId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getCartItems().removeIf(item -> item.getId().equals(cartItemId));
        Cart updatedCart = cartRepository.save(cart);
        return mapToDTO(updatedCart);
    }

    public CartDTO updateQuantity(String email, Long cartItemId, int newQuantity) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (newQuantity <= 0) {
            return removeFromCart(email, cartItemId);
        }

        CartItem targetItem = null;
        for (CartItem item : cart.getCartItems()) {
            if (item.getId().equals(cartItemId)) {
                targetItem = item;
                break;
            }
        }

        if (targetItem == null) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        // Validate stock before updating
        if (targetItem.getVariant() != null) {
            if (targetItem.getVariant().getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock. Only " + targetItem.getVariant().getStockQuantity() + " items available.");
            }
        } else {
            if (targetItem.getProduct().getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock. Only " + targetItem.getProduct().getStockQuantity() + " items available.");
            }
        }

        targetItem.setQuantity(newQuantity);
        Cart updatedCart = cartRepository.save(cart);
        return mapToDTO(updatedCart);
    }

    public CartDTO mergeCart(String email, List<CartRequest> guestItems) {
        for (CartRequest item : guestItems) {
            try {
                addToCart(email, item);
            } catch (Exception e) {
                // Ignore failures for individual guest items (like out of stock) during merge to ensure progress
            }
        }
        return getCartForUser(email);
    }

    private CartDTO mapToDTO(Cart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartId(cart.getId());
        BigDecimal total = BigDecimal.ZERO;

        List<CartItemDTO> itemDTOs = cart.getCartItems().stream().map(item -> {
            CartItemDTO iDto = new CartItemDTO();
            iDto.setId(item.getId());
            iDto.setProductId(item.getProduct().getId());
            iDto.setProductName(item.getProduct().getName());
            iDto.setProductImageUrl(item.getProduct().getImageUrl());
            iDto.setQuantity(item.getQuantity());

            BigDecimal price;
            if (item.getVariant() != null) {
                iDto.setVariantId(item.getVariant().getId());
                iDto.setVariantWeight(item.getVariant().getWeight());
                price = item.getVariant().getPrice();
            } else {
                iDto.setVariantId(null);
                iDto.setVariantWeight(item.getProduct().getWeight());
                price = item.getProduct().getPrice();
            }

            iDto.setPrice(price);
            BigDecimal sub = price.multiply(new BigDecimal(item.getQuantity()));
            iDto.setSubTotal(sub);
            return iDto;
        }).collect(Collectors.toList());

        for (CartItemDTO i : itemDTOs) {
            total = total.add(i.getSubTotal());
        }

        dto.setItems(itemDTOs);
        dto.setTotal(total);
        return dto;
    }
}
