package com.example.orderstudy.user;

import jakarta.validation.constraints.NotBlank;

public class UserDtos {
    public record CreateUserRequest(@NotBlank String name) {
    }

    public record UserResponse(Long id, String name) {
        static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName());
        }
    }
}
