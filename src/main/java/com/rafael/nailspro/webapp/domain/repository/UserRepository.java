package com.rafael.nailspro.webapp.domain.repository;

import com.rafael.nailspro.webapp.domain.enums.user.UserStatus;
import com.rafael.nailspro.webapp.domain.model.User;
import com.rafael.nailspro.webapp.shared.tenant.IgnoreTenantFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @IgnoreTenantFilter
    Optional<User> findByEmailIgnoreCase(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.password = :newPassword WHERE u.email = :email")
    void updatePassword(@Param("email") String userEmail,
                        @Param("newPassword") String newPassword);

    @IgnoreTenantFilter
    boolean existsByEmail(String email);

    boolean existsByIdAndStatus(Long id, UserStatus status);
}
