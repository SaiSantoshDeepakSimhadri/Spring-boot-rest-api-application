package com.learnspringboot.Spring_boot_rest_api_application.mapper;
import com.learnspringboot.model.ProductDto; // The Generated DTO
import com.learnspringboot.Spring_boot_rest_api_application.model.Product;   // Your Database Entity
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    // Entity -> DTO (For GET requests)
    ProductDto toDto(Product entity);

    // DTO -> Entity (For POST/PUT requests)
    Product toEntity(ProductDto dto);
}

