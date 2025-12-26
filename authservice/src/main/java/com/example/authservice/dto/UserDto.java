package com.example.authservice.dto;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private Set<String> roles;
    private Instant createdAt;

    public UserDto(UUID id, String username, String email, Set<String> roles, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
