package com.example.userservice.service.impl;

import com.example.userservice.domain.User;
import com.example.userservice.dto.PagedResponse;
import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.spec.UserSpecifications;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }
        User user = UserMapper.toEntity(request);
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Override
    public UserResponse update(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        UserMapper.updateEntity(user, request);
        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserMapper.toResponse(user);
    }

    // ✅ SEARCH + FILTERS + PAGINATION
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> search(
            String name,
            String email,
            String phone,
            String role,
            Instant createdFrom,
            Instant createdTo,
            Pageable pageable
    ) {
        Specification<User> spec = Specification.where(UserSpecifications.nameContains(name))
                .and(UserSpecifications.emailContains(email))
                .and(UserSpecifications.phoneEquals(phone))
                .and(UserSpecifications.rolesContains(role))
                .and(UserSpecifications.createdAtFrom(createdFrom))
                .and(UserSpecifications.createdAtTo(createdTo));

        Page<User> page = userRepository.findAll(spec, pageable);

        List<UserResponse> content = page.getContent().stream()
                .map(UserMapper::toResponse)
                .toList();

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .sort(pageable.getSort().toString())
                .build();
    }
}