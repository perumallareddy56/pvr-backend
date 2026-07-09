package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.Warehouse;
import com.pvr.primenaturals.repository.WarehouseRepository;
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
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody Warehouse warehouse, HttpServletRequest req) {
        Warehouse saved = warehouseRepository.save(warehouse);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_WAREHOUSE", operator, "Created warehouse: " + saved.getName(), req.getRemoteAddr());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Warehouse> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouse, HttpServletRequest req) {
        Warehouse existing = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
        existing.setName(warehouse.getName());
        existing.setLocationCode(warehouse.getLocationCode());
        existing.setAddress(warehouse.getAddress());
        existing.setCity(warehouse.getCity());
        existing.setState(warehouse.getState());
        existing.setCountry(warehouse.getCountry());
        existing.setLatitude(warehouse.getLatitude());
        existing.setLongitude(warehouse.getLongitude());
        existing.setActive(warehouse.isActive());
        Warehouse updated = warehouseRepository.save(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_WAREHOUSE", operator, "Updated warehouse ID " + id + ": " + updated.getName(), req.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteWarehouse(@PathVariable Long id, HttpServletRequest req) {
        warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
        warehouseRepository.deleteById(id);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_WAREHOUSE", operator, "Deleted warehouse ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok("Warehouse deleted");
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Warehouse> toggleWarehouse(@PathVariable Long id, HttpServletRequest req) {
        Warehouse wh = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found: " + id));
        wh.setActive(!wh.isActive());
        Warehouse saved = warehouseRepository.save(wh);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("TOGGLE_WAREHOUSE", operator,
                "Warehouse " + wh.getName() + " set to " + (saved.isActive() ? "ACTIVE" : "INACTIVE"), req.getRemoteAddr());
        return ResponseEntity.ok(saved);
    }
}
