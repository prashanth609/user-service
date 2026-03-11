package com.example.userservice.service.impl;


import com.example.userservice.domain.User;
import com.example.userservice.dto.AuthRequest;
import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UserResponse;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;
import com.example.userservice.security.UserPrincipal;
import com.example.userservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User already exists with email: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles("ROLE_USER")
                .build();

        User saved = userRepository.save(user);
        return UserMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        Object principalObj = auth.getPrincipal();

        UserPrincipal up;
        if (principalObj instanceof UserPrincipal) {
            up = (UserPrincipal) principalObj;
        } else {
            User u = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with email: " + request.getEmail()
                    ));
            up = new UserPrincipal(u);
        }

        String roles = up.rolesString();
        String token = jwtService.generateToken(up, roles);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(1800000) // optionally make this configurable via @Value
                .email(up.getUsername())
                .roles(roles)
                .build();
    }
}