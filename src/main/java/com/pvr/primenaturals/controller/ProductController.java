package com.pvr.primenaturals.controller;

import com.pvr.primenaturals.dto.response.ProductResponseDTO;
import com.pvr.primenaturals.dto.response.ProductVariantResponseDTO;
import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private com.pvr.primenaturals.repository.ReviewRepository reviewRepository;

    private ProductResponseDTO mapToResponseDTO(Product p) {
        if (p == null) return null;
        
        List<ProductVariantResponseDTO> variantDTOs = null;
        if (p.getVariants() != null) {
            variantDTOs = p.getVariants().stream().map(v -> ProductVariantResponseDTO.builder()
                    .id(v.getId())
                    .weight(v.getWeight())
                    .price(v.getPrice())
                    .mrp(v.getMrp())
                    .stockQuantity(v.getStockQuantity())
                    .sku(v.getSku())
                    .active(v.isActive())
                    .build()
            ).collect(Collectors.toList());
        }

        List<com.pvr.primenaturals.entity.Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(p.getId());
        double avgRating = 0.0;
        int reviewCount = 0;
        if (reviews != null && !reviews.isEmpty()) {
            reviewCount = reviews.size();
            double sum = reviews.stream().mapToDouble(com.pvr.primenaturals.entity.Review::getRating).sum();
            avgRating = sum / reviewCount;
        }

        return ProductResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl())
                .weight(p.getWeight())
                .process(p.getProcess())
                .subCategoryId(p.getSubCategory() != null ? p.getSubCategory().getId() : null)
                .subCategoryName(p.getSubCategory() != null ? p.getSubCategory().getName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .active(p.isActive())
                .mrp(p.getMrp())
                .ingredients(p.getIngredients())
                .nutritionInfo(p.getNutritionInfo())
                .benefits(p.getBenefits())
                .howToUse(p.getHowToUse())
                .storageInstructions(p.getStorageInstructions())
                .shelfLife(p.getShelfLife())
                .manufacturerDetails(p.getManufacturerDetails())
                .countryOfOrigin(p.getCountryOfOrigin())
                .certifications(p.getCertifications())
                .variants(variantDTOs)
                .rating(avgRating)
                .reviewCount(reviewCount)
                .build();
    }

    @GetMapping
    public List<ProductResponseDTO> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search, type, activeOnly);
        } else if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("All")) {
            products = productService.getProductsByTypeName(type, activeOnly);
        } else {
            products = activeOnly ? productService.getAllActiveProducts() : productService.getAllProductsForAdmin();
        }
        return products.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @GetMapping("/{id:[0-9]+}")
    public ProductResponseDTO getProductById(@PathVariable Long id) {
        return mapToResponseDTO(productService.getProductById(id));
    }

    @Autowired
    private com.pvr.primenaturals.service.AuditLogService auditLogService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO addProduct(@RequestBody Product product, jakarta.servlet.http.HttpServletRequest req) {
        ProductResponseDTO dto = mapToResponseDTO(productService.addProduct(product));
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("ADD_PRODUCT", operator, "Created product: " + product.getName() + " (SKU: " + product.getSku() + ")", req.getRemoteAddr());
        return dto;
    }

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponseDTO updateProduct(@PathVariable Long id, @RequestBody Product product, jakarta.servlet.http.HttpServletRequest req) {
        ProductResponseDTO dto = mapToResponseDTO(productService.updateProduct(id, product));
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("UPDATE_PRODUCT", operator, "Updated product ID " + id + ": " + product.getName(), req.getRemoteAddr());
        return dto;
    }

    /**
     * ARCHIVE a product — sets active=false. Always a soft delete.
     * The product remains in the database and can be restored.
     */
    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> archiveProduct(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest req) {
        productService.archiveProduct(id);
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("ARCHIVE_PRODUCT", operator, "Archived product ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    /**
     * PERMANENTLY DELETE a product — removes from DB. Cannot be undone.
     */
    @DeleteMapping("/{id:[0-9]+}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> permanentlyDeleteProduct(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest req) {
        productService.permanentlyDeleteProduct(id);
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("DELETE_PRODUCT", operator, "Permanently deleted product ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id:[0-9]+}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> restoreProduct(@PathVariable Long id, jakarta.servlet.http.HttpServletRequest req) {
        productService.restoreProduct(id);
        String operator = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("RESTORE_PRODUCT", operator, "Restored product ID " + id, req.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    // Category Management Aliases
    @GetMapping("/types")
    public List<com.pvr.primenaturals.entity.ProductType> getAllProductTypes() {
        return productService.getAllProductTypes();
    }

    @PostMapping("/types")
    @PreAuthorize("hasRole('ADMIN')")
    public com.pvr.primenaturals.entity.ProductType addProductType(@RequestBody com.pvr.primenaturals.entity.ProductType type) {
        return productService.addProductType(type);
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProductType(@PathVariable Long id) {
        productService.deleteProductType(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/subcategories")
    public List<com.pvr.primenaturals.entity.ProductSubCategory> getAllSubCategories() {
        return productService.getAllSubCategories();
    }

    @PostMapping("/subcategories")
    @PreAuthorize("hasRole('ADMIN')")
    public com.pvr.primenaturals.entity.ProductSubCategory addSubCategory(@RequestBody com.pvr.primenaturals.entity.ProductSubCategory subCategory) {
        return productService.addSubCategory(subCategory);
    }

    @DeleteMapping("/subcategories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSubCategory(@PathVariable Long id) {
        productService.deleteProductSubCategory(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/types/{typeId}/subcategories")
    public List<com.pvr.primenaturals.entity.ProductSubCategory> getSubCategories(@PathVariable Long typeId) {
        return productService.getSubCategoriesByType(typeId);
    }

    private ProductVariantResponseDTO mapToVariantResponseDTO(com.pvr.primenaturals.entity.ProductVariant v) {
        if (v == null) return null;
        return ProductVariantResponseDTO.builder()
                .id(v.getId())
                .weight(v.getWeight())
                .price(v.getPrice())
                .mrp(v.getMrp())
                .stockQuantity(v.getStockQuantity())
                .sku(v.getSku())
                .active(v.isActive())
                .build();
    }

    @GetMapping("/{id}/variants")
    public List<ProductVariantResponseDTO> getProductVariants(@PathVariable Long id) {
        return productService.getVariantsByProductId(id).stream()
                .map(this::mapToVariantResponseDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponseDTO addProductVariant(@PathVariable Long id, @RequestBody com.pvr.primenaturals.entity.ProductVariant variant) {
        return mapToVariantResponseDTO(productService.addVariant(id, variant));
    }

    @PutMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductVariantResponseDTO updateProductVariant(@PathVariable Long variantId, @RequestBody com.pvr.primenaturals.entity.ProductVariant variant) {
        return mapToVariantResponseDTO(productService.updateVariant(variantId, variant));
    }

    @DeleteMapping("/variants/{variantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProductVariant(@PathVariable Long variantId) {
        productService.deleteVariant(variantId);
        return ResponseEntity.ok().build();
    }
}
