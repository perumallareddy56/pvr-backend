package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.repository.query.Param;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.user.id = :userId")
    List<Wishlist> findByUserId(@Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.user.id = :userId AND w.product.id = :productId")
    java.util.Optional<Wishlist> findByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByProductId(Long productId);
}
