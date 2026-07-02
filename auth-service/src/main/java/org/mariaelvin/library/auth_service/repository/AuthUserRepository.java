package org.mariaelvin.library.auth_service.repository;

import org.mariaelvin.library.auth_service.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
