package com.example.order.dto;

import com.example.order.entity.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private Order.OrderStatus status;
}

