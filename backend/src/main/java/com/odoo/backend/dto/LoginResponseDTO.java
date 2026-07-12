package com.odoo.backend.dto;

import lombok.*;

/**
 * Response DTO returned after successful authentication.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    // =========================================================================
    // JWT INFORMATION
    // =========================================================================

    /**
     * JWT Access Token
     */
    private String token;

    /**
     * Usually "Bearer"
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token validity in milliseconds.
     * Example: 86400000 = 24 Hours
     */
    private Long expiresIn;

    // =========================================================================
    // USER INFORMATION
    // =========================================================================

    private UserResponseDTO user;

}