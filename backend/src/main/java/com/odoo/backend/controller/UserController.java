package com.odoo.backend.controller;

import com.odoo.backend.dto.UserResponseDTO;
import com.odoo.backend.dto.common.ApiResponse;
import com.odoo.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================================
 * User Controller
 * ============================================================================
 *
 * Handles:
 * - User Management
 * - User Profile
 * - Enable / Disable Users
 *
 * Base URL:
 * /api/users
 *
 * ============================================================================
 */

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // =========================================================================
    // Get All Users
    // =========================================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Users fetched successfully.",
                        userService.getAllUsers(pageable)
                )
        );
    }

    // =========================================================================
    // Get User By Id
    // =========================================================================

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User fetched successfully.",
                        userService.getUserById(id)
                )
        );
    }

    // =========================================================================
    // Update User
    // =========================================================================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserResponseDTO request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User updated successfully.",
                        userService.updateUser(id, request)
                )
        );
    }

    // =========================================================================
    // Enable User
    // =========================================================================

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> enableUser(
            @PathVariable Long id) {

        userService.enableUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User enabled successfully."
                )
        );
    }

    // =========================================================================
    // Disable User
    // =========================================================================

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> disableUser(
            @PathVariable Long id) {

        userService.disableUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User disabled successfully."
                )
        );
    }

    // =========================================================================
    // Delete User
    // =========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User deleted successfully."
                )
        );
    }

}