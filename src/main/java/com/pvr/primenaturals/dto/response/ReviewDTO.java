package com.pvr.primenaturals.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean verifiedPurchase;
    private int helpfulVotes;
}
