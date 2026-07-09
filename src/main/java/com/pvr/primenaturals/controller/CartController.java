package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.request.CartRequest;
import com.pvr.primenaturals.dto.response.CartDTO;
import com.pvr.primenaturals.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public CartDTO getCart(Authentication authentication) {
        return cartService.getCartForUser(authentication.getName());
    }

    @PostMapping("/add")
    public CartDTO addToCart(Authentication authentication, @RequestBody CartRequest cartRequest) {
        return cartService.addToCart(authentication.getName(), cartRequest);
    }

    @DeleteMapping("/remove/{itemId}")
    public CartDTO removeFromCart(Authentication authentication, @PathVariable Long itemId) {
        return cartService.removeFromCart(authentication.getName(), itemId);
    }

    @PutMapping("/update/{itemId}")
    public CartDTO updateItemQuantity(Authentication authentication, @PathVariable Long itemId, @RequestParam int quantity) {
        return cartService.updateQuantity(authentication.getName(), itemId, quantity);
    }

    @PostMapping("/merge")
    public CartDTO mergeCart(Authentication authentication, @RequestBody List<CartRequest> guestItems) {
        return cartService.mergeCart(authentication.getName(), guestItems);
    }
}
