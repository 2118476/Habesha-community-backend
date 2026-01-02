package com.habesha.community.repository;

import com.habesha.community.model.Role;
import com.habesha.community.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for performing CRUD operations on {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    /**
     * Find a single user by their unique username/handle.
     */
    Optional<User> findByUsername(String username);

    /**
     * Lookup a user by their reset password token.  This is used
     * during password reset flows to retrieve the appropriate user
     * given a token that was previously generated and stored on the
     * user record.
     */
    Optional<User> findByResetPasswordToken(String resetPasswordToken);

    // ✅ Add this line
    List<User> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(String name, String username);
}
