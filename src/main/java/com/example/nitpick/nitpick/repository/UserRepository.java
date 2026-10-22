package com.example.nitpick.nitpick.repository;

import com.example.nitpick.nitpick.model.Role;
import com.example.nitpick.nitpick.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByStudentId(String studentId);
    boolean existsByEmail(String email);
    boolean existsByStudentId(String studentId);
    List<User> findByIsActive(Boolean isActive);
    List<User> findByRole(Role role);
}
