package com.example.orderstudy.dto.product;

import com.example.orderstudy.domain.product.Product;

public record ProductResponse(Long id, String name, long price, long stockQuantity) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity());
    }
}
