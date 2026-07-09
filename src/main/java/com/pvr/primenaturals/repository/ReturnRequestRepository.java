package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findByUserId(Long userId);
    List<ReturnRequest> findByOrderId(Long orderId);
    List<ReturnRequest> findByStatus(ReturnRequest.ReturnStatus status);
}
