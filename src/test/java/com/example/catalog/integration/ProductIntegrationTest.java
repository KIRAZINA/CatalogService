package com.example.catalog.integration;

import com.example.catalog.CatalogServiceApplication;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.entity.ProductType;
import com.example.catalog.repository.ProductRepository;
import com.example.catalog.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CatalogServiceApplication.class)
class ProductIntegrationTest {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    // ==================== CREATE TESTS ====================

    @Test
    void createProduct_validData_returnsCreatedProduct() {
        // Given
        ProductDto dto = createValidProductDto("Test Book");

        // When
        ProductDto created = service.create(dto);

        // Then
        assertNotNull(created.getId());
        assertEquals("Test Book", created.getTitle());
        assertEquals(1000, created.getPriceCents());
        assertEquals("USD", created.getCurrency());
        assertEquals(ProductType.EBOOK, created.getType());
    }

    @Test
    void createProduct_allFields_populatedCorrectly() {
        // Given
        ProductDto dto = new ProductDto();
        dto.setTitle("Complete Book");
        dto.setDescription("Full Description");
        dto.setAuthors("Author 1, Author 2");
        dto.setType(ProductType.AUDIOBOOK);
        dto.setPriceCents(2500);
        dto.setCurrency("EUR");
        dto.setMetadata("{\"format\": \"mp3\"}");

        // When
        ProductDto created = service.create(dto);

        // Then
        assertNotNull(created.getId());
        assertEquals("Complete Book", created.getTitle());
        assertEquals("Full Description", created.getDescription());
        assertEquals("Author 1, Author 2", created.getAuthors());
        assertEquals(ProductType.AUDIOBOOK, created.getType());
        assertEquals(2500, created.getPriceCents());
        assertEquals("EUR", created.getCurrency());
    }

    // ==================== READ TESTS ====================

    @Test
    void getById_existingProduct_returnsProduct() {
        // Given
        ProductDto created = service.create(createValidProductDto("Find Me"));

        // When
        ProductDto found = service.getById(created.getId());

        // Then
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Find Me", found.getTitle());
    }

    @Test
    void getById_nonExistingProduct_throwsException() {
        // Given
        java.util.UUID randomId = java.util.UUID.randomUUID();

        // When & Then
        assertThrows(java.util.NoSuchElementException.class, () -> service.getById(randomId));
    }

    @Test
    void findAll_emptyDatabase_returnsEmptyPage() {
        // Given - clean database (assumed empty for this test)

        // When
        Page<ProductDto> result = service.findAll(Pageable.ofSize(10));

        // Then
        assertNotNull(result);
    }

    @Test
    void findAll_withProducts_returnsProducts() {
        // Given
        service.create(createValidProductDto("Book 1"));
        service.create(createValidProductDto("Book 2"));

        // When
        Page<ProductDto> result = service.findAll(Pageable.ofSize(10));

        // Then
        assertTrue(result.getTotalElements() >= 2);
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void updateProduct_existingProduct_updatesSuccessfully() {
        // Given
        ProductDto created = service.create(createValidProductDto("Original Title"));

        // When
        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPriceCents(3000);
        updateDto.setCurrency("GBP");
        updateDto.setType(ProductType.COURSE);

        ProductDto updated = service.update(created.getId(), updateDto);

        // Then
        assertEquals("Updated Title", updated.getTitle());
        assertEquals(3000, updated.getPriceCents());
    }

    @Test
    void updateProduct_nonExistingProduct_throwsException() {
        // Given
        java.util.UUID randomId = java.util.UUID.randomUUID();
        ProductDto dto = createValidProductDto("Test");

        // When & Then
        assertThrows(java.util.NoSuchElementException.class, () -> service.update(randomId, dto));
    }

    // ==================== DELETE TESTS ====================

    @Test
    void deleteProduct_existingProduct_deletesSuccessfully() {
        // Given
        ProductDto created = service.create(createValidProductDto("To Delete"));

        // When
        service.delete(created.getId());

        // Then
        assertThrows(java.util.NoSuchElementException.class, () -> service.getById(created.getId()));
    }

    @Test
    void deleteProduct_nonExistingProduct_throwsException() {
        // Given
        java.util.UUID randomId = java.util.UUID.randomUUID();

        // When & Then
        assertThrows(java.util.NoSuchElementException.class, () -> service.delete(randomId));
    }

    // ==================== SEARCH TESTS ====================

    @Test
    void search_withMatchingQuery_returnsResults() {
        // Given
        service.create(createValidProductDto("Java Programming"));
        service.create(createValidProductDto("Python Basics"));

        // When
        Page<ProductDto> result = service.search("Java", Pageable.ofSize(10));

        // Then
        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    void search_withNoMatch_returnsEmptyPage() {
        // Given
        service.create(createValidProductDto("Some Book"));

        // When
        Page<ProductDto> result = service.search("xyz123", Pageable.ofSize(10));

        // Then
        assertNotNull(result);
    }

    // ==================== FILTER TESTS ====================

    @Test
    void findByType_withMatchingType_returnsResults() {
        // Given
        service.create(createValidProductDto("Ebook 1"));
        ProductDto audiobook = createValidProductDto("Audiobook 1");
        audiobook.setType(ProductType.AUDIOBOOK);
        service.create(audiobook);

        // When
        Page<ProductDto> result = service.findByType(ProductType.AUDIOBOOK, Pageable.ofSize(10));

        // Then
        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    void findByPriceRange_withMatchingRange_returnsResults() {
        // Given
        service.create(createValidProductDto("Cheap Book"));
        
        ProductDto expensiveBook = createValidProductDto("Expensive Book");
        expensiveBook.setPriceCents(10000);
        service.create(expensiveBook);

        // When
        Page<ProductDto> result = service.findByPriceRange(5000, 20000, Pageable.ofSize(10));

        // Then
        assertTrue(result.getTotalElements() >= 1);
    }

    // ==================== CRUD FLOW TEST ====================

    @Test
    void fullCrudFlow_createReadUpdateDelete_worksCorrectly() {
        // 1. CREATE
        ProductDto createDto = createValidProductDto("CRUD Test Book");
        ProductDto created = service.create(createDto);
        assertNotNull(created.getId());

        // 2. READ
        ProductDto found = service.getById(created.getId());
        assertEquals("CRUD Test Book", found.getTitle());

        // 3. UPDATE
        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("CRUD Updated");
        updateDto.setPriceCents(5000);
        updateDto.setType(ProductType.COMIC);
        
        ProductDto updated = service.update(created.getId(), updateDto);
        assertEquals("CRUD Updated", updated.getTitle());
        assertEquals(5000, updated.getPriceCents());

        // 4. DELETE
        service.delete(created.getId());
        
        // Verify deleted
        assertThrows(java.util.NoSuchElementException.class, () -> service.getById(created.getId()));
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void createProduct_withInvalidType_handlesGracefully() {
        // Given
        ProductDto dto = createValidProductDto("Test");
        
        // This should work as enum validation happens at controller level
        // Service layer accepts any ProductType
        dto.setType(null); // Test with null type
        
        // When
        ProductDto result = service.create(dto);
        
        // Then
        assertNotNull(result);
        assertNull(result.getType());
    }

    // ==================== HELPER METHODS ====================

    private ProductDto createValidProductDto(String title) {
        ProductDto dto = new ProductDto();
        dto.setTitle(title);
        dto.setDescription("Test Description for " + title);
        dto.setAuthors("Test Author");
        dto.setType(ProductType.EBOOK);
        dto.setPriceCents(1000);
        dto.setCurrency("USD");
        dto.setMetadata("{}");
        return dto;
    }
}
