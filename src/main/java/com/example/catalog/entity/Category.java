package com.example.catalog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
}