package com.contextenglish.service;

import com.contextenglish.entity.Achievement;
import com.contextenglish.entity.User;
import com.contextenglish.entity.UserAchievement;
import com.contextenglish.entity.UserProgress;
import com.contextenglish.entity.enums.AchievementCriteria;
import com.contextenglish.repository.AchievementRepository;
import com.contextenglish.repository.UserAchievementRepository;
import com.contextenglish.repository.UserWordProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Checks achievement criteria after key events and awards any newly-earned badges. */
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserWordProgressRepository userWordProgressRepository;

    @Transactional
    public List<Achievement> checkAndAwardAfterSession(User user, UserProgress progress) {
        List<Achievement> newlyEarned = new java.util.ArrayList<>();
        for (Achievement achievement : achievementRepository.findAll()) {
            if (userAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
                continue;
            }
            if (isEarned(achievement, user, progress)) {
                award(user, achievement);
                newlyEarned.add(achievement);
            }
        }
        return newlyEarned;
    }

    private boolean isEarned(Achievement achievement, User user, UserProgress progress) {
        int value = achievement.getCriteriaValue() == null ? 0 : achievement.getCriteriaValue();
        return switch (achievement.getCriteriaType()) {
            case FIRST_SESSION -> progress.getSessionsCompleted() >= 1;
            case SESSIONS_COMPLETED -> progress.getSessionsCompleted() >= value;
            case STREAK_DAYS -> progress.getCurrentStreak() >= value;
            case LEVEL_UP -> user.getCurrentLevel() != com.contextenglish.entity.enums.LevelType.BEGINNER;
            case WORDS_LEARNED ->
                    userWordProgressRepository.countByUserIdAndMasteryLevelGreaterThanEqual(user.getId(), 3) >= value;
        };
    }

    private void award(User user, Achievement achievement) {
        UserAchievement ua = new UserAchievement();
        ua.setUser(user);
        ua.setAchievement(achievement);
        userAchievementRepository.save(ua);
    }
}
