package com.pvr.primenaturals.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.ProductSubCategory;
import com.pvr.primenaturals.entity.ProductType;
import com.pvr.primenaturals.repository.ProductRepository;
import com.pvr.primenaturals.repository.ProductSubCategoryRepository;
import com.pvr.primenaturals.repository.ProductTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class ProductDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductDataInitializer.class);

    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ProductSubCategoryRepository productSubCategoryRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    public ProductDataInitializer(ProductRepository productRepository, 
                          ProductTypeRepository productTypeRepository,
                          ProductSubCategoryRepository productSubCategoryRepository,
                          ResourceLoader resourceLoader,
                          ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.productSubCategoryRepository = productSubCategoryRepository;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        long productCount = productRepository.count();
        if (productCount > 0) {
            log.info("Products already seeded ({} products found). Skipping seed.", productCount);
            return;
        }

        log.info("Database is empty. Seeding with initial product data...");

        InputStream tempStream;
        try {
            tempStream = resourceLoader.getResource("classpath:products_seed.json").getInputStream();
        } catch (Exception e) {
            log.warn("Classpath resource missing (likely IDE build quirk). Falling back to direct filesystem read...");
            java.io.File localFile = new java.io.File("src/main/resources/products_seed.json");
            if (localFile.exists()) {
                tempStream = new java.io.FileInputStream(localFile);
            } else {
                log.error("Failed to find products_seed.json anywhere. Seed aborted.");
                return;
            }
        }

        final InputStream finalInputStream = tempStream;
        try (finalInputStream) {
            List<Map<String, Object>> seedData = objectMapper.readValue(finalInputStream, new TypeReference<List<Map<String, Object>>>() {});
            
            for (Map<String, Object> data : seedData) {
                String categoryName = (String) data.getOrDefault("category", "Indian Spices & Groceries");
                
                // 1. Find or create the ProductType (Main Filter Button in UI)
                ProductType type = productTypeRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            ProductType nt = new ProductType();
                            nt.setName(categoryName);
                            nt.setDescription("Premium " + categoryName + " selection");
                            return productTypeRepository.save(nt);
                        });

               // 2. Find or create the ProductSubCategory
                String subCategoryName = (String) data.getOrDefault("subCategory", categoryName + " Core");
                ProductSubCategory subCategory = productSubCategoryRepository.findByName(subCategoryName)
                        .orElseGet(() -> {
                            ProductSubCategory nsc = new ProductSubCategory();
                            nsc.setName(subCategoryName);
                            nsc.setDescription("Quality selection for " + subCategoryName);
                            nsc.setProductType(type);
                            return productSubCategoryRepository.save(nsc);
                        });

                Product product = new Product();
                product.setName((String) data.get("name"));
                product.setDescription((String) data.get("description"));
                product.setPrice(new java.math.BigDecimal(data.get("price").toString()));
                product.setWeight((String) data.get("weight"));
                product.setImageUrl((String) data.get("imageUrl"));
                @SuppressWarnings("unchecked")
                List<String> process = (List<String>) data.get("process");
                product.setProcess(process);
                product.setStockQuantity(100); 
                product.setSubCategory(subCategory);
                
                productRepository.save(product);
            }
            log.info("Successfully seeded {} products.", seedData.size());
        } catch (Exception e) {
            log.error("Failed to seed products: {}", e.getMessage(), e);
        }
    }
}

