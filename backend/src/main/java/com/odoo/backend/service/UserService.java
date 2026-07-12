package com.odoo.backend.service;

import com.odoo.backend.dto.UserResponseDTO;
import com.odoo.backend.entity.User;
import com.odoo.backend.exception.BadRequestException;
import com.odoo.backend.exception.UserNotFoundException;
import com.odoo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ============================================================================
 * User Service
 * ============================================================================
 *
 * Handles:
 * - User Management
 * - User Profile
 * - Enable / Disable Users
 * - Delete Users
 *
 * ============================================================================
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // =========================================================================
    // Get All Users
    // =========================================================================

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {

        log.info("Fetching all users.");

        return userRepository.findAll(pageable)
                .map(UserResponseDTO::from);
    }

    // =========================================================================
    // Get User By Id
    // =========================================================================

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long userId) {

        return UserResponseDTO.from(findUser(userId));
    }

    // =========================================================================
    // Update User
    // =========================================================================

    public UserResponseDTO updateUser(Long userId,
                                      UserResponseDTO request) {

        User user = findUser(userId);

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        User updatedUser = userRepository.save(user);

        log.info("User updated : {}", updatedUser.getEmail());

        return UserResponseDTO.from(updatedUser);
    }

    // =========================================================================
    // Enable User
    // =========================================================================

    @PreAuthorize("hasRole('ADMIN')")
    public void enableUser(Long userId) {

        User user = findUser(userId);

        user.activate();

        userRepository.save(user);

        log.info("User enabled : {}", user.getEmail());
    }

    // =========================================================================
    // Disable User
    // =========================================================================

    @PreAuthorize("hasRole('ADMIN')")
    public void disableUser(Long userId) {

        User user = findUser(userId);

        user.deactivate();

        userRepository.save(user);

        log.info("User disabled : {}", user.getEmail());
    }

    // =========================================================================
    // Delete User
    // =========================================================================

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {

        User user = findUser(userId);

        userRepository.delete(user);

        log.info("User deleted : {}", user.getEmail());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private User findUser(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));
    }

}