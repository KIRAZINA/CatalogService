package com.example.catalog.integration;

import com.example.catalog.CatalogServiceApplication;
import com.example.catalog.document.ProductDocument;
import com.example.catalog.dto.ProductCreateDto;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.entity.ProductType;
import com.example.catalog.event.ContentIndexEvent;
import com.example.catalog.repository.ProductRepository;
import com.example.catalog.repository.elasticsearch.ProductElasticsearchRepository;
import com.example.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CatalogServiceApplication.class)
@ActiveProfiles("test")
class ProductIntegrationTest {

    @MockBean
    private ProductElasticsearchRepository elasticsearchRepository;

    @MockBean
    private KafkaTemplate<String, ContentIndexEvent> kafkaTemplate;

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    @Test
    void createAndSearch() {
        // Setup mock for search
        when(elasticsearchRepository.search(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        ProductCreateDto dto = new ProductCreateDto();
        dto.setTitle("Test Book");
        dto.setDescription("Test Description");
        dto.setType(ProductType.EBOOK);
        dto.setPriceCents(1000);
        dto.setCurrency("USD");

        ProductDto created = service.create(dto);
        assertNotNull(created.getId());

        // Search
        service.search("Test", Pageable.ofSize(10));
        // We mocked elasticsearch to return empty, so just verifying it runs without error
    }

    @Test
    void createUpdateDelete() {
        // Create
        ProductCreateDto createDto = new ProductCreateDto();
        createDto.setTitle("Original Title");
        createDto.setDescription("Original Desc");
        createDto.setType(ProductType.EBOOK);
        createDto.setPriceCents(2000);
        createDto.setCurrency("EUR");
        
        ProductDto created = service.create(createDto);
        assertNotNull(created.getId());
        
        // Update
        com.example.catalog.dto.ProductUpdateDto updateDto = new com.example.catalog.dto.ProductUpdateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Desc");
        updateDto.setType(ProductType.EBOOK);
        updateDto.setPriceCents(2500);
        updateDto.setCurrency("EUR");
        
        ProductDto updated = service.update(created.getId(), updateDto);
        org.junit.jupiter.api.Assertions.assertEquals("Updated Title", updated.getTitle());
        org.junit.jupiter.api.Assertions.assertEquals(2500, updated.getPriceCents());
        
        // Delete
        service.delete(created.getId());
        
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            service.getById(created.getId());
        });
    }
}
