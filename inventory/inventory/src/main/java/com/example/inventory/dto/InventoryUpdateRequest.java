package com.example.inventory.dto;

import lombok.Data;

@Data
public class InventoryUpdateRequest {
    private Long productId;
    private Integer quantityChange; // Positive for increase, negative for decrease
}
