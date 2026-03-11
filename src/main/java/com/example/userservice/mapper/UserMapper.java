package com.example.userservice.mapper;

import com.example.userservice.domain.User;
import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;

public final class UserMapper {
    private UserMapper() {}

    public static User toEntity(UserCreateRequest req) {
        return User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .build();
    }

    public static void updateEntity(User user, UserUpdateRequest req) {
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
    }

    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
