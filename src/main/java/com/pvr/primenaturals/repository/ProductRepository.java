package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByActiveTrueAndSubCategoryId(Long subCategoryId);
    List<Product> findByActiveTrueAndNameContainingIgnoreCase(String name);
    List<Product> findByActiveTrueAndSubCategoryProductTypeName(String typeName);
    List<Product> findByActiveTrueAndNameContainingIgnoreCaseAndSubCategoryProductTypeName(String name, String typeName);
    
    // For Admin
    List<Product> findBySubCategoryId(Long subCategoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findBySubCategoryProductTypeName(String typeName);
    List<Product> findByNameContainingIgnoreCaseAndSubCategoryProductTypeName(String name, String typeName);
}
