package com.example.catalog.service;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.entity.Category;
import com.example.catalog.entity.Product;
import com.example.catalog.entity.ProductType;
import com.example.catalog.entity.Tag;
import com.example.catalog.repository.CategoryRepository;
import com.example.catalog.repository.ProductRepository;
import com.example.catalog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ProductService service;

    private Product sampleProduct;
    private ProductDto sampleDto;

    @BeforeEach
    void setUp() {
        // Create sample product
        sampleProduct = new Product();
        sampleProduct.setId(UUID.randomUUID());
        sampleProduct.setTitle("Test Book");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setAuthors("Test Author");
        sampleProduct.setPriceCents(1000);
        sampleProduct.setCurrency("USD");
        sampleProduct.setType(ProductType.EBOOK);
        sampleProduct.setMetadata("{}");
        sampleProduct.setCreatedAt(Instant.now());

        // Create categories and tags
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Fiction");
        sampleProduct.setCategories(Set.of(category));

        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName("Bestseller");
        sampleProduct.setTags(Set.of(tag));

        // Create sample DTO
        sampleDto = new ProductDto();
        sampleDto.setTitle("Test Book");
        sampleDto.setDescription("Test Description");
        sampleDto.setAuthors("Test Author");
        sampleDto.setPriceCents(1000);
        sampleDto.setCurrency("USD");
        sampleDto.setType(ProductType.EBOOK);
        sampleDto.setMetadata("{}");
    }

    // ==================== CREATE TESTS ====================

    @Test
    void create_withValidDto_returnsCreatedProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.create(sampleDto);

        // Then
        assertNotNull(result);
        assertEquals(sampleProduct.getTitle(), result.getTitle());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_withCategories_savesCategories() {
        // Given
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Fiction");

        sampleDto.setCategoryIds(Set.of(categoryId));
        sampleProduct.setCategories(Set.of(category));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.create(sampleDto);

        // Then
        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void create_withTags_savesTags() {
        // Given
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("New");

        sampleDto.setTagIds(Set.of(tagId));
        sampleProduct.setTags(Set.of(tag));

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.create(sampleDto);

        // Then
        assertNotNull(result);
        verify(tagRepository).findById(tagId);
    }

    // ==================== READ TESTS ====================

    @Test
    void getById_existingId_returnsProduct() {
        // Given
        UUID id = sampleProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        // When
        ProductDto result = service.getById(id);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void getById_nonExistingId_throwsException() {
        // Given
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> service.getById(id));
    }

    @Test
    void findAll_returnsPageOfProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<ProductDto> result = service.findAll(pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Book", result.getContent().get(0).getTitle());
    }

    // ==================== UPDATE TESTS ====================

    @Test
    void update_existingId_updatesProduct() {
        // Given
        UUID id = sampleProduct.getId();
        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("Updated Title");
        updateDto.setPriceCents(2000);
        updateDto.setType(ProductType.AUDIOBOOK);

        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.update(id, updateDto);

        // Then
        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_nonExistingId_throwsException() {
        // Given
        UUID id = UUID.randomUUID();
        ProductDto updateDto = new ProductDto();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> service.update(id, updateDto));
    }

    // ==================== DELETE TESTS ====================

    @Test
    void delete_existingId_deletesProduct() {
        // Given
        UUID id = sampleProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        // When
        service.delete(id);

        // Then
        verify(productRepository).delete(sampleProduct);
    }

    @Test
    void delete_nonExistingId_throwsException() {
        // Given
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> service.delete(id));
    }

    // ==================== SEARCH TESTS ====================

    @Test
    void search_withQuery_returnsMatchingProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.searchByText("Test", pageable)).thenReturn(page);

        // When
        Page<ProductDto> result = service.search("Test", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        verify(productRepository).searchByText("Test", pageable);
    }

    // ==================== FILTER TESTS ====================

    @Test
    void findByType_returnsFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByType(ProductType.EBOOK, pageable)).thenReturn(page);

        // When
        Page<ProductDto> result = service.findByType(ProductType.EBOOK, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(ProductType.EBOOK, result.getContent().get(0).getType());
    }

    @Test
    void findByPriceRange_returnsFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByPriceCentsBetween(500, 2000, pageable)).thenReturn(page);

        // When
        Page<ProductDto> result = service.findByPriceRange(500, 2000, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByCategory_returnsFilteredProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByCategoryName("Fiction", pageable)).thenReturn(page);

        // When
        Page<ProductDto> result = service.findByCategory("Fiction", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
    }

    // ==================== EDGE CASES ====================

    @Test
    void create_withNullCategories_doesNotFail() {
        // Given
        sampleDto.setCategoryIds(null);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.create(sampleDto);

        // Then
        assertNotNull(result);
        verify(categoryRepository, never()).findById(any());
    }

    @Test
    void create_withNullTags_doesNotFail() {
        // Given
        sampleDto.setTagIds(null);
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.create(sampleDto);

        // Then
        assertNotNull(result);
        verify(tagRepository, never()).findById(any());
    }

    @Test
    void update_withCategories_updatesCategories() {
        // Given
        UUID id = sampleProduct.getId();
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Updated");

        ProductDto updateDto = new ProductDto();
        updateDto.setCategoryIds(Set.of(categoryId));

        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        ProductDto result = service.update(id, updateDto);

        // Then
        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
    }
}
