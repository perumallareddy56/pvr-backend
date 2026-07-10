package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.SocialLink;
import com.pvr.primenaturals.repository.SocialLinkRepository;
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
@RequestMapping("/api/social-links")
public class SocialLinkController {

    @Autowired
    private SocialLinkRepository socialLinkRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public List<SocialLink> getActiveSocialLinks() {
        return socialLinkRepository.findByActiveTrue();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SocialLink> getAllSocialLinks() {
        return socialLinkRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSocialLink(@RequestBody SocialLink socialLink, HttpServletRequest req) {
        if (socialLinkRepository.findByPlatformIgnoreCase(socialLink.getPlatform()).isPresent()) {
            return ResponseEntity.badRequest().body("Platform " + socialLink.getPlatform() + " already exists.");
        }
        SocialLink saved = socialLinkRepository.save(socialLink);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_SOCIAL_LINK", operator, "Created social link: " + saved.getPlatform(), req.getRemoteAddr());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SocialLink> updateSocialLink(@PathVariable Long id, @RequestBody SocialLink socialLink, HttpServletRequest req) {
        SocialLink existing = socialLinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Social link not found with ID: " + id));
        existing.setUrl(socialLink.getUrl());
        existing.setActive(socialLink.isActive());
        
        SocialLink updated = socialLinkRepository.save(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_SOCIAL_LINK", operator, "Updated social link ID " + id + ": " + updated.getPlatform(), req.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSocialLink(@PathVariable Long id, HttpServletRequest req) {
        SocialLink existing = socialLinkRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Social link not found with ID: " + id));
        socialLinkRepository.delete(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_SOCIAL_LINK", operator, "Deleted social link ID " + id + ": " + existing.getPlatform(), req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}
