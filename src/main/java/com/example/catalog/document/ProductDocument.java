package com.example.catalog.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String id; // Змінено з UUID на String

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Long)
    private Long priceCents;

    @Field(type = FieldType.Text)
    private String currency;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Long)
    private Long createdAt;

    @Field(type = FieldType.Text)
    private String authors;

    @Field(type = FieldType.Text)
    private String metadata;
}