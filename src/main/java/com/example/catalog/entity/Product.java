package com.example.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data // Lombok генеруватиме getters/setters, які бачитиме MapStruct після processing
@EqualsAndHashCode // Додано для уникнення warning в DTO
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;

    private String authors; // Stored as JSONB, e.g., ["Author1", "Author2"]

    @Column(name = "price_cents")
    private long priceCents;

    private String currency;

    @Enumerated(EnumType.STRING)
    private ProductType type;

    private String metadata; // Additional metadata as JSONB

    @CreationTimestamp
    private Instant createdAt;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "product_tags",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}