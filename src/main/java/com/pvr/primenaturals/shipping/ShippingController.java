package com.pvr.primenaturals.shipping;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    private final ShippingService shippingService;

    public ShippingController(ShippingService shippingService) {
        this.shippingService = shippingService;
    }

    @PostMapping("/check")
    public ResponseEntity<ShippingResponse> checkShipping(@RequestBody ShippingRequest request) {
        if (request.getPincode() == null || request.getPincode().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        ShippingResponse response = shippingService.checkShipping(request);
        return ResponseEntity.ok(response);
    }
}
