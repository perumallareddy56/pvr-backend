package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.Coupon;
import com.pvr.primenaturals.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private com.pvr.primenaturals.service.AuditLogService auditLogService;

    // Public endpoint – any logged-in user can validate a coupon code
    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        BigDecimal cartTotal = new BigDecimal(body.get("cartTotal"));
        
        com.pvr.primenaturals.security.UserDetailsImpl userDetails = 
                (com.pvr.primenaturals.security.UserDetailsImpl) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        Map<String, Object> result = couponService.validateCoupon(code, cartTotal, userDetails.getId());
        return ResponseEntity.ok(result);
    }

    // Admin-only CRUD
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Coupon> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon createCoupon(@RequestBody Coupon coupon, jakarta.servlet.http.HttpServletRequest req) {
        Coupon created = couponService.createCoupon(coupon);
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_COUPON", operator, "Created coupon code: " + coupon.getCode(), req.getRemoteAddr());
        return created;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Coupon updateCoupon(@PathVariable Long id, @RequestBody Coupon coupon, jakarta.servlet.http.HttpServletRequest req) {
        Coupon updated = couponService.updateCoupon(id, coupon);
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_COUPON", operator, "Updated coupon ID " + id + ": " + coupon.getCode(), req.getRemoteAddr());
        return updated;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest req) {
        couponService.deleteCoupon(id);
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_COUPON", operator, "Deleted coupon ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok("Coupon deleted");
    }
}
