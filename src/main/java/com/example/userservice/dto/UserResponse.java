package com.example.userservice.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Instant createdAt;
    private Instant updatedAt;
}
