package com.contextenglish.repository;

import com.contextenglish.entity.SessionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionAttemptRepository extends JpaRepository<SessionAttempt, Long> {

    List<SessionAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<SessionAttempt> findFirstByUserIdAndPassageIdAndCompletedAtIsNullOrderByStartedAtDesc(Long userId, Long passageId);

    @Query("SELECT AVG(sa.score) FROM SessionAttempt sa WHERE sa.user.id = :userId " +
           "AND sa.completedAt IS NOT NULL AND sa.completedAt > :since")
    Double findRecentAverageScore(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    long countByUserIdAndCompletedAtIsNotNull(Long userId);

    @Query("SELECT sa FROM SessionAttempt sa WHERE sa.user.id = :userId AND sa.completedAt IS NOT NULL " +
           "ORDER BY sa.completedAt DESC")
    List<SessionAttempt> findCompletedByUserOrderByDateDesc(@Param("userId") Long userId);
}
