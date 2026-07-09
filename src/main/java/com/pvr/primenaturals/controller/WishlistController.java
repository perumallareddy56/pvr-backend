package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<Product> getWishlist(Authentication authentication) {
        return wishlistService.getWishlist(authentication.getName());
    }

    @PostMapping("/toggle/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> toggleWishlist(Authentication authentication, @PathVariable Long productId) {
        wishlistService.toggleWishlist(authentication.getName(), productId);
        return ResponseEntity.ok("Wishlist updated");
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> syncWishlist(Authentication authentication, @RequestBody List<Long> productIds) {
        wishlistService.syncWishlist(authentication.getName(), productIds);
        return ResponseEntity.ok("Wishlist synchronized");
    }
}
