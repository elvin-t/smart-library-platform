package org.mariaelvin.library.user_service.service;

import lombok.RequiredArgsConstructor;
import org.mariaelvin.library.user_service.dto.*;
import org.mariaelvin.library.user_service.entity.User;
import org.mariaelvin.library.user_service.exception.UserAlreadyExistsException;
import org.mariaelvin.library.user_service.exception.UserNotFoundException;
import org.mariaelvin.library.user_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        return userRepository.findById(request.getId())
                .map(this::toResponse)
                .orElseGet(() -> {

                    if (userRepository.existsByEmail(request.getEmail())) {
                        throw new UserAlreadyExistsException(
                                "User already exists with email: " + request.getEmail()
                        );
                    }

                    User user = User.builder()
                            .id(request.getId())
                            .email(request.getEmail())
                            .fullName(request.getFullName())
                            .phone(request.getPhone())
                            .membershipType(MembershipType.STANDARD)
                            .membershipStatus(MembershipStatus.ACTIVE)
                            .build();

                    return toResponse(userRepository.save(user));
                });
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .membershipType(user.getMembershipType())
                .membershipStatus(user.getMembershipStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}