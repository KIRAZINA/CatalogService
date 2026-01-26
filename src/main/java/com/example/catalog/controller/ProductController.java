package com.example.catalog.controller;

import com.example.catalog.dto.ProductCreateDto;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.dto.ProductUpdateDto;
import com.example.catalog.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/catalog")
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public Page<ProductDto> getCatalog(@RequestParam Optional<String> query,
                                       @RequestParam Optional<String> type,
                                       @RequestParam Optional<Long> minPrice,
                                       @RequestParam Optional<Long> maxPrice,
                                       @RequestParam Optional<String> category,
                                       Pageable pageable) {
        // Handle filters (example: combine as needed)
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
    @PreAuthorize("hasRole('USER')")
    public ProductDto getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto create(@RequestBody ProductCreateDto dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto update(@PathVariable UUID id, @RequestBody ProductUpdateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}