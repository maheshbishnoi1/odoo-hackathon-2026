package com.odoo.backend.dto;

import com.odoo.backend.entity.User;
import com.odoo.backend.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO representing user details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    // =========================================================================
    // BASIC INFORMATION
    // =========================================================================

    private Long id;

    private String fullName;

    private String email;

    private String phoneNumber;

    // =========================================================================
    // ROLE
    // =========================================================================

    private Role role;

    // =========================================================================
    // STATUS
    // =========================================================================

    private Boolean enabled;

    // =========================================================================
    // AUDIT
    // =========================================================================

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLogin;

    // =========================================================================
    // MAPPER
    // =========================================================================

    public static UserResponseDTO from(User user) {

        if (user == null) {
            return null;
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}