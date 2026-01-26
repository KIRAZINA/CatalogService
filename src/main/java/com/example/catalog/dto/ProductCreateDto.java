package com.example.catalog.dto;

import com.example.catalog.entity.ProductType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode // Базовий для DTO
public class ProductCreateDto {

    private String title;
    private String authors; // JSON string
    private String description;
    private long priceCents;
    private String currency;
    private ProductType type;
    private String metadata; // JSON string
    private Set<UUID> categoryIds = new java.util.HashSet<>();
    private Set<UUID> tagIds = new java.util.HashSet<>();
}