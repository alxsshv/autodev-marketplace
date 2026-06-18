package com.autodev.auth.entity;

import jakarta.persistence.*;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(schema = "auth", name = "users")
@Builder
public class User {

    public User() {
    }

    public User(Long id, String keycloakUserId, String email, boolean enabled, LocalDateTime createdAt) {
        this.id = id;
        this.keycloakUserId = keycloakUserId;
        this.email = email;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "keycloak_user_id",
            nullable = false, unique = true)
    private String keycloakUserId;

    @Column(name = "email",
            nullable = false, unique = true)
    private String email;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public void setKeycloakUserId(String keycloakUserId) {
        this.keycloakUserId = keycloakUserId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
