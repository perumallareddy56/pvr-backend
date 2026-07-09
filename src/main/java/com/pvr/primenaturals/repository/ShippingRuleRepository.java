package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.ShippingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShippingRuleRepository extends JpaRepository<ShippingRule, Long> {
    List<ShippingRule> findByCountryAndActiveTrue(String country);
}
