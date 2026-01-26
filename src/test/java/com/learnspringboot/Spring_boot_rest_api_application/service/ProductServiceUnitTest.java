package com.learnspringboot.Spring_boot_rest_api_application.service;

import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import com.learnspringboot.Spring_boot_rest_api_application.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ProductServiceUnitTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        productService = new ProductService(productRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void getAllProductsDelegatesToRepository() {
        Product p = new Product("A","d",1.0,"c"); p.setId(1L);
        when(productRepository.findAll()).thenReturn(List.of(p));
        List<Product> list = productService.getAllProducts();
        assertThat(list).hasSize(1);
        verify(productRepository).findAll();
    }

    @Test
    void getProductByIdDelegates() {
        Product p = new Product("A","d",1.0,"c"); p.setId(2L);
        when(productRepository.findById(2L)).thenReturn(Optional.of(p));
        Optional<Product> res = productService.getProductById(2L);
        assertThat(res).isPresent();
    }

    @Test
    void searchMethodsDelegateCorrectly() {
        Product p = new Product("A","d",1.0,"smartphones"); p.setId(3L);
        when(productRepository.findProductByCategoryContainingIgnoreCase("smart")).thenReturn(List.of(p));
        when(productRepository.findProductByNameContainingIgnoreCase("a")).thenReturn(List.of(p));
        when(productRepository.findProductByPriceLessThanEqual(10.0)).thenReturn(List.of(p));
        when(productRepository.findProductByPriceGreaterThanEqual(1.0)).thenReturn(List.of(p));

        assertThat(productService.getProductsByCategory("smart")).hasSize(1);
        assertThat(productService.getProductsByName("a")).hasSize(1);
        assertThat(productService.getProductsByPriceLessThanEqual(10.0)).hasSize(1);
        assertThat(productService.getProductsByPriceGreaterThanEqual(1.0)).hasSize(1);
    }

    @Test
    void saveAndDeleteMethodsDelegate() {
        Product p = new Product("A","d",1.0,"c");
        when(productRepository.save(p)).thenReturn(p);
        Product saved = productService.saveProduct(p);
        assertThat(saved).isEqualTo(p);

        when(productRepository.saveAll(List.of(p))).thenReturn(List.of(p));
        List<Product> savedList = productService.saveProducts(List.of(p));
        assertThat(savedList).hasSize(1);

        doNothing().when(productRepository).delete(p);
        productService.deleteProduct(p);
        verify(productRepository).delete(p);
    }
}
