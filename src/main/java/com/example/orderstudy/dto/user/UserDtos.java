package com.example.orderstudy.dto.user;

import com.example.orderstudy.domain.user.User;
import jakarta.validation.constraints.NotBlank;

public class UserDtos {
    public record CreateUserRequest(@NotBlank String name) {
    }

    public record UserResponse(Long id, String name) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getName());
        }
    }
}
