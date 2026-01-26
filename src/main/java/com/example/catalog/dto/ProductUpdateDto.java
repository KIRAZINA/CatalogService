package com.example.catalog.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) // Виправлення warning, враховує суперкласс ProductCreateDto
public class ProductUpdateDto extends ProductCreateDto {
    // Поля успадковуються, додай специфічні якщо потрібно
}