package org.mariaelvin.library.auth_service.security;

import org.mariaelvin.library.auth_service.entity.AuthUser;
import org.mariaelvin.library.auth_service.repository.AuthUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service   // ✅ CRITICAL
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public CustomUserDetailsService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {

        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
                user.getEmail(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}