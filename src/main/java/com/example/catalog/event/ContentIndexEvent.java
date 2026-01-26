package com.example.catalog.event;

import lombok.Data;

import java.util.UUID;

@Data
public class ContentIndexEvent {

    private String action; // "create", "update", "delete"
    private UUID productId;
    private Object data; // Optional full data if needed, but for simplicity, just ID
}