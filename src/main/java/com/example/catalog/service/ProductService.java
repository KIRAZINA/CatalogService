package com.example.catalog.service;

import com.example.catalog.document.ProductDocument;
import com.example.catalog.dto.ProductCreateDto;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.ProductUpdateDto;
import com.example.catalog.entity.Category;
import com.example.catalog.entity.Product;
import com.example.catalog.entity.Tag;
import com.example.catalog.entity.ProductType;
import com.example.catalog.event.ContentIndexEvent;
import com.example.catalog.mapper.ProductMapper;
import com.example.catalog.repository.CategoryRepository;
import com.example.catalog.repository.ProductRepository;
import com.example.catalog.repository.TagRepository;
import com.example.catalog.repository.elasticsearch.ProductElasticsearchRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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

    @Autowired(required = false)
    private ProductElasticsearchRepository elasticsearchRepository;

    @Autowired
    private ProductMapper mapper;

    @Autowired(required = false)
    private KafkaTemplate<String, ContentIndexEvent> kafkaTemplate;

    @Transactional(readOnly = true) // Тепер readOnly розпізнається
    @Cacheable(value = "products", key = "#id")
    public ProductDto getById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        return mapper.toDto(product);
    }

    @Transactional
    public ProductDto create(ProductCreateDto dto) {

        Product product = mapper.toEntity(dto);
        Set<Category> categories = dto.getCategoryIds().stream()
                .map(categoryRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        Set<Tag> tags = dto.getTagIds().stream()
                .map(tagRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        product.setCategories(categories);
        product.setTags(tags);
        product = productRepository.save(product);

        // Index directly for search facade (self-contained), and publish event for other services
        ProductDocument document = mapper.toDocument(product);
        if (elasticsearchRepository != null) {
            elasticsearchRepository.save(document);
        }

        // Publish event
        ContentIndexEvent event = new ContentIndexEvent();
        event.setAction("create");
        event.setProductId(product.getId());
        if (kafkaTemplate != null) {
            kafkaTemplate.send("topic.content.index", event);
        }

        return mapper.toDto(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductDto update(UUID id, ProductUpdateDto dto) {

        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        // Update fields
        product.setTitle(dto.getTitle());
        product.setAuthors(dto.getAuthors());
        product.setDescription(dto.getDescription());
        product.setPriceCents(dto.getPriceCents());
        product.setCurrency(dto.getCurrency());
        product.setType(dto.getType());
        product.setMetadata(dto.getMetadata());
        Set<Category> categories = dto.getCategoryIds().stream()
                .map(categoryRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        Set<Tag> tags = dto.getTagIds().stream()
                .map(tagRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        product.setCategories(categories);
        product.setTags(tags);
        product = productRepository.save(product);

        // Update index
        ProductDocument document = mapper.toDocument(product);
        if (elasticsearchRepository != null) {
            elasticsearchRepository.save(document);
        }

        // Publish event
        ContentIndexEvent event = new ContentIndexEvent();
        event.setAction("update");
        event.setProductId(product.getId());
        if (kafkaTemplate != null) {
            kafkaTemplate.send("topic.content.index", event);
        }

        return mapper.toDto(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void delete(UUID id) {

        Product product = productRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found"));
        productRepository.delete(product);

        // Delete from index
        if (elasticsearchRepository != null) {
            elasticsearchRepository.deleteById(id.toString());
        }

        // Publish event
        ContentIndexEvent event = new ContentIndexEvent();
        event.setAction("delete");
        event.setProductId(id);
        if (kafkaTemplate != null) {
            kafkaTemplate.send("topic.content.index", event);
        }
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> search(String query, Pageable pageable) {
        if (elasticsearchRepository == null) {
            return findAll(pageable);
        }
        // Search facade using Elasticsearch
        Page<ProductDocument> documents = elasticsearchRepository.search(query, pageable);
        // Map to DTOs (fetch full from JPA if needed, but for simplicity, assume document has enough)
        // In production, fetch IDs and load from JPA for consistency
        return documents.map(doc -> {
            return getById(UUID.fromString(doc.getId())); // Uses cache
        });
    }

    // Additional filter methods
    public Page<ProductDto> findByType(ProductType type, Pageable pageable) {
        return productRepository.findByType(type, pageable).map(mapper::toDto);
    }

    public Page<ProductDto> findByPriceRange(long min, long max, Pageable pageable) {
        return productRepository.findByPriceCentsBetween(min, max, pageable).map(mapper::toDto);
    }

    public Page<ProductDto> findByCategory(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryName(categoryName, pageable).map(mapper::toDto);
    }
}