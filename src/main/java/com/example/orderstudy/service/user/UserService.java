package com.example.orderstudy.service.user;

import com.example.orderstudy.domain.user.User;
import com.example.orderstudy.dto.user.CreateUserRequest;
import com.example.orderstudy.dto.user.UserResponse;
import com.example.orderstudy.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        User user = userRepository.save(User.create(request.name()));
        return UserResponse.from(user);
    }
}
