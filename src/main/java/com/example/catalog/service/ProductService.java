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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.NoSuchElementException;
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
        Product product = toEntity(dto);
        
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            Set<Category> categories = dto.getCategoryIds().stream()
                    .map(categoryRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            product.setCategories(categories);
        }
        
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Tag> tags = dto.getTagIds().stream()
                    .map(tagRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            product.setTags(tags);
        }
        
        product = productRepository.save(product);
        return toDto(product);
    }

    @Transactional
    public ProductDto update(UUID id, ProductDto dto) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        
        product.setTitle(dto.getTitle());
        product.setAuthors(dto.getAuthors());
        product.setDescription(dto.getDescription());
        product.setPriceCents(dto.getPriceCents());
        product.setCurrency(dto.getCurrency());
        product.setType(dto.getType());
        product.setMetadata(dto.getMetadata());
        
        if (dto.getCategoryIds() != null) {
            Set<Category> categories = dto.getCategoryIds().stream()
                    .map(categoryRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            product.setCategories(categories);
        }
        
        if (dto.getTagIds() != null) {
            Set<Tag> tags = dto.getTagIds().stream()
                    .map(tagRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            product.setTags(tags);
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
        return productRepository.findByPriceCentsBetween(min, max, pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findByCategory(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryName(categoryName, pageable).map(this::toDto);
    }

    // Manual mapping methods (replacing MapStruct)
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
}
