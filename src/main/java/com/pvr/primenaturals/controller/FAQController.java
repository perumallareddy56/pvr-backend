package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.FAQ;
import com.pvr.primenaturals.repository.FAQRepository;
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
@RequestMapping("/api/faqs")
public class FAQController {

    @Autowired
    private FAQRepository faqRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public List<FAQ> getActiveFAQs() {
        return faqRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FAQ> getAllFAQs() {
        return faqRepository.findAllByOrderByDisplayOrderAsc();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQ> createFAQ(@RequestBody FAQ faq, HttpServletRequest req) {
        FAQ saved = faqRepository.save(faq);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_FAQ", operator, "Created FAQ ID " + saved.getId() + ": " + saved.getQuestion(), req.getRemoteAddr());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FAQ> updateFAQ(@PathVariable Long id, @RequestBody FAQ faq, HttpServletRequest req) {
        FAQ existing = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with ID: " + id));
        existing.setQuestion(faq.getQuestion());
        existing.setAnswer(faq.getAnswer());
        existing.setDisplayOrder(faq.getDisplayOrder());
        existing.setActive(faq.isActive());
        
        FAQ updated = faqRepository.save(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_FAQ", operator, "Updated FAQ ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteFAQ(@PathVariable Long id, HttpServletRequest req) {
        FAQ faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ not found with ID: " + id));
        faqRepository.delete(faq);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_FAQ", operator, "Deleted FAQ ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}
