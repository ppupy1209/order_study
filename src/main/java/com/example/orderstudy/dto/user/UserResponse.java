package com.example.orderstudy.dto.user;

import com.example.orderstudy.domain.user.User;

public record UserResponse(Long id, String name) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName());
    }
}
