package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.entity.Wishlist;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.UserRepository;
import com.pvr.primenaturals.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getWishlist(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(Wishlist::getProduct)
                .collect(Collectors.toList());
    }

    @Transactional
    public void toggleWishlist(String email, Long productId) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        wishlistRepository.findByUserIdAndProductId(user.getId(), productId).ifPresentOrElse(
                item -> wishlistRepository.delete(item),
                () -> {
                    Wishlist newItem = new Wishlist();
                    newItem.setUser(user);
                    newItem.setProduct(product);
                    wishlistRepository.save(newItem);
                }
        );
    }

    @Transactional
    public void syncWishlist(String email, List<Long> productIds) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        for (Long pId : productIds) {
            if (wishlistRepository.findByUserIdAndProductId(user.getId(), pId).isEmpty()) {
                productRepository.findById(pId).ifPresent(prod -> {
                    Wishlist newItem = new Wishlist();
                    newItem.setUser(user);
                    newItem.setProduct(prod);
                    wishlistRepository.save(newItem);
                });
            }
        }
    }
}
