package com.example.userservice.controller;

import com.example.userservice.domain.User;
import com.example.userservice.dto.PagedResponse;
import com.example.userservice.dto.UserCreateRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.dto.UserUpdateRequest;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.spec.UserSpecifications;
import com.example.userservice.service.UserService;
import com.example.userservice.util.EtagUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    // ✅ SEARCH + FILTERS + PAGINATION + ETAG
    // Example:
    // GET /api/v1/users/search?name=pras&page=0&size=10&sort=createdAt,desc
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<UserResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            Pageable pageable,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    ) {
        // Build spec for accurate ETag (based on IDs + updatedAt/createdAt)
        Specification<User> spec = Specification.where(UserSpecifications.nameContains(name))
                .and(UserSpecifications.emailContains(email))
                .and(UserSpecifications.phoneEquals(phone))
                .and(UserSpecifications.rolesContains(role))
                .and(UserSpecifications.createdAtFrom(createdFrom))
                .and(UserSpecifications.createdAtTo(createdTo));

        Page<User> entityPage = userRepository.findAll(spec, pageable);

        String etag = EtagUtil.forUserSearch(
                name, email, phone, role, createdFrom, createdTo, pageable, entityPage
        );

        if (EtagUtil.matchesIfNoneMatch(ifNoneMatch, etag)) {
            return ResponseEntity.status(304).eTag(etag).build();
        }

        PagedResponse<UserResponse> body = userService.search(
                name, email, phone, role, createdFrom, createdTo, pageable
        );

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.noCache().mustRevalidate())
                .body(body);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserDetails userDetails) {
        return userService.getByEmail(userDetails.getUsername());
    }
}