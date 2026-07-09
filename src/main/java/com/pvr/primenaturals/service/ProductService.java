package com.pvr.primenaturals.service;

import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.ProductSubCategory;
import com.pvr.primenaturals.entity.ProductType;
import com.pvr.primenaturals.entity.ProductVariant;
import com.pvr.primenaturals.exception.ResourceNotFoundException;
import com.pvr.primenaturals.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private void broadcastProductSync() {
        messagingTemplate.convertAndSend("/topic/products/sync", "REFRESH");
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    @Autowired
    private ProductSubCategoryRepository productSubCategoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public List<ProductType> getAllProductTypes() {
        return productTypeRepository.findAll();
    }

    public List<ProductSubCategory> getSubCategoriesByType(Long typeId) {
        return productSubCategoryRepository.findByProductTypeId(typeId);
    }

    public List<ProductSubCategory> getAllSubCategories() {
        return productSubCategoryRepository.findAll();
    }

    public ProductType addProductType(ProductType type) {
        return productTypeRepository.save(type);
    }

    public ProductSubCategory addSubCategory(ProductSubCategory subCategory) {
        return productSubCategoryRepository.save(subCategory);
    }

    @Transactional
    public void deleteProductType(Long id) {
        List<ProductSubCategory> subCats = productSubCategoryRepository.findByProductTypeId(id);
        for (ProductSubCategory sc : subCats) {
            deleteProductSubCategory(sc.getId());
        }
        productTypeRepository.deleteById(id);
    }

    @Transactional
    public void deleteProductSubCategory(Long id) {
        List<Product> products = productRepository.findBySubCategoryId(id);
        for (Product p : products) {
            permanentlyDeleteProduct(p.getId());
        }
        productSubCategoryRepository.deleteById(id);
    }

    @Cacheable(value = "products", key = "'active'")
    public List<Product> getAllActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    @Cacheable(value = "products", key = "#query + '_' + #typeName + '_' + #activeOnly")
    public List<Product> searchProducts(String query, String typeName, boolean activeOnly) {
        List<Product> allProducts;
        if (typeName != null && !typeName.trim().isEmpty() && !typeName.equalsIgnoreCase("All")) {
            allProducts = activeOnly ? productRepository.findByActiveTrueAndSubCategoryProductTypeName(typeName)
                                     : productRepository.findBySubCategoryProductTypeName(typeName);
        } else {
            allProducts = activeOnly ? productRepository.findByActiveTrue()
                                     : productRepository.findAll();
        }

        if (query == null || query.trim().isEmpty()) {
            return allProducts;
        }

        String lowerQuery = query.toLowerCase().trim();
        String[] queryTokens = lowerQuery.split("\\s+");

        return allProducts.stream().filter(p -> {
            String lowerName = p.getName().toLowerCase();
            String lowerDesc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";

            if (lowerName.contains(lowerQuery) || lowerDesc.contains(lowerQuery)) {
                return true;
            }

            int matchCount = 0;
            for (String token : queryTokens) {
                if (lowerName.contains(token) || lowerDesc.contains(token)) {
                    matchCount++;
                } else {
                    for (String word : lowerName.split("\\s+")) {
                        // Remove punctuation from comparison words
                        String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
                        if (cleanWord.length() > 2 && getLevenshteinDistance(token, cleanWord) <= 1) {
                            matchCount++;
                            break;
                        }
                    }
                }
            }
            return matchCount >= (int) Math.ceil(queryTokens.length * 0.6);
        }).collect(Collectors.toList());
    }

    private int getLevenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1)) {
                            newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.length()] = lastValue;
            }
        }
        return costs[s2.length()];
    }

    @Cacheable(value = "products", key = "#name + '_' + #activeOnly")
    public List<Product> getProductsByTypeName(String name, boolean activeOnly) {
        return activeOnly ? productRepository.findByActiveTrueAndSubCategoryProductTypeName(name)
                          : productRepository.findBySubCategoryProductTypeName(name);
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product addProduct(Product product) {
        Product p = productRepository.save(product);
        broadcastProductSync();
        return p;
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(Long id, Product newProductData) {
        Product existingProduct = getProductById(id);
        existingProduct.setName(newProductData.getName());
        existingProduct.setDescription(newProductData.getDescription());
        existingProduct.setPrice(newProductData.getPrice());
        existingProduct.setStockQuantity(newProductData.getStockQuantity());
        existingProduct.setImageUrl(newProductData.getImageUrl());
        existingProduct.setWeight(newProductData.getWeight());
        if (newProductData.getSubCategory() != null && newProductData.getSubCategory().getId() != null) {
            ProductSubCategory subCat = productSubCategoryRepository.findById(newProductData.getSubCategory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sub-category not found"));
            existingProduct.setSubCategory(subCat);
        }
        Product p = productRepository.save(existingProduct);
        broadcastProductSync();
        return p;
    }

    /**
     * ARCHIVE a product — always a soft delete.
     * Sets active=false so the product stays in the database and can be viewed/restored.
     * This preserves all order history, reviews, and references.
     */
    @CacheEvict(value = "products", allEntries = true, beforeInvocation = true)
    @Transactional
    public void archiveProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
        broadcastProductSync();
    }

    /**
     * PERMANENTLY DELETE a product — hard delete from database.
     * Cleans up all cart, wishlist, and review references first.
     * Cannot be undone. Should only be called from the explicit "delete" admin action.
     */
    @CacheEvict(value = "products", allEntries = true, beforeInvocation = true)
    @Transactional
    public void permanentlyDeleteProduct(Long id) {
        // Clean up all associated data before removing the product
        cartItemRepository.deleteByProductId(id);
        wishlistRepository.deleteByProductId(id);
        reviewRepository.deleteByProductId(id);
        productRepository.deleteById(id);
        broadcastProductSync();
    }

    @CacheEvict(value = "products", allEntries = true, beforeInvocation = true)
    @Transactional
    public void restoreProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(true);
        productRepository.save(product);
        broadcastProductSync();
    }

    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductVariant addVariant(Long productId, ProductVariant variant) {
        Product product = getProductById(productId);
        variant.setProduct(product);
        ProductVariant saved = productVariantRepository.save(variant);
        broadcastProductSync();
        return saved;
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public ProductVariant updateVariant(Long variantId, ProductVariant variantDetails) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id " + variantId));
        variant.setWeight(variantDetails.getWeight());
        variant.setPrice(variantDetails.getPrice());
        variant.setMrp(variantDetails.getMrp());
        variant.setStockQuantity(variantDetails.getStockQuantity());
        variant.setSku(variantDetails.getSku());
        variant.setActive(variantDetails.isActive());
        ProductVariant updated = productVariantRepository.save(variant);
        broadcastProductSync();
        return updated;
    }

    @CacheEvict(value = "products", allEntries = true)
    @Transactional
    public void deleteVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id " + variantId));
        productVariantRepository.delete(variant);
        broadcastProductSync();
    }
}
