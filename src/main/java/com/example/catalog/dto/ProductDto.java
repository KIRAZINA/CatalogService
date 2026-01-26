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
    private String authors; // JSON string
    private String description;
    private long priceCents;
    private String currency;
    private ProductType type;
    private String metadata; // JSON string
    private Instant createdAt;
    private Set<String> categoryNames;
    private Set<String> tagNames;
}