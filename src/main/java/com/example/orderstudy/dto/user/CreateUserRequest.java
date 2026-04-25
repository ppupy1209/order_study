package com.example.orderstudy.dto.user;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank String name) {
}
