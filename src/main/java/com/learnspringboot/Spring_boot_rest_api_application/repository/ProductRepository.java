package com.learnspringboot.Spring_boot_rest_api_application.repository;

import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    //Find product by category
    List<Product> findProductByCategory(String category);

    //Find products that are less than or equal to the mentioned price
    List<Product> findProductByPriceLessThanEqual(Double price);

    //Find products containing name (case-insensitive)
    List<Product> findProductByNameContainingIgnoreCase(String name);
}
