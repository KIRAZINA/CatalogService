package com.example.catalog.mapper;

import com.example.catalog.document.ProductDocument;
import com.example.catalog.dto.ProductCreateDto;
import com.example.catalog.dto.ProductDto;
import com.example.catalog.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // Для toEntity: Мапимо тільки прості поля, колекції ігноруємо (обробка в сервісі)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "longToInstant")
    Product toEntity(ProductDocument document);

    // Для toDto: Джерело - ентіті Product, витягуємо з її полів
    @Mapping(target = "categoryNames", source = "categories", qualifiedByName = "categoryNames")
    @Mapping(target = "tagNames", source = "tags", qualifiedByName = "tagNames")
    ProductDto toDto(Product entity);

    // Для toDocument: Те саме, source з ентіті
    @Mapping(target = "id", source = "id", qualifiedByName = "uuidToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "instantToLong")
    ProductDocument toDocument(Product entity);

    @Named("categoryNames")
    default Set<String> categoryNames(Set<com.example.catalog.entity.Category> categories) {
        return categories != null ? categories.stream().map(com.example.catalog.entity.Category::getName).collect(Collectors.toSet()) : Set.of();
    }

    @Named("tagNames")
    default Set<String> tagNames(Set<com.example.catalog.entity.Tag> tags) {
        return tags != null ? tags.stream().map(com.example.catalog.entity.Tag::getName).collect(Collectors.toSet()) : Set.of();
    }

    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("instantToLong")
    default Long instantToLong(Instant instant) {
        return instant != null ? instant.toEpochMilli() : null; // Змінено на Long, щоб відповідати ProductDocument
    }

    @Named("longToInstant")
    default Instant longToInstant(Long timestamp) {
        return timestamp != null ? Instant.ofEpochMilli(timestamp) : null;
    }
}