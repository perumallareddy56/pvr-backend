package com.pvr.primenaturals.service;

import com.pvr.primenaturals.dto.response.ReviewDTO;
import com.pvr.primenaturals.entity.Review;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.OrderRepository;
import com.pvr.primenaturals.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private OrderRepository orderRepository;

    public List<ReviewDTO> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<ReviewDTO> getMyReviews(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToDTO).collect(Collectors.toList());
    }

    public ReviewDTO addReview(Review review) {
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }
        boolean verified = orderRepository.hasPurchasedProduct(review.getUser().getId(), review.getProduct().getId());
        review.setVerifiedPurchase(verified);
        return mapToDTO(reviewRepository.save(review));
    }

    @Transactional
    public ReviewDTO editReview(Long reviewId, Long userId, int rating, String comment) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or not yours"));
        review.setRating(rating);
        review.setComment(comment);
        return mapToDTO(reviewRepository.save(review));
    }

    @Transactional
    public void deleteOwnReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or not yours"));
        reviewRepository.delete(review);
    }

    @Transactional
    public ReviewDTO markHelpful(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));
        review.setHelpfulVotes(review.getHelpfulVotes() + 1);
        return mapToDTO(reviewRepository.save(review));
    }

    /** Admin: get all reviews for moderation */
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUserName(review.getUser().getName());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setVerifiedPurchase(review.isVerifiedPurchase());
        dto.setHelpfulVotes(review.getHelpfulVotes());
        return dto;
    }
}

