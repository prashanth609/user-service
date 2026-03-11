package com.example.userservice.repository.spec;

import com.example.userservice.domain.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<User> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<User> emailContains(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) return null;
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<User> phoneEquals(String phone) {
        return (root, query, cb) -> {
            if (phone == null || phone.isBlank()) return null;
            return cb.equal(root.get("phone"), phone);
        };
    }

    // role can be "ADMIN"/"USER" or "ROLE_ADMIN"/"ROLE_USER"
    public static Specification<User> rolesContains(String role) {
        return (root, query, cb) -> {
            if (role == null || role.isBlank()) return null;
            String normalized = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
            return cb.like(root.get("roles"), "%" + normalized + "%");
        };
    }

    public static Specification<User> createdAtFrom(Instant from) {
        return (root, query, cb) -> from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<User> createdAtTo(Instant to) {
        return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}