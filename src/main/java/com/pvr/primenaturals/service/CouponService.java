package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.Coupon;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.CouponRepository;
import com.pvr.primenaturals.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CouponService {

    @Autowired private CouponRepository couponRepository;
    @Autowired private OrderRepository orderRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    /** Validate coupon for a given user and cart total */
    @Transactional
    public Map<String, Object> validateCoupon(String code, BigDecimal cartTotal, Long userId) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));

        if (!coupon.isActive()) throw new IllegalStateException("This coupon is no longer active.");
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().isBefore(LocalDate.now()))
            throw new IllegalStateException("This coupon has expired.");
        if (coupon.getUsageLimit() != null && coupon.getTimesUsed() >= coupon.getUsageLimit())
            throw new IllegalStateException("This coupon has reached its usage limit.");
        if (coupon.getMinOrderValue() != null && cartTotal.compareTo(coupon.getMinOrderValue()) < 0)
            throw new IllegalStateException("Minimum order value of ₹" + coupon.getMinOrderValue() + " required.");

        // User-specific targeting check
        if (coupon.getSpecificUserId() != null && !coupon.getSpecificUserId().equals(userId))
            throw new IllegalStateException("This coupon is not valid for your account.");

        // First-order check
        if (coupon.isFirstOrderOnly() && userId != null) {
            long orderCount = orderRepository.countByUserId(userId);
            if (orderCount > 0) throw new IllegalStateException("This coupon is only valid for first-time orders.");
        }

        BigDecimal discount;
        String message;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = cartTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0)
                discount = coupon.getMaxDiscountAmount();
            message = "Coupon applied! You save ₹" + discount;
        } else if ("FREE_DELIVERY".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = BigDecimal.ZERO;
            message = "Free delivery applied!";
        } else {
            discount = coupon.getDiscountValue();
            message = "Coupon applied! You save ₹" + discount;
        }
        discount = discount.min(cartTotal);

        return Map.of(
                "code", coupon.getCode(),
                "discountAmount", discount,
                "discountType", coupon.getDiscountType(),
                "message", message
        );
    }

    /** Overload without userId for backward compatibility */
    public Map<String, Object> validateCoupon(String code, BigDecimal cartTotal) {
        return validateCoupon(code, cartTotal, null);
    }

    @Transactional
    public void markUsed(String code) {
        couponRepository.findByCodeIgnoreCase(code).ifPresent(c -> {
            c.setTimesUsed(c.getTimesUsed() + 1);
            couponRepository.save(c);
        });
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        couponRepository.deleteById(id);
    }

    @Transactional
    public Coupon updateCoupon(Long id, Coupon updated) {
        Coupon existing = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        existing.setCode(updated.getCode());
        existing.setDiscountType(updated.getDiscountType());
        existing.setDiscountValue(updated.getDiscountValue());
        existing.setMinOrderValue(updated.getMinOrderValue());
        existing.setMaxDiscountAmount(updated.getMaxDiscountAmount());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setUsageLimit(updated.getUsageLimit());
        existing.setActive(updated.isActive());
        existing.setSpecificUserId(updated.getSpecificUserId());
        existing.setTargetCategory(updated.getTargetCategory());
        existing.setFirstOrderOnly(updated.isFirstOrderOnly());
        return couponRepository.save(existing);
    }
}
