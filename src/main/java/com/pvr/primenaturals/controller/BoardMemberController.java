package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.BoardMember;
import com.pvr.primenaturals.repository.BoardMemberRepository;
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
@RequestMapping("/api/board-members")
public class BoardMemberController {

    @Autowired
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public List<BoardMember> getActiveBoardMembers() {
        return boardMemberRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BoardMember> getAllBoardMembers() {
        return boardMemberRepository.findAllByOrderByDisplayOrderAsc();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardMember> createBoardMember(@RequestBody BoardMember member, HttpServletRequest req) {
        BoardMember saved = boardMemberRepository.save(member);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_BOARD_MEMBER", operator, "Created Board Member ID " + saved.getId() + ": " + saved.getName(), req.getRemoteAddr());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BoardMember> updateBoardMember(@PathVariable Long id, @RequestBody BoardMember member, HttpServletRequest req) {
        BoardMember existing = boardMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board Member not found with ID: " + id));
        existing.setName(member.getName());
        existing.setRole(member.getRole());
        existing.setInitials(member.getInitials());
        existing.setBio(member.getBio());
        existing.setImageUrl(member.getImageUrl());
        existing.setDisplayOrder(member.getDisplayOrder());
        existing.setActive(member.isActive());
        
        BoardMember updated = boardMemberRepository.save(existing);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_BOARD_MEMBER", operator, "Updated Board Member ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteBoardMember(@PathVariable Long id, HttpServletRequest req) {
        BoardMember member = boardMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board Member not found with ID: " + id));
        boardMemberRepository.delete(member);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_BOARD_MEMBER", operator, "Deleted Board Member ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }
}
