package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.ServiceablePincode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceablePincodeRepository extends JpaRepository<ServiceablePincode, Long> {
    Optional<ServiceablePincode> findByPincode(String pincode);
    boolean existsByPincodeAndServiceableTrue(String pincode);
}
