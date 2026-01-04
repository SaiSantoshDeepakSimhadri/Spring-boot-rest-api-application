package com.learnspringboot.Spring_boot_rest_api_application.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnspringboot.Spring_boot_rest_api_application.mapper.ProductMapper;
import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import com.learnspringboot.Spring_boot_rest_api_application.service.ProductService;
import com.learnspringboot.api.ProductApiDelegate;
import com.learnspringboot.model.ProductDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
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
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<ProductDto> getProductById(Long id) {
        return productService.getProductById(id)
                .map(productMapper::toDto)
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByCategory(String categoryName) {
        List<ProductDto> dtos = productService.getProductsByCategory(categoryName)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByMaxPrice(Double maxPrice) {
        List<ProductDto> dtos = productService.getProductsByPriceLessThanEqual(maxPrice)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByMinPrice(Double minPrice) {
        List<ProductDto> dtos = productService.getProductsByPriceGreaterThanEqual(minPrice)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<List<ProductDto>> getProductsByName(String name) {
        List<ProductDto> dtos = productService.getProductsByName(name)
                .stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
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
                return new ResponseEntity<>(result, HttpStatus.CREATED);
            } else {
                ProductDto dto = objectMapper.readValue(trimmed, ProductDto.class);
                Product entity = productMapper.toEntity(dto);
                Product saved = productService.saveProduct(entity);
                ProductDto result = productMapper.toDto(saved);
                return new ResponseEntity<>(result, HttpStatus.CREATED);
            }
        } catch (IOException e) {
            return new ResponseEntity<>("Invalid JSON: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<Object> deleteProduct(Long id) {
        return productService.getProductById(id)
                .map(product -> {
                    productService.deleteProduct(product);
                    return new ResponseEntity<Object>(HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
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
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }
}
