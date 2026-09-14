package com.contextenglish.service;

import com.contextenglish.dto.request.ChangePasswordRequest;
import com.contextenglish.dto.request.PreferenceUpdateRequest;
import com.contextenglish.dto.request.RegisterRequest;
import com.contextenglish.entity.User;
import com.contextenglish.entity.UserPreference;
import com.contextenglish.entity.UserProgress;
import com.contextenglish.entity.enums.LevelType;
import com.contextenglish.entity.enums.Role;
import com.contextenglish.exception.DuplicateUserException;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.UserPreferenceRepository;
import com.contextenglish.repository.UserProgressRepository;
import com.contextenglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserProgressRepository userProgressRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("That username is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("An account with that email already exists.");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.LEARNER);
        user.setCurrentLevel(LevelType.BEGINNER);
        userRepository.save(user);

        UserPreference preference = new UserPreference();
        preference.setUser(user);
        userPreferenceRepository.save(preference);

        UserProgress progress = new UserProgress();
        progress.setUser(user);
        userProgressRepository.save(progress);

        return user;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public void updatePreferences(Long userId, PreferenceUpdateRequest request) {
        UserPreference pref = userPreferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Preferences not found"));
        if (request.getNativeLanguage() != null) pref.setNativeLanguage(request.getNativeLanguage());
        if (request.getDailyGoalMinutes() != null) pref.setDailyGoalMinutes(request.getDailyGoalMinutes());
        pref.setStretchMode(request.isStretchMode());
        if (request.getTopicsOfInterest() != null) pref.setTopicsOfInterest(request.getTopicsOfInterest());
        userPreferenceRepository.save(pref);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getById(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void markOnboardingComplete(Long userId) {
        User user = getById(userId);
        user.setOnboardingComplete(true);
        userRepository.save(user);
    }
}
