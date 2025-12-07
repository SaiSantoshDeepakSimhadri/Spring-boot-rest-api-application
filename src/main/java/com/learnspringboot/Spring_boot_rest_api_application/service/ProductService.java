package com.learnspringboot.Spring_boot_rest_api_application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import com.learnspringboot.Spring_boot_rest_api_application.repository.ProductRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    @Value("${product.backup.file}")
    private String BACKUP_FILE;


    @Autowired
    private ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository) {
        this.productRepository=productRepository;
    }
    //get all the products
    public List<Product> getAllProducts() {
        return productRepository.findAll().stream().toList();
    }

    //get products by their id
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    //get products by category
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findProductByCategoryContainingIgnoreCase(category).stream().toList();
    }

    //get products by price (less than or equal)
    public List<Product> getProductsByPriceLessThanEqual(Double price) {
        return productRepository.findProductByPriceLessThanEqual(price).stream().toList();
    }

    //get products by price (greater than or equal)
    public List<Product> getProductsByPriceGreaterThanEqual(Double price) {
        return productRepository.findProductByPriceGreaterThanEqual(price).stream().toList();
    }

    //get product by name
    public List<Product> getProductsByName(String name) {
        return productRepository.findProductByNameContainingIgnoreCase(name).stream().toList();
    }

    //save the created product
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    //save the created products (one or more)
    public List<Product> saveProducts(List<Product> productList) {
        return productRepository.saveAll(productList).stream().toList();
    }

    //delete a product
    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    @PreDestroy
    public void backupProducts() {
        System.out.println("🔄 Saving products before shutdown...");

        List<Product> products = productRepository.findAll();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(BACKUP_FILE), products);
            System.out.println("✅ Products saved to: " + new File(BACKUP_FILE).getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Failed to save products: " + e.getMessage());
        }
    }

    // Commenting this, as the code has bug that rewrites the IDs of each product in DB on every startup.
//    @PostConstruct
//    public void restoreProducts() {
//        File file = new File(BACKUP_FILE);
//        if (!file.exists()) {
//            System.out.println("ℹ️ No backup file found — skipping restore.");
//            return;
//        }
//
//        System.out.println("♻️ Restoring products from backup...");
//        try {
//            List<Product> products = objectMapper.readValue(
//                    file,
//                    objectMapper.getTypeFactory().constructCollectionType(List.class, Product.class)
//            );
//
//            // Clear existing products to prevent ID conflicts
//            productRepository.deleteAllInBatch();
//
//            // Reset IDs to avoid merge issues (let the DB auto-generate)
//            products.forEach(p -> p.setId(null));
//
//            productRepository.saveAll(products);
//            System.out.println("✅ Products restored successfully from backup!");
//
//        } catch (IOException e) {
//            System.err.println("❌ Failed to restore products: " + e.getMessage());
//        } catch (Exception ex) {
//            System.err.println("⚠️ Unexpected error during restore: " + ex.getMessage());
//        }
//    }

}
