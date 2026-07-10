package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.CompanyLocation;
import com.pvr.primenaturals.repository.CompanyLocationRepository;
import com.pvr.primenaturals.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/locations")
public class CompanyLocationController {

    @Autowired
    private CompanyLocationRepository companyLocationRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public List<CompanyLocation> getActiveLocations() {
        return companyLocationRepository.findByActiveTrue();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<CompanyLocation> getAllLocations() {
        return companyLocationRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyLocation> createLocation(@RequestBody CompanyLocation location, HttpServletRequest req) {
        CompanyLocation saved = companyLocationRepository.save(location);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_LOCATION", operator, "Created location: " + saved.getName(), req.getRemoteAddr());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CompanyLocation> updateLocation(@PathVariable Long id, @RequestBody CompanyLocation location, HttpServletRequest req) {
        CompanyLocation existing = companyLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found: " + id));
        existing.setName(location.getName());
        existing.setAddress(location.getAddress());
        existing.setPhone(location.getPhone());
        existing.setEmail(location.getEmail());
        existing.setType(location.getType());
        existing.setActive(location.isActive());
        
        CompanyLocation updated = companyLocationRepository.save(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_LOCATION", operator, "Updated location ID " + id + ": " + updated.getName(), req.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteLocation(@PathVariable Long id, HttpServletRequest req) {
        CompanyLocation existing = companyLocationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found: " + id));
        companyLocationRepository.delete(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_LOCATION", operator, "Deleted location ID " + id + ": " + existing.getName(), req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}
