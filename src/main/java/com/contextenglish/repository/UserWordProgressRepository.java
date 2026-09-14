package com.contextenglish.repository;

import com.contextenglish.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserIdAndTargetItemId(Long userId, Long targetItemId);

    List<UserWordProgress> findByUserIdOrderByMasteryLevelAsc(Long userId);

    List<UserWordProgress> findByUserIdAndNextReviewDateLessThanEqual(Long userId, LocalDate date);

    long countByUserIdAndMasteryLevelGreaterThanEqual(Long userId, int masteryLevel);

    @Query("SELECT ti.itemType, SUM(uwp.correctCount), SUM(uwp.attempts) " +
           "FROM UserWordProgress uwp JOIN uwp.targetItem ti WHERE uwp.user.id = :userId GROUP BY ti.itemType")
    List<Object[]> findAccuracyByItemType(@Param("userId") Long userId);
}
