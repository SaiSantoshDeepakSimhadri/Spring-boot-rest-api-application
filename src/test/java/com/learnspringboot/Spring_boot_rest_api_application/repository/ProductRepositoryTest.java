package com.learnspringboot.Spring_boot_rest_api_application.repository;

import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("findProductByCategoryContainingIgnoreCase returns matching products")
    void testFindByCategory() {
        productRepository.deleteAll();
        productRepository.save(new Product("iPhone", "desc", 699.0, "Smartphones"));
        productRepository.save(new Product("Galaxy", "desc", 599.0, "smartphones"));
        List<Product> found = productRepository.findProductByCategoryContainingIgnoreCase("smart");
        assertThat(found).hasSize(2);
    }

}

