package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    long countByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query(
        "SELECT COUNT(o) > 0 FROM Order o JOIN o.orderItems oi WHERE o.user.id = :userId AND oi.product.id = :productId"
    )
    boolean hasPurchasedProduct(@org.springframework.data.repository.query.Param("userId") Long userId,
                                @org.springframework.data.repository.query.Param("productId") Long productId);
}

