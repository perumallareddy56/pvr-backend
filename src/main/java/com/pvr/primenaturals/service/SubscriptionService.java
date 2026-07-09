package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.request.SubscriptionRequest;
import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.Subscription;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.SubscriptionRepository;
import com.pvr.primenaturals.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    public List<Subscription> getUserSubscriptions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return subscriptionRepository.findByUserId(user.getId());
    }

    @Transactional
    public Subscription createSubscription(String email, SubscriptionRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setProduct(product);
        sub.setQuantity(req.getQuantity() > 0 ? req.getQuantity() : 1);
        sub.setFrequency(req.getFrequency());
        sub.setNextDeliveryDate(req.getStartDate() != null ? req.getStartDate() : LocalDate.now().plusDays(1));
        sub.setVariantWeight(req.getVariantWeight());
        sub.setPricePerDelivery(product.getPrice());
        sub.setActive(true);
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public Subscription pauseResume(String email, Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        if (!sub.getUser().getEmail().equals(email)) throw new RuntimeException("Unauthorized");
        sub.setActive(!sub.isActive());
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void cancel(String email, Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        if (!sub.getUser().getEmail().equals(email)) throw new RuntimeException("Unauthorized");
        subscriptionRepository.delete(sub);
    }

    // Admin: get all active subscriptions
    public List<Subscription> getAllActive() {
        return subscriptionRepository.findByActiveTrue();
    }
}
