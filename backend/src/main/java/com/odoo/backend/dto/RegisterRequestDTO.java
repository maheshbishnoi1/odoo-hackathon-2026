package com.odoo.backend.dto;

import com.odoo.backend.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Request DTO used during user registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class RegisterRequestDTO {

    // =========================================================================
    // BASIC INFORMATION
    // =========================================================================

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain exactly 10 digits"
    )
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100,
            message = "Password must be between 8 and 100 characters")
    private String password;

    /**
     * Role assigned to the user.
     *
     * Default:
     * DISPATCHER
     *
     * Admin can later change the role if required.
     */
    @Builder.Default
    private Role role = Role.DISPATCHER;
}