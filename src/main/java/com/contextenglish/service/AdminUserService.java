package com.contextenglish.service;

import com.contextenglish.entity.User;
import com.contextenglish.entity.enums.Role;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void changeRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN && newRole == Role.LEARNER && countAdmins() <= 1) {
            throw new IllegalStateException("Cannot demote the last remaining admin.");
        }
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN && countAdmins() <= 1) {
            throw new IllegalStateException("Cannot delete the last remaining admin.");
        }
        userRepository.delete(user);
    }

    private long countAdmins() {
        return userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).count();
    }
}
