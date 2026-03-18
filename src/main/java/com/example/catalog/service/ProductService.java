package com.example.catalog.service;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.entity.Category;
import com.example.catalog.entity.Product;
import com.example.catalog.entity.ProductType;
import com.example.catalog.entity.Tag;
import com.example.catalog.repository.CategoryRepository;
import com.example.catalog.repository.ProductRepository;
import com.example.catalog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Transactional(readOnly = true)
    public ProductDto getById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return toDto(product);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        validateCreateRequest(dto);
        Product product = toEntity(dto);

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            product.setCategories(resolveCategories(dto.getCategoryIds()));
        }

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            product.setTags(resolveTags(dto.getTagIds()));
        }

        product = productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public ProductDto update(UUID id, ProductDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));

        if (dto.getTitle() != null) {
            validateText(dto.getTitle(), "Title must not be blank");
            product.setTitle(dto.getTitle());
        }
        if (dto.getAuthors() != null) {
            product.setAuthors(dto.getAuthors());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getPriceCents() != null) {
            validatePrice(dto.getPriceCents());
            product.setPriceCents(dto.getPriceCents());
        }
        if (dto.getCurrency() != null) {
            validateText(dto.getCurrency(), "Currency must not be blank");
            product.setCurrency(dto.getCurrency());
        }
        if (dto.getType() != null) {
            product.setType(dto.getType());
        }
        if (dto.getMetadata() != null) {
            product.setMetadata(dto.getMetadata());
        }

        if (dto.getCategoryIds() != null) {
            product.setCategories(resolveCategories(dto.getCategoryIds()));
        }

        if (dto.getTagIds() != null) {
            product.setTags(resolveTags(dto.getTagIds()));
        }

        product = productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> search(String query, Pageable pageable) {
        return productRepository.searchByText(query, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByType(ProductType type, Pageable pageable) {
        return productRepository.findByType(type, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByPriceRange(long min, long max, Pageable pageable) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum price must be less than or equal to maximum price");
        }
        return productRepository.findByPriceCentsBetween(min, max, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByCategory(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryName(categoryName, pageable).map(this::toDto);
    }

    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setAuthors(product.getAuthors());
        dto.setDescription(product.getDescription());
        dto.setPriceCents(product.getPriceCents());
        dto.setCurrency(product.getCurrency());
        dto.setType(product.getType());
        dto.setMetadata(product.getMetadata());
        dto.setCreatedAt(product.getCreatedAt());

        if (product.getCategories() != null) {
            dto.setCategoryNames(product.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toSet()));
        }

        if (product.getTags() != null) {
            dto.setTagNames(product.getTags().stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    private Product toEntity(ProductDto dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setAuthors(dto.getAuthors());
        product.setDescription(dto.getDescription());
        product.setPriceCents(dto.getPriceCents());
        product.setCurrency(dto.getCurrency());
        product.setType(dto.getType());
        product.setMetadata(dto.getMetadata());
        return product;
    }

    private void validateCreateRequest(ProductDto dto) {
        validateText(dto.getTitle(), "Title is required");
        validateText(dto.getCurrency(), "Currency is required");
        if (dto.getType() == null) {
            throw new IllegalArgumentException("Type is required");
        }
        if (dto.getPriceCents() == null) {
            throw new IllegalArgumentException("Price is required");
        }
        validatePrice(dto.getPriceCents());
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private void validatePrice(Long priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price must be greater than or equal to 0");
        }
    }

    private Set<Category> resolveCategories(Set<UUID> categoryIds) {
        return categoryIds.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id)))
                .collect(Collectors.toSet());
    }

    private Set<Tag> resolveTags(Set<UUID> tagIds) {
        return tagIds.stream()
                .map(id -> tagRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id)))
                .collect(Collectors.toSet());
    }
}
