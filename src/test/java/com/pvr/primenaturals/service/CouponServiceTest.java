package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.Coupon;
import com.pvr.primenaturals.repository.CouponRepository;
import com.pvr.primenaturals.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon percentageCoupon;
    private Coupon fixedCoupon;

    @BeforeEach
    public void setUp() {
        percentageCoupon = new Coupon();
        percentageCoupon.setId(1L);
        percentageCoupon.setCode("SPICE10");
        percentageCoupon.setDiscountType("PERCENTAGE");
        percentageCoupon.setDiscountValue(new BigDecimal("10.00"));
        percentageCoupon.setMinOrderValue(new BigDecimal("500.00"));
        percentageCoupon.setMaxDiscountAmount(new BigDecimal("100.00"));
        percentageCoupon.setExpiryDate(LocalDate.now().plusDays(10));
        percentageCoupon.setUsageLimit(100);
        percentageCoupon.setTimesUsed(0);
        percentageCoupon.setActive(true);

        fixedCoupon = new Coupon();
        fixedCoupon.setId(2L);
        fixedCoupon.setCode("FLAT50");
        fixedCoupon.setDiscountType("FIXED");
        fixedCoupon.setDiscountValue(new BigDecimal("50.00"));
        fixedCoupon.setMinOrderValue(new BigDecimal("300.00"));
        fixedCoupon.setActive(true);
    }

    @Test
    public void testValidateCoupon_Percentage_Success() {
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        Map<String, Object> result = couponService.validateCoupon("SPICE10", new BigDecimal("600.00"));

        assertNotNull(result);
        assertEquals("SPICE10", result.get("code"));
        assertEquals(new BigDecimal("60.00"), result.get("discountAmount")); // 10% of 600
        assertTrue(result.get("message").toString().contains("₹60.00"));
    }

    @Test
    public void testValidateCoupon_Percentage_CapsAtMaxDiscount() {
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        // 10% of 1500 is 150, but max discount is 100
        Map<String, Object> result = couponService.validateCoupon("SPICE10", new BigDecimal("1500.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.get("discountAmount"));
    }

    @Test
    public void testValidateCoupon_Fixed_Success() {
        when(couponRepository.findByCodeIgnoreCase("FLAT50")).thenReturn(Optional.of(fixedCoupon));

        Map<String, Object> result = couponService.validateCoupon("FLAT50", new BigDecimal("400.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.get("discountAmount"));
    }

    @Test
    public void testValidateCoupon_Inactive_ThrowsException() {
        percentageCoupon.setActive(false);
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        assertThrows(IllegalStateException.class, () -> couponService.validateCoupon("SPICE10", new BigDecimal("600.00")));
    }

    @Test
    public void testValidateCoupon_Expired_ThrowsException() {
        percentageCoupon.setExpiryDate(LocalDate.now().minusDays(1));
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        assertThrows(IllegalStateException.class, () -> couponService.validateCoupon("SPICE10", new BigDecimal("600.00")));
    }

    @Test
    public void testValidateCoupon_MinOrderNotMet_ThrowsException() {
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        // Min order is 500, we pass 400
        assertThrows(IllegalStateException.class, () -> couponService.validateCoupon("SPICE10", new BigDecimal("400.00")));
    }

    @Test
    public void testValidateCoupon_SpecificUserMismatch_ThrowsException() {
        percentageCoupon.setSpecificUserId(99L);
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        // Validating with user ID 1L
        assertThrows(IllegalStateException.class, () -> couponService.validateCoupon("SPICE10", new BigDecimal("600.00"), 1L));
    }

    @Test
    public void testValidateCoupon_FirstOrderOnly_Success() {
        percentageCoupon.setFirstOrderOnly(true);
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));
        when(orderRepository.countByUserId(1L)).thenReturn(0L); // First order!

        assertDoesNotThrow(() -> couponService.validateCoupon("SPICE10", new BigDecimal("600.00"), 1L));
    }

    @Test
    public void testValidateCoupon_FirstOrderOnly_ThrowsExceptionWhenNotFirst() {
        percentageCoupon.setFirstOrderOnly(true);
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));
        when(orderRepository.countByUserId(1L)).thenReturn(3L); // Not first order

        assertThrows(IllegalStateException.class, () -> couponService.validateCoupon("SPICE10", new BigDecimal("600.00"), 1L));
    }

    @Test
    public void testMarkUsed_Success() {
        when(couponRepository.findByCodeIgnoreCase("SPICE10")).thenReturn(Optional.of(percentageCoupon));

        couponService.markUsed("SPICE10");

        assertEquals(1, percentageCoupon.getTimesUsed());
        verify(couponRepository, times(1)).save(percentageCoupon);
    }
}
