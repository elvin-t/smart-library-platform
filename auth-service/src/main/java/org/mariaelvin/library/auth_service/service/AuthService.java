package org.mariaelvin.library.auth_service.service;

import lombok.AllArgsConstructor;
import org.mariaelvin.library.auth_service.client.UserClient;
import org.mariaelvin.library.auth_service.dto.*;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserClient userClient;
    private final UserServiceClientFacade userServiceClientFacade;


    @Transactional
    public AdminCreateUserResponse createUserByAdmin(AdminCreateUserRequest request) {

        String email = request.getEmail().trim().toLowerCase();
        String requestedRole = request.getRole().trim().toUpperCase();

        if (authUserRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }

        if (!requestedRole.equals("MEMBER") && !requestedRole.equals("LIBRARIAN")) {
            throw new InvalidUserRequestException(
                    "Admin can create only MEMBER or LIBRARIAN users"
            );
        }

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() ->
                        new InvalidUserRequestException("Role not found: " + requestedRole)
                );

        AuthUser authUser = AuthUser.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .roles(Set.of(role))
                .build();

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setId(savedAuthUser.getId());
        userRequest.setEmail(email);
        userRequest.setFullName(request.getFullName());
        userRequest.setPhone(request.getPhone());

        userServiceClientFacade.createUser(userRequest);

        return AdminCreateUserResponse.builder()
                .id(savedAuthUser.getId())
                .email(savedAuthUser.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .roles(savedAuthUser.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .active(savedAuthUser.isActive())
                .userProfileCreated(true)
                .build();
    }

    @Transactional
    public void register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (authUserRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }

        // ✅ Public registration always creates MEMBER only
        Role role = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new InvalidUserRequestException("Role not found: MEMBER"));

        AuthUser authUser = AuthUser.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .roles(Set.of(role))
                .build();

        AuthUser savedAuthUser = authUserRepository.save(authUser);

        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setId(savedAuthUser.getId());
        userRequest.setEmail(email);
        userRequest.setFullName(request.getFullName());
        userRequest.setPhone(request.getPhone());

        userServiceClientFacade.createUser(userRequest);
    }


    public String login(LoginRequest request) {

        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is deactivated");
        }


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return jwtTokenService.generateToken(user);
    }

    @Transactional
    public AdminUserStatusResponse deactivateUser(Long userId) {

        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Auth user not found with id: " + userId
                ));

        if (!authUser.isActive()) {
            return AdminUserStatusResponse.builder()
                    .id(authUser.getId())
                    .email(authUser.getEmail())
                    .roles(authUser.getRoles()
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet()))
                    .active(false)
                    .message("User is already deactivated")
                    .build();
        }

        authUser.setActive(false);

        AuthUser savedUser = authUserRepository.save(authUser);

        return AdminUserStatusResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .active(savedUser.isActive())
                .message("User deactivated successfully")
                .build();
    }

    @Transactional
    public AdminUserStatusResponse activateUser(Long userId) {

        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Auth user not found with id: " + userId
                ));

        if (authUser.isActive()) {
            return AdminUserStatusResponse.builder()
                    .id(authUser.getId())
                    .email(authUser.getEmail())
                    .roles(authUser.getRoles()
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet()))
                    .active(true)
                    .message("User is already active")
                    .build();
        }

        authUser.setActive(true);

        AuthUser savedUser = authUserRepository.save(authUser);

        return AdminUserStatusResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .active(savedUser.isActive())
                .message("User activated successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public AdminAuthUserStatusResponse getAuthUserStatus(Long userId) {

        AuthUser authUser = authUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Auth user not found with id: " + userId
                ));

        return toAdminAuthUserStatusResponse(authUser);
    }

    @Transactional(readOnly = true)
    public List<AdminAuthUserStatusResponse> getAllAuthUserStatuses() {

        return authUserRepository.findAll()
                .stream()
                .map(this::toAdminAuthUserStatusResponse)
                .collect(Collectors.toList());
    }

    private AdminAuthUserStatusResponse toAdminAuthUserStatusResponse(AuthUser authUser) {

        return AdminAuthUserStatusResponse.builder()
                .id(authUser.getId())
                .email(authUser.getEmail())
                .active(authUser.isActive())
                .roles(authUser.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
