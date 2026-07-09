package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.ServiceablePincode;
import com.pvr.primenaturals.service.PincodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/pincodes")
public class PincodeController {

    @Autowired
    private PincodeService pincodeService;

    @GetMapping("/check/{pincode}")
    public ResponseEntity<ServiceablePincode> checkPincode(@PathVariable String pincode) {
        return pincodeService.checkPincode(pincode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double latitude, @RequestParam double longitude) {
        return pincodeService.reverseGeocode(latitude, longitude)
                .map(pincode -> {
                    java.util.Map<String, String> response = new java.util.HashMap<>();
                    response.put("pincode", pincode);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    java.util.Map<String, String> errorResponse = new java.util.HashMap<>();
                    errorResponse.put("error", "Could not retrieve pincode for coordinates");
                    return ResponseEntity.badRequest().body(errorResponse);
                });
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ServiceablePincode> getAllPincodes() {
        return pincodeService.getAllPincodes();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceablePincode addOrUpdatePincode(@RequestBody ServiceablePincode serviceablePincode) {
        return pincodeService.addOrUpdatePincode(serviceablePincode);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePincode(@PathVariable Long id) {
        pincodeService.deletePincode(id);
        return ResponseEntity.ok().build();
    }
}
