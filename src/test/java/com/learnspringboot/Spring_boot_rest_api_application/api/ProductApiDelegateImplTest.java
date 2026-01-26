package com.learnspringboot.Spring_boot_rest_api_application.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnspringboot.Spring_boot_rest_api_application.mapper.ProductMapper;
import com.learnspringboot.Spring_boot_rest_api_application.model.Product;
import com.learnspringboot.Spring_boot_rest_api_application.service.ProductService;
import com.learnspringboot.model.ProductDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ProductApiDelegateImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProductApiDelegateImpl delegate;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        delegate = new ProductApiDelegateImpl(productService, productMapper, objectMapper);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void getAllProductsReturnsMappedDtos() {
        Product p1 = new Product("A", "d1", 10.0, "cat1"); p1.setId(1L);
        Product p2 = new Product("B", "d2", 20.0, "cat2"); p2.setId(2L);
        when(productService.getAllProducts()).thenReturn(List.of(p1, p2));

        ProductDto dto1 = objectMapper.convertValue(p1, ProductDto.class);
        ProductDto dto2 = objectMapper.convertValue(p2, ProductDto.class);
        when(productMapper.toDto(p1)).thenReturn(dto1);
        when(productMapper.toDto(p2)).thenReturn(dto2);

        ResponseEntity<List<ProductDto>> resp = delegate.getAllProducts();
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).hasSize(2);
    }

    @Test
    void getProductByIdNotFound() {
        when(productService.getProductById(5L)).thenReturn(Optional.empty());
        ResponseEntity<ProductDto> resp = delegate.getProductById(5L);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void getProductByIdFound() {
        Product p = new Product("X","d",10.0,"c"); p.setId(3L);
        when(productService.getProductById(3L)).thenReturn(Optional.of(p));
        ProductDto dto = objectMapper.convertValue(p, ProductDto.class);
        when(productMapper.toDto(p)).thenReturn(dto);

        ResponseEntity<ProductDto> resp = delegate.getProductById(3L);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isEqualTo(dto);
    }

    @Test
    void getProductsByCategoryDelegatesToServiceAndMaps() {
        Product p = new Product("X","d",10.0,"Smartphones"); p.setId(4L);
        when(productService.getProductsByCategory("smart")).thenReturn(List.of(p));
        ProductDto dto = objectMapper.convertValue(p, ProductDto.class);
        when(productMapper.toDto(p)).thenReturn(dto);

        ResponseEntity<List<ProductDto>> resp = delegate.getProductsByCategory("smart");
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void getProductsByNameDelegatesToServiceAndMaps() {
        Product p = new Product("N","d",10.0,"c"); p.setId(5L);
        when(productService.getProductsByName("n")).thenReturn(List.of(p));
        ProductDto dto = objectMapper.convertValue(p, ProductDto.class);
        when(productMapper.toDto(p)).thenReturn(dto);

        ResponseEntity<List<ProductDto>> resp = delegate.getProductsByName("n");
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).hasSize(1);
    }

    @Test
    void getProductsByPriceMethods() {
        Product p = new Product("P","d",15.0,"c"); p.setId(6L);
        when(productService.getProductsByPriceLessThanEqual(20.0)).thenReturn(List.of(p));
        when(productService.getProductsByPriceGreaterThanEqual(10.0)).thenReturn(List.of(p));
        ProductDto dto = objectMapper.convertValue(p, ProductDto.class);
        when(productMapper.toDto(p)).thenReturn(dto);

        ResponseEntity<List<ProductDto>> low = delegate.getProductsByMaxPrice(20.0);
        ResponseEntity<List<ProductDto>> high = delegate.getProductsByMinPrice(10.0);
        assertThat(low.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(high.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(low.getBody()).hasSize(1);
        assertThat(high.getBody()).hasSize(1);
    }

    @Test
    void deleteProductNotFound() {
        when(productService.getProductById(99L)).thenReturn(Optional.empty());
        ResponseEntity<Object> resp = delegate.deleteProduct(99L);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void deleteProductFoundReturnsMessage() {
        Product p = new Product("ToDel","d",1.0,"c"); p.setId(8L);
        when(productService.getProductById(8L)).thenReturn(Optional.of(p));
        ResponseEntity<Object> resp = delegate.deleteProduct(8L);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isInstanceOf(java.util.Map.class);
    }

    @Test
    void updateProductNotFound() {
        ProductDto input = objectMapper.convertValue(new Product("A","b",1.0,"c"), ProductDto.class);
        when(productService.getProductById(111L)).thenReturn(Optional.empty());
        ResponseEntity<ProductDto> resp = delegate.updateProduct(111L, input);
        assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void updateProductFoundAppliesChanges() {
        Product existing = new Product("Old","d",5.0,"c");
        existing.setId(10L);
        Product incoming = new Product("New","nd",7.0,"nc");
        ProductDto incomingDto = objectMapper.convertValue(incoming, ProductDto.class);

        when(productService.getProductById(10L)).thenReturn(Optional.of(existing));
        when(productMapper.toEntity(incomingDto)).thenReturn(incoming);
        Product saved = new Product("New","nd",7.0,"nc"); saved.setId(10L);
        when(productService.saveProduct(existing)).thenReturn(saved);
        when(productMapper.toDto(saved)).thenReturn(objectMapper.convertValue(saved, ProductDto.class));

        ResponseEntity<ProductDto> resp = delegate.updateProduct(10L, incomingDto);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
    }

    // New tests for createProduct
    @Test
    void createSingleProductReturnsCreatedDto() throws Exception {
        Product incoming = new Product("NewProd","descr",12.5,"gadgets");
        ProductDto incomingDto = objectMapper.convertValue(incoming, ProductDto.class);

        Product entity = new Product("NewProd","descr",12.5,"gadgets");
        Product saved = new Product("NewProd","descr",12.5,"gadgets"); saved.setId(21L);
        ProductDto savedDto = objectMapper.convertValue(saved, ProductDto.class);

        String body = objectMapper.writeValueAsString(incomingDto);

        when(productMapper.toEntity(incomingDto)).thenReturn(entity);
        when(productService.saveProduct(entity)).thenReturn(saved);
        when(productMapper.toDto(saved)).thenReturn(savedDto);

        ResponseEntity<Object> resp = delegate.createProduct(body);
        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        assertThat(resp.getBody()).isEqualTo(savedDto);
    }

    @Test
    void createMultipleProductsReturnsCreatedDtos() throws Exception {
        Product p1 = new Product("P1","d1",5.0,"c1");
        Product p2 = new Product("P2","d2",6.0,"c2");
        ProductDto dto1 = objectMapper.convertValue(p1, ProductDto.class);
        ProductDto dto2 = objectMapper.convertValue(p2, ProductDto.class);

        Product e1 = new Product("P1","d1",5.0,"c1");
        Product e2 = new Product("P2","d2",6.0,"c2");
        Product s1 = new Product("P1","d1",5.0,"c1"); s1.setId(31L);
        Product s2 = new Product("P2","d2",6.0,"c2"); s2.setId(32L);
        ProductDto sd1 = objectMapper.convertValue(s1, ProductDto.class);
        ProductDto sd2 = objectMapper.convertValue(s2, ProductDto.class);

        String body = objectMapper.writeValueAsString(List.of(dto1, dto2));

        when(productMapper.toEntity(dto1)).thenReturn(e1);
        when(productMapper.toEntity(dto2)).thenReturn(e2);
        when(productService.saveProducts(List.of(e1, e2))).thenReturn(List.of(s1, s2));
        when(productMapper.toDto(s1)).thenReturn(sd1);
        when(productMapper.toDto(s2)).thenReturn(sd2);

        ResponseEntity<Object> resp = delegate.createProduct(body);
        assertThat(resp.getStatusCodeValue()).isEqualTo(201);
        assertThat(resp.getBody()).isInstanceOf(List.class);
        List<?> list = (List<?>) resp.getBody();
        assertThat(list).hasSize(2);
    }
}

