package com.example.catalog.repository.elasticsearch;

import com.example.catalog.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"description\", \"authors\"]}}")
    Page<ProductDocument> search(String query, Pageable pageable);
}