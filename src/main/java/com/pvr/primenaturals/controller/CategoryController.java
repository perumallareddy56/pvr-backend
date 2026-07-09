package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.entity.ProductSubCategory;
import com.pvr.primenaturals.entity.ProductType;
import com.pvr.primenaturals.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private ProductService productService;

    @GetMapping("/types")
    public List<ProductType> getAllProductTypes() {
        return productService.getAllProductTypes();
    }

    @GetMapping("/types/{typeId}/subcategories")
    public List<ProductSubCategory> getSubCategories(@PathVariable Long typeId) {
        return productService.getSubCategoriesByType(typeId);
    }

    @GetMapping("/subcategories")
    public List<ProductSubCategory> getAllSubCategories() {
        return productService.getAllSubCategories();
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductType addProductType(@RequestBody ProductType type) {
        return productService.addProductType(type);
    }

    @PostMapping("/subcategories")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductSubCategory addSubCategory(@RequestBody ProductSubCategory subCategory) {
        return productService.addSubCategory(subCategory);
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProductType(@PathVariable Long id) {
        productService.deleteProductType(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subcategories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubCategory(@PathVariable Long id) {
        productService.deleteProductSubCategory(id);
        return ResponseEntity.ok().build();
    }
}
