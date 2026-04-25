package com.example.orderstudy.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ProductDtos {
    public record CreateProductRequest(
            @NotBlank String name,
            @Min(0) long price,
            @Min(0) long stockQuantity
    ) {
    }

    public record ProductResponse(Long id, String name, long price, long stockQuantity) {
        static ProductResponse from(Product product) {
            return new ProductResponse(product.getId(), product.getName(), product.getPrice(), product.getStockQuantity());
        }
    }
}
