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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Fiction");
        sampleProduct.setCategories(Set.of(category));

        Tag tag = new Tag();
        tag.setId(UUID.randomUUID());
        tag.setName("Bestseller");
        sampleProduct.setTags(Set.of(tag));

        sampleDto = new ProductDto();
        sampleDto.setTitle("Test Book");
        sampleDto.setDescription("Test Description");
        sampleDto.setAuthors("Test Author");
        sampleDto.setPriceCents(1000L);
        sampleDto.setCurrency("USD");
        sampleDto.setType(ProductType.EBOOK);
        sampleDto.setMetadata("{}");
    }

    @Test
    void create_withValidDto_returnsCreatedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto result = service.create(sampleDto);

        assertNotNull(result);
        assertEquals(sampleProduct.getTitle(), result.getTitle());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void create_withMissingType_throwsException() {
        sampleDto.setType(null);

        assertThrows(IllegalArgumentException.class, () -> service.create(sampleDto));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_withCategories_savesCategories() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Fiction");

        sampleDto.setCategoryIds(Set.of(categoryId));
        sampleProduct.setCategories(Set.of(category));

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto result = service.create(sampleDto);

        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void create_withUnknownCategory_throwsException() {
        UUID categoryId = UUID.randomUUID();
        sampleDto.setCategoryIds(Set.of(categoryId));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.create(sampleDto));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void create_withTags_savesTags() {
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("New");

        sampleDto.setTagIds(Set.of(tagId));
        sampleProduct.setTags(Set.of(tag));

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto result = service.create(sampleDto);

        assertNotNull(result);
        verify(tagRepository).findById(tagId);
    }

    @Test
    void getById_existingId_returnsProduct() {
        UUID id = sampleProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        ProductDto result = service.getById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Book", result.getTitle());
    }

    @Test
    void getById_nonExistingId_throwsException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(id));
    }

    @Test
    void findAll_returnsPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductDto> result = service.findAll(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Book", result.getContent().get(0).getTitle());
    }

    @Test
    void update_existingId_updatesOnlyProvidedFields() {
        UUID id = sampleProduct.getId();
        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("Updated Title");
        updateDto.setPriceCents(2000L);

        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDto result = service.update(id, updateDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals(2000L, result.getPriceCents());
        assertEquals("USD", result.getCurrency());
        assertEquals(ProductType.EBOOK, result.getType());
    }

    @Test
    void update_withBlankTitle_throwsException() {
        UUID id = sampleProduct.getId();
        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("   ");
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        assertThrows(IllegalArgumentException.class, () -> service.update(id, updateDto));
    }

    @Test
    void update_nonExistingId_throwsException() {
        UUID id = UUID.randomUUID();
        ProductDto updateDto = new ProductDto();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update(id, updateDto));
    }

    @Test
    void delete_existingId_deletesProduct() {
        UUID id = sampleProduct.getId();
        when(productRepository.findById(id)).thenReturn(Optional.of(sampleProduct));

        service.delete(id);

        verify(productRepository).delete(sampleProduct);
    }

    @Test
    void delete_nonExistingId_throwsException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.delete(id));
    }

    @Test
    void search_withQuery_returnsMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.searchByText("Test", pageable)).thenReturn(page);

        Page<ProductDto> result = service.search("Test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository).searchByText("Test", pageable);
    }

    @Test
    void findByType_returnsFilteredProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByType(ProductType.EBOOK, pageable)).thenReturn(page);

        Page<ProductDto> result = service.findByType(ProductType.EBOOK, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(ProductType.EBOOK, result.getContent().get(0).getType());
    }

    @Test
    void findByPriceRange_returnsFilteredProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByPriceCentsBetween(500, 2000, pageable)).thenReturn(page);

        Page<ProductDto> result = service.findByPriceRange(500, 2000, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByPriceRange_withInvalidRange_throwsException() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(IllegalArgumentException.class, () -> service.findByPriceRange(2000, 500, pageable));
    }

    @Test
    void findByCategory_returnsFilteredProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(sampleProduct), pageable, 1);
        when(productRepository.findByCategoryName("Fiction", pageable)).thenReturn(page);

        Page<ProductDto> result = service.findByCategory("Fiction", pageable);

        assertEquals(1, result.getTotalElements());
    }
}
