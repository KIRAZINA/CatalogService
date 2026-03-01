package com.example.catalog.dto;

import com.example.catalog.entity.ProductType;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class ProductDto {

    private UUID id;
    private String title;
    private String authors;
    private String description;
    private long priceCents;
    private String currency;
    private ProductType type;
    private String metadata;
    private Instant createdAt;
    private Set<String> categoryNames;
    private Set<String> tagNames;
    
    // For create/update operations
    private Set<UUID> categoryIds;
    private Set<UUID> tagIds;
}
