package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.Review;
import com.pvr.primenaturals.entity.User;
import com.pvr.primenaturals.repository.UserRepository;
import com.pvr.primenaturals.repository.ReviewRepository;
import com.pvr.primenaturals.dto.response.ReviewDTO;
import com.pvr.primenaturals.security.UserDetailsImpl;
import com.pvr.primenaturals.service.ProductService;
import com.pvr.primenaturals.service.ReviewService;
import com.pvr.primenaturals.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/product/{productId}")
    public List<ReviewDTO> getProductReviews(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    @PostMapping("/product/{productId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> addReview(@PathVariable Long productId, @RequestBody Review reviewRequest) {
        Product product = productService.getProductById(productId);
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        Review review = Review.builder()
                .product(product).user(user)
                .rating(reviewRequest.getRating())
                .comment(reviewRequest.getComment())
                .build();
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    /** User: edit their own review */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewDTO> editReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        int rating = (int) body.get("rating");
        String comment = (String) body.get("comment");
        return ResponseEntity.ok(reviewService.editReview(id, userDetails.getId(), rating, comment));
    }

    /** User: delete their own review */
    @DeleteMapping("/my/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteOwnReview(@PathVariable Long id) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        reviewService.deleteOwnReview(id, userDetails.getId());
        return ResponseEntity.ok("Review deleted");
    }

    /** Admin: delete any review */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeleteReview(@PathVariable Long id, HttpServletRequest req) {
        reviewRepository.deleteById(id);
        String operator = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_REVIEW", operator, "Deleted review with ID: " + id, req.getRemoteAddr());
        return ResponseEntity.ok("Review deleted");
    }

    /** Admin: get all reviews */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReviewDTO> getAllReviews() {
        return reviewService.getAllReviews();
    }

    /** User: get own reviews */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<ReviewDTO> getMyReviews() {
        UserDetailsImpl ud = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return reviewService.getMyReviews(ud.getId());
    }

    /** Any authenticated user: mark a review helpful */
    @PostMapping("/{id}/helpful")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewDTO> markHelpful(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.markHelpful(id));
    }
}
