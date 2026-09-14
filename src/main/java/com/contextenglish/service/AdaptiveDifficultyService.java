package com.contextenglish.service;

import com.contextenglish.entity.User;
import com.contextenglish.entity.UserProgress;
import com.contextenglish.entity.enums.LevelType;
import com.contextenglish.repository.SessionAttemptRepository;
import com.contextenglish.repository.UserRepository;
import com.contextenglish.util.ScoreCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Nudges the learner's continuous difficulty_score up/down after each
 * completed session based on rolling recent accuracy, then maps that
 * continuous score onto the simple BEGINNER/INTERMEDIATE/ADVANCED label
 * shown in the UI. This gives gradual movement instead of abrupt jumps.
 */
@Service
@RequiredArgsConstructor
public class AdaptiveDifficultyService {

    private static final double PROMOTE_THRESHOLD = 0.75;
    private static final double DEMOTE_THRESHOLD = 0.40;
    private static final double STEP = 0.15;

    private final SessionAttemptRepository sessionAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public void recalculate(User user, UserProgress progress) {
        Double avgScore = sessionAttemptRepository.findRecentAverageScore(
                user.getId(), LocalDateTime.now().minusDays(14));
        if (avgScore == null) {
            return;
        }
        double recentAccuracy = avgScore / 100.0;

        double newScore = progress.getDifficultyScore();
        if (recentAccuracy >= PROMOTE_THRESHOLD) {
            newScore += STEP;
        } else if (recentAccuracy <= DEMOTE_THRESHOLD) {
            newScore -= STEP;
        }
        newScore = ScoreCalculator.clamp(newScore, 0.0, 3.0);
        progress.setDifficultyScore(newScore);

        LevelType newLevel = scoreToLevel(newScore);
        if (newLevel != user.getCurrentLevel()) {
            user.setCurrentLevel(newLevel);
            userRepository.save(user);
        }
    }

    private LevelType scoreToLevel(double score) {
        if (score < 1.0) return LevelType.BEGINNER;
        if (score < 2.0) return LevelType.INTERMEDIATE;
        return LevelType.ADVANCED;
    }
}
