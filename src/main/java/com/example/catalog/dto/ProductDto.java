package com.example.catalog.dto;

import com.example.catalog.entity.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class ProductDto {

    private UUID id;
    @NotBlank(message = "Title is required")
    private String title;
    private String authors;
    private String description;
    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be greater than or equal to 0")
    private Long priceCents;
    @NotBlank(message = "Currency is required")
    private String currency;
    @NotNull(message = "Type is required")
    private ProductType type;
    private String metadata;
    private Instant createdAt;
    private Set<String> categoryNames;
    private Set<String> tagNames;
    
    // For create/update operations
    private Set<UUID> categoryIds;
    private Set<UUID> tagIds;
}
