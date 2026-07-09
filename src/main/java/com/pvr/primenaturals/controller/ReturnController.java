package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.request.ReturnRequestDTO;
import com.pvr.primenaturals.entity.ReturnRequest;
import com.pvr.primenaturals.service.ReturnRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    @Autowired
    private ReturnRequestService returnRequestService;

    /** Customer: submit a return request */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> submitReturn(Authentication auth, @RequestBody ReturnRequestDTO dto) {
        return ResponseEntity.ok(returnRequestService.submitRequest(auth.getName(), dto));
    }

    /** Customer: get their own return requests */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<ReturnRequest> getMyReturns(Authentication auth) {
        return returnRequestService.getMyRequests(auth.getName());
    }

    /** Admin: get all return requests */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReturnRequest> getAllReturns() {
        return returnRequestService.getAllRequests();
    }

    /** Admin: approve a return */
    @PatchMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.getOrDefault("adminNote", "") : "";
        return ResponseEntity.ok(returnRequestService.approveRequest(id, note));
    }

    /** Admin: reject a return */
    @PatchMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.getOrDefault("adminNote", "") : "";
        return ResponseEntity.ok(returnRequestService.rejectRequest(id, note));
    }

    /** Admin: initiate refund */
    @PatchMapping("/admin/{id}/refund/initiate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> initiateRefund(@PathVariable Long id) {
        return ResponseEntity.ok(returnRequestService.initiateRefund(id));
    }

    /** Admin: complete refund */
    @PatchMapping("/admin/{id}/refund/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReturnRequest> completeRefund(@PathVariable Long id) {
        return ResponseEntity.ok(returnRequestService.completeRefund(id));
    }
}
