package com.example.catalog.repository;

import com.example.catalog.entity.Product;
import com.example.catalog.entity.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByType(ProductType type, Pageable pageable);

    Page<Product> findByPriceCentsBetween(long min, long max, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.name = :categoryName")
    Page<Product> findByCategoryName(String categoryName, Pageable pageable);

    // Additional complex filters can be added using Specifications if needed
}