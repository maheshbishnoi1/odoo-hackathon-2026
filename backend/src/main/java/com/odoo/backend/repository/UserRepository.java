package com.odoo.backend.repository;

import com.odoo.backend.entity.User;
import com.odoo.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link User}.
 *
 * Provides CRUD operations along with commonly used
 * lookup, filtering, counting and update queries.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // =========================================================================
    // Authentication
    // =========================================================================

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if email already exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone number already exists.
     */
    boolean existsByPhoneNumber(String phoneNumber);

    // =========================================================================
    // User Listing
    // =========================================================================

    /**
     * Get users by role.
     */
    Page<User> findByRole(Role role, Pageable pageable);

    /**
     * Get enabled users.
     */
    Page<User> findByEnabledTrue(Pageable pageable);

    /**
     * Get disabled users.
     */
    Page<User> findByEnabledFalse(Pageable pageable);

    /**
     * Search users by full name.
     */
    Page<User> findByFullNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    // =========================================================================
    // Dashboard
    // =========================================================================

    /**
     * Count users by role.
     */
    long countByRole(Role role);

    /**
     * Count enabled users.
     */
    long countByEnabledTrue();

    /**
     * Count disabled users.
     */
    long countByEnabledFalse();

    // =========================================================================
    // Reports
    // =========================================================================

    /**
     * Users created between two dates.
     */
    @Query("""
            SELECT u
            FROM User u
            WHERE u.createdAt BETWEEN :startDate AND :endDate
            ORDER BY u.createdAt DESC
            """)
    List<User> findUsersCreatedBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // =========================================================================
    // Last Login
    // =========================================================================

    /**
     * Update user's last login time.
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.lastLogin = :loginTime
            WHERE u.email = :email
            """)
    int updateLastLogin(
            @Param("email") String email,
            @Param("loginTime") LocalDateTime loginTime
    );

    // =========================================================================
    // Enable / Disable User
    // =========================================================================

    /**
     * Enable user.
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.enabled = true
            WHERE u.id = :userId
            """)
    int enableUser(@Param("userId") Long userId);

    /**
     * Disable user.
     */
    @Modifying
    @Query("""
            UPDATE User u
            SET u.enabled = false
            WHERE u.id = :userId
            """)
    int disableUser(@Param("userId") Long userId);

}