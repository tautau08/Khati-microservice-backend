package com.example.order.service;

import com.example.order.dto.InventoryUpdateRequest;
import com.example.order.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class InventoryClient {

    private final @Qualifier("inventoryRestClient") RestClient inventoryRestClient;

    public ProductResponse getProductById(Long productId) {
        return inventoryRestClient.get()
                .uri("/api/inventory/products/{id}", productId)
                .retrieve()
                .body(ProductResponse.class);
    }

    public void updateInventory(Long productId, Integer quantityChange) {
        InventoryUpdateRequest request = new InventoryUpdateRequest();
        request.setProductId(productId);
        request.setQuantityChange(quantityChange);

        inventoryRestClient.post()
                .uri("/api/inventory/internal/update")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
