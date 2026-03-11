package com.example.userservice.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType; // "Bearer"
    private long expiresInMs;
    private String email;
    private String roles;
}