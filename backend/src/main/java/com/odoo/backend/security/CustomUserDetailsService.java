package com.odoo.backend.security;

import com.odoo.backend.entity.User;
import com.odoo.backend.exception.ResourceNotFoundException;
import com.odoo.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads user details from the database for Spring Security.
 *
 * Spring Security uses this service during authentication
 * and JWT validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by email.
     *
     * @param email User email
     * @return UserDetails
     * @throws UsernameNotFoundException if user does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        log.info("Loading user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

        return user;
    }

    /**
     * Returns the complete User entity.
     */
    @Transactional(readOnly = true)
    public User loadUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + userId
                        ));
    }

    /**
     * Returns the complete User entity.
     */
    @Transactional(readOnly = true)
    public User loadUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found with email: " + email
                        ));
    }

}