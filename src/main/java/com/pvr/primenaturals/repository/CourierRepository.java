package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByActiveTrue();
    Optional<Courier> findByCode(String code);
}
