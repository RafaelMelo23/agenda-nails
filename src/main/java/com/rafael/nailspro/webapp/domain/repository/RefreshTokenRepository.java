package com.rafael.nailspro.webapp.domain.repository;

import com.rafael.nailspro.webapp.domain.model.RefreshToken;
import com.rafael.nailspro.webapp.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser_Id(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = TRUE WHERE rt.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") Long userId);

    Long user(User user);

    void deleteByExpiryDateBefore(Instant now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = TRUE WHERE rt.token = :token AND rt.user.id = :userId")
    void revokeToken(@Param("token") String token,
                     @Param("userId") Long userId);
}
