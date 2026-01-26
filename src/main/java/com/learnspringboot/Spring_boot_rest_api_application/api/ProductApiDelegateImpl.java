package com.learnspringboot.Spring_boot_rest_api_application.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnspringboot.Spring_boot_rest_api_application.mapper.ProductMapper;
import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import com.learnspringboot.Spring_boot_rest_api_application.service.ProductService;
import com.learnspringboot.api.ProductApiDelegate;
import com.learnspringboot.model.ProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductApiDelegateImpl implements ProductApiDelegate {

    private final ProductService productService; // Your existing service
    private final ProductMapper productMapper;   // The MapStruct mapper
    private final ObjectMapper objectMapper;     // for dynamic JSON parsing

    public ProductApiDelegateImpl(ProductService productService, ProductMapper productMapper, ObjectMapper objectMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> dtos = productService.getAllProducts()
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtos);
    }

    @Override
    public ResponseEntity<ProductDto> getProductById(Long id) {
        return productService.getProductById(id)
                .map(productMapper::toDto)
                .map(p -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(p))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByCategory(String categoryName) {
        List<ProductDto> dtos = productService.getProductsByCategory(categoryName)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtos);
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByMaxPrice(Double maxPrice) {
        List<ProductDto> dtos = productService.getProductsByPriceLessThanEqual(maxPrice)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtos);
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByMinPrice(Double minPrice) {
        List<ProductDto> dtos = productService.getProductsByPriceGreaterThanEqual(minPrice)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtos);
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByName(String name) {
        List<ProductDto> dtos = productService.getProductsByName(name)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(dtos);
    }

    @Override
    public ResponseEntity<Object> createProduct(String body) {
        try {
            String trimmed = body == null ? "" : body.trim();
            if (trimmed.startsWith("[")) {
                List<ProductDto> dtoList = objectMapper.readValue(trimmed, new TypeReference<List<ProductDto>>() {});
                List<Product> entities = dtoList.stream()
                        .map(productMapper::toEntity)
                        .collect(Collectors.toList());
                List<Product> saved = productService.saveProducts(entities);
                List<ProductDto> result = saved.stream().map(productMapper::toDto).collect(Collectors.toList());
                return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(result);
            } else {
                ProductDto dto = objectMapper.readValue(trimmed, ProductDto.class);
                Product entity = productMapper.toEntity(dto);
                Product saved = productService.saveProduct(entity);
                ProductDto result = productMapper.toDto(saved);
                return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(result);
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.TEXT_PLAIN).body("Invalid JSON: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Object> deleteProduct(Long id) {
        Optional<Product> opt = productService.getProductById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        productService.deleteProduct(opt.get());
        String msg = "Product with id " + id + " has been deleted";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", msg));
    }

    @Override
    public ResponseEntity<ProductDto> updateProduct(Long id, ProductDto productDto) {
        return productService.getProductById(id)
                .map(existing -> {
                    Product incoming = productMapper.toEntity(productDto);
                    existing.setName(incoming.getName());
                    existing.setDescription(incoming.getDescription());
                    existing.setPrice(incoming.getPrice());
                    existing.setCategory(incoming.getCategory());
                    Product saved = productService.saveProduct(existing);
                    return productMapper.toDto(saved);
                })
                .map(p -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(p))
                .orElse(ResponseEntity.notFound().build());
    }
}
