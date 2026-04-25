package com.example.orderstudy.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
        @NotBlank String name,
        @Min(0) long price,
        @Min(0) long stockQuantity
) {
}
