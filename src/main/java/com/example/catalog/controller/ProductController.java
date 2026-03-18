package com.example.catalog.controller;

import com.example.catalog.dto.ProductDto;
import com.example.catalog.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/catalog")
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    public Page<ProductDto> getCatalog(@RequestParam Optional<String> query,
                                       @RequestParam Optional<String> type,
                                       @RequestParam Optional<Long> minPrice,
                                       @RequestParam Optional<Long> maxPrice,
                                       @RequestParam Optional<String> category,
                                       Pageable pageable) {
        if (query.isPresent()) {
            return service.search(query.get(), pageable);
        } else if (type.isPresent()) {
            return service.findByType(com.example.catalog.entity.ProductType.valueOf(type.get()), pageable);
        } else if (minPrice.isPresent() && maxPrice.isPresent()) {
            return service.findByPriceRange(minPrice.get(), maxPrice.get(), pageable);
        } else if (category.isPresent()) {
            return service.findByCategory(category.get(), pageable);
        }
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ProductDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    public ProductDto create(@Valid @RequestBody ProductDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ProductDto update(@PathVariable UUID id, @RequestBody ProductDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
