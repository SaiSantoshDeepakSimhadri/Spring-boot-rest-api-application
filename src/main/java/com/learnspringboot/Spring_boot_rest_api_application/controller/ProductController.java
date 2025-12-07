package com.learnspringboot.Spring_boot_rest_api_application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import com.learnspringboot.Spring_boot_rest_api_application.repository.ProductRepository;
import com.learnspringboot.Spring_boot_rest_api_application.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper; // Used to detect JSON type dynamically

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts()
    {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryName));
    }

    @GetMapping("/price/max/{maxPrice}")
    public ResponseEntity<List<Product>> getProductsByMaxPrice(@PathVariable Double maxPrice) {
        return ResponseEntity.ok(productService.getProductsByPriceLessThanEqual(maxPrice));
    }

    @GetMapping("/price/min/{minPrice}")
    public ResponseEntity<List<Product>> getProductsByMinPrice(@PathVariable Double minPrice) {
        return ResponseEntity.ok(productService.getProductsByPriceGreaterThanEqual(minPrice));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> getProductsByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.getProductsByName(name));
    }

    //add new product or products
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody String products)throws IOException {
        products = products.trim();

        if (products.startsWith("[")) {
            // Bulk insert
            List<Product> productList = objectMapper.readValue(products, new TypeReference<List<Product>>() {});
            List<Product> saved = productService.saveProducts(productList);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } else {
            // Single insert
            Product product = objectMapper.readValue(products, Product.class);
            Product saved = productService.saveProduct(product);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        }
    }

    //update a product
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product productToUpdate = productService.getProductById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productToUpdate.setName(product.getName());
        productToUpdate.setDescription(product.getDescription());
        productToUpdate.setPrice(product.getPrice());
        productToUpdate.setCategory(product.getCategory());
        Product updatedProduct = productService.saveProduct(productToUpdate);
        return new ResponseEntity<>(updatedProduct,HttpStatus.OK);
    }

    //Delete a product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Product productToDelete = productService.getProductById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        productService.deleteProduct(productToDelete);
        return ResponseEntity.ok("Product id: "+productToDelete.getId()+", name: "+productToDelete.getName()+", Deleted");
    }
}
