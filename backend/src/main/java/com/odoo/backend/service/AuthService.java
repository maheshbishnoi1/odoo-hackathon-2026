package com.odoo.backend.service;

import com.odoo.backend.dto.LoginResponseDTO;
import com.odoo.backend.dto.RegisterRequestDTO;
import com.odoo.backend.dto.UserResponseDTO;
import com.odoo.backend.entity.User;
import com.odoo.backend.enums.Role;
import com.odoo.backend.repository.UserRepository;
import com.odoo.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Register new user
     */
    public UserResponseDTO register(RegisterRequestDTO request) {

        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UsernameNotFoundException("Email already registered.");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UsernameNotFoundException("Phone number already registered.");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Set Role
        if (request.getRole() == null) {
            user.setRole(Role.DISPATCHER);
        } else {
            user.setRole(request.getRole());
        }

        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return UserResponseDTO.from(savedUser);
    }

    /**
     * Login
     */
    public LoginResponseDTO login(String email, String password) {

        log.info("Login request received for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid email or password."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UsernameNotFoundException("Invalid email or password.");
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new UsernameNotFoundException("Account has been disabled.");
        }

        user.updateLastLogin();
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        log.info("Login successful: {}", user.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(UserResponseDTO.from(user))
                .build();
    }

    /**
     * Get current logged-in user
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentUser(String email) {
        return UserResponseDTO.from(findUserByEmail(email));
    }

    /**
     * Get current logged-in user entity
     */
    @Transactional(readOnly = true)
    public User getCurrentUserEntity(String email) {
        return findUserByEmail(email);
    }

    /**
     * Find user by email
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email));
    }

    /**
     * Find user by id
     */
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with id: " + id));
    }
}