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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = CatalogServiceApplication.class)
class ProductIntegrationTest {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    @Test
    void createProduct_validData_returnsCreatedProduct() {
        ProductDto dto = createValidProductDto("Test Book");

        ProductDto created = service.create(dto);

        assertNotNull(created.getId());
        assertEquals("Test Book", created.getTitle());
        assertEquals(1000L, created.getPriceCents());
        assertEquals("USD", created.getCurrency());
        assertEquals(ProductType.EBOOK, created.getType());
    }

    @Test
    void createProduct_allFields_populatedCorrectly() {
        ProductDto dto = new ProductDto();
        dto.setTitle("Complete Book");
        dto.setDescription("Full Description");
        dto.setAuthors("Author 1, Author 2");
        dto.setType(ProductType.AUDIOBOOK);
        dto.setPriceCents(2500L);
        dto.setCurrency("EUR");
        dto.setMetadata("{\"format\": \"mp3\"}");

        ProductDto created = service.create(dto);

        assertNotNull(created.getId());
        assertEquals("Complete Book", created.getTitle());
        assertEquals("Full Description", created.getDescription());
        assertEquals("Author 1, Author 2", created.getAuthors());
        assertEquals(ProductType.AUDIOBOOK, created.getType());
        assertEquals(2500L, created.getPriceCents());
        assertEquals("EUR", created.getCurrency());
    }

    @Test
    void createProduct_missingType_throwsException() {
        ProductDto dto = createValidProductDto("Invalid Book");
        dto.setType(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
    }

    @Test
    void getById_existingProduct_returnsProduct() {
        ProductDto created = service.create(createValidProductDto("Find Me"));

        ProductDto found = service.getById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Find Me", found.getTitle());
    }

    @Test
    void getById_nonExistingProduct_throwsException() {
        UUID randomId = UUID.randomUUID();

        assertThrows(java.util.NoSuchElementException.class, () -> service.getById(randomId));
    }

    @Test
    void findAll_emptyDatabase_returnsPage() {
        Page<ProductDto> result = service.findAll(Pageable.ofSize(10));

        assertNotNull(result);
    }

    @Test
    void findAll_withProducts_returnsProducts() {
        service.create(createValidProductDto("Book 1"));
        service.create(createValidProductDto("Book 2"));

        Page<ProductDto> result = service.findAll(Pageable.ofSize(10));

        assertTrue(result.getTotalElements() >= 2);
    }

    @Test
    void updateProduct_existingProduct_updatesSuccessfully() {
        ProductDto created = service.create(createValidProductDto("Original Title"));

        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPriceCents(3000L);
        updateDto.setCurrency("GBP");
        updateDto.setType(ProductType.COURSE);

        ProductDto updated = service.update(created.getId(), updateDto);

        assertEquals("Updated Title", updated.getTitle());
        assertEquals(3000L, updated.getPriceCents());
        assertEquals("GBP", updated.getCurrency());
    }

    @Test
    void updateProduct_partialUpdate_preservesExistingFields() {
        ProductDto created = service.create(createValidProductDto("Original Title"));

        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("Patched Title");

        ProductDto updated = service.update(created.getId(), updateDto);

        assertEquals("Patched Title", updated.getTitle());
        assertEquals(1000L, updated.getPriceCents());
        assertEquals("USD", updated.getCurrency());
        assertEquals(ProductType.EBOOK, updated.getType());
    }

    @Test
    void updateProduct_nonExistingProduct_throwsException() {
        UUID randomId = UUID.randomUUID();
        ProductDto dto = createValidProductDto("Test");

        assertThrows(java.util.NoSuchElementException.class, () -> service.update(randomId, dto));
    }

    @Test
    void deleteProduct_existingProduct_deletesSuccessfully() {
        ProductDto created = service.create(createValidProductDto("To Delete"));

        service.delete(created.getId());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getById(created.getId()));
    }

    @Test
    void deleteProduct_nonExistingProduct_throwsException() {
        UUID randomId = UUID.randomUUID();

        assertThrows(java.util.NoSuchElementException.class, () -> service.delete(randomId));
    }

    @Test
    void search_withMatchingQuery_returnsResults() {
        service.create(createValidProductDto("Java Programming"));
        service.create(createValidProductDto("Python Basics"));

        Page<ProductDto> result = service.search("Java", Pageable.ofSize(10));

        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    void search_withNoMatch_returnsEmptyPage() {
        service.create(createValidProductDto("Some Book"));

        Page<ProductDto> result = service.search("xyz123", Pageable.ofSize(10));

        assertNotNull(result);
    }

    @Test
    void findByType_withMatchingType_returnsResults() {
        service.create(createValidProductDto("Ebook 1"));
        ProductDto audiobook = createValidProductDto("Audiobook 1");
        audiobook.setType(ProductType.AUDIOBOOK);
        service.create(audiobook);

        Page<ProductDto> result = service.findByType(ProductType.AUDIOBOOK, Pageable.ofSize(10));

        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    void findByPriceRange_withMatchingRange_returnsResults() {
        service.create(createValidProductDto("Cheap Book"));

        ProductDto expensiveBook = createValidProductDto("Expensive Book");
        expensiveBook.setPriceCents(10000L);
        service.create(expensiveBook);

        Page<ProductDto> result = service.findByPriceRange(5000, 20000, Pageable.ofSize(10));

        assertTrue(result.getTotalElements() >= 1);
    }

    @Test
    void fullCrudFlow_createReadUpdateDelete_worksCorrectly() {
        ProductDto createDto = createValidProductDto("CRUD Test Book");
        ProductDto created = service.create(createDto);
        assertNotNull(created.getId());

        ProductDto found = service.getById(created.getId());
        assertEquals("CRUD Test Book", found.getTitle());

        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("CRUD Updated");
        updateDto.setPriceCents(5000L);
        updateDto.setType(ProductType.COMIC);

        ProductDto updated = service.update(created.getId(), updateDto);
        assertEquals("CRUD Updated", updated.getTitle());
        assertEquals(5000L, updated.getPriceCents());

        service.delete(created.getId());

        assertThrows(java.util.NoSuchElementException.class, () -> service.getById(created.getId()));
    }

    private ProductDto createValidProductDto(String title) {
        ProductDto dto = new ProductDto();
        dto.setTitle(title);
        dto.setDescription("Test Description for " + title);
        dto.setAuthors("Test Author");
        dto.setType(ProductType.EBOOK);
        dto.setPriceCents(1000L);
        dto.setCurrency("USD");
        dto.setMetadata("{}");
        return dto;
    }
}
