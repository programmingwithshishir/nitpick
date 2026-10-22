package com.example.nitpick.nitpick.service;

import com.example.nitpick.nitpick.dto.UserRequest;
import com.example.nitpick.nitpick.dto.UserResponse;
import com.example.nitpick.nitpick.exception.ConflictException;
import com.example.nitpick.nitpick.exception.ResourceNotFoundException;
import com.example.nitpick.nitpick.model.Role;
import com.example.nitpick.nitpick.model.User;
import com.example.nitpick.nitpick.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        if (request.getStudentId() != null && !request.getStudentId().isBlank()) {
            if (userRepository.existsByStudentId(request.getStudentId())) {
                throw new ConflictException("User with student ID " + request.getStudentId() + " already exists");
            }
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .studentId(request.getStudentId() != null ? request.getStudentId().trim() : null)
                .password(request.getPassword())
                .role(request.getRole() != null ? request.getRole() : Role.STUDENT)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Boolean activeOnly) {
        List<User> users;
        if (Boolean.TRUE.equals(activeOnly)) {
            users = userRepository.findByIsActive(true);
        } else {
            users = userRepository.findAll();
        }
        return users.stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (!user.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        if (request.getStudentId() != null && !request.getStudentId().isBlank()) {
            String trimmedStudentId = request.getStudentId().trim();
            if (!trimmedStudentId.equalsIgnoreCase(user.getStudentId()) && userRepository.existsByStudentId(trimmedStudentId)) {
                throw new ConflictException("User with student ID " + request.getStudentId() + " already exists");
            }
            user.setStudentId(trimmedStudentId);
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(normalizedEmail);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(request.getPassword());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsActive(false);
        userRepository.save(user);
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
