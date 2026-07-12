package com.odoo.backend.controller;

import com.odoo.backend.dto.LoginRequestDTO;
import com.odoo.backend.dto.LoginResponseDTO;
import com.odoo.backend.dto.RegisterRequestDTO;
import com.odoo.backend.dto.UserResponseDTO;
import com.odoo.backend.dto.common.ApiResponse;
import com.odoo.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * Authentication Controller
 * ============================================================================
 *
 * Handles:
 * - User Registration
 * - User Login
 * - Current Logged-in User
 *
 * Base URL:
 * /api/auth
 * ============================================================================
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =========================================================================
    // Register
    // =========================================================================

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO request) {

        UserResponseDTO response = authService.register(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User registered successfully.",
                        response
                )
        );
    }

    // =========================================================================
    // Login
    // =========================================================================

    /**
     * Authenticate user.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        LoginResponseDTO response = authService.login(request.getEmail(), request.getPassword());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful.",
                        response
                )
        );
    }

    // =========================================================================
    // Current User
    // =========================================================================

    /**
     * Returns currently logged-in user.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> currentUser(
            Authentication authentication) {

        UserResponseDTO response =
                authService.getCurrentUser(authentication.getName());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Current user fetched successfully.",
                        response
                )
        );
    }

}