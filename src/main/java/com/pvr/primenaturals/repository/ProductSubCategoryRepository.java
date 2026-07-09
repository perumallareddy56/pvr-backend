package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.ProductSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSubCategoryRepository extends JpaRepository<ProductSubCategory, Long> {
    Optional<ProductSubCategory> findByName(String name);
    List<ProductSubCategory> findByProductTypeId(Long typeId);
}
