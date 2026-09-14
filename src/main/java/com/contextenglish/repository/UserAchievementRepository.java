package com.contextenglish.repository;

import com.contextenglish.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
    long countByUserId(Long userId);
}
