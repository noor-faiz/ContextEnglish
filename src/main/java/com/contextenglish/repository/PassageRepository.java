package com.contextenglish.repository;

import com.contextenglish.entity.Passage;
import com.contextenglish.entity.enums.ContentType;
import com.contextenglish.entity.enums.LevelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PassageRepository extends JpaRepository<Passage, Long> {

    List<Passage> findByLevelAndContentType(LevelType level, ContentType contentType);

    List<Passage> findByContentType(ContentType contentType);

    @Query("SELECT p FROM Passage p WHERE p.level = :level AND p.contentType = :contentType " +
           "AND p.id NOT IN (SELECT sa.passage.id FROM SessionAttempt sa " +
           "WHERE sa.user.id = :userId AND sa.completedAt IS NOT NULL AND sa.completedAt > :sinceDate)")
    List<Passage> findUnseenByLevel(@Param("userId") Long userId,
                                     @Param("level") LevelType level,
                                     @Param("contentType") ContentType contentType,
                                     @Param("sinceDate") LocalDateTime sinceDate);

    @Query("SELECT DISTINCT ti.passage FROM UserWordProgress uwp JOIN uwp.targetItem ti " +
           "WHERE uwp.user.id = :userId AND uwp.nextReviewDate <= CURRENT_DATE")
    List<Passage> findPassagesWithDueWords(@Param("userId") Long userId);
}
