package org.mariaelvin.library.auth_service.service;

import lombok.AllArgsConstructor;
import org.mariaelvin.library.auth_service.config.UserClient;
import org.mariaelvin.library.auth_service.dto.CreateUserRequest;
import org.mariaelvin.library.auth_service.dto.LoginRequest;
import org.mariaelvin.library.auth_service.dto.RegisterRequest;
import org.mariaelvin.library.auth_service.entity.AuthUser;
import org.mariaelvin.library.auth_service.entity.Role;
import org.mariaelvin.library.auth_service.exception.InvalidCredentialsException;
import org.mariaelvin.library.auth_service.exception.InvalidUserRequestException;
import org.mariaelvin.library.auth_service.exception.UserAlreadyExistsException;
import org.mariaelvin.library.auth_service.exception.UserNotFoundException;
import org.mariaelvin.library.auth_service.repository.AuthUserRepository;
import org.mariaelvin.library.auth_service.repository.RoleRepository;
import org.mariaelvin.library.auth_service.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@AllArgsConstructor
public class AuthService {

    private  final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserClient userClient;

    @Transactional
    public void register(RegisterRequest request) {

        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // ✅ Validate frontend role
        String requestedRole = request.getRole().toUpperCase();

        if (!requestedRole.equals("MEMBER") && !requestedRole.equals("LIBRARIAN")) {
            throw new InvalidUserRequestException("Invalid role selected");
        }

        // ✅ Fetch role from Auth DB
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new RuntimeException("Role not found: " + requestedRole));

        // ✅ Save credentials in Auth DB
        AuthUser authUser = AuthUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .roles(Set.of(role))
                .build();

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        // ✅ Send profile data to User Service
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setId(savedAuthUser.getId());
        userRequest.setEmail(request.getEmail());
        userRequest.setFullName(request.getFullName());
        userRequest.setPhone(request.getPhone());

        userClient.createUser(userRequest);
    }

    public String login(LoginRequest request) {

        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return jwtTokenService.generateToken(user);
    }
}
