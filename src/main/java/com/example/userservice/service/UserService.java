package com.example.userservice.service;

import com.example.userservice.dto.PagedResponse;
import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface UserService {
    UserResponse create(UserCreateRequest request);
    UserResponse getById(Long id);
    List<UserResponse> getAll();
    UserResponse update(Long id, UserUpdateRequest request);
    void delete(Long id);
    UserResponse getByEmail(String email);

    PagedResponse<UserResponse> search(
            String name,
            String email,
            String phone,
            String role,
            Instant createdFrom,
            Instant createdTo,
            Pageable pageable
    );
}