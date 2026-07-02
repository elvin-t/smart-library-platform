package org.mariaelvin.library.user_service.repository;

import org.mariaelvin.library.user_service.dto.MembershipStatus;
import org.mariaelvin.library.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByMembershipStatus(MembershipStatus membershipStatus);
}