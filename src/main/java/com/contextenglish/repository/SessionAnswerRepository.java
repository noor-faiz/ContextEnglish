package com.contextenglish.repository;

import com.contextenglish.entity.SessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, Long> {
    List<SessionAnswer> findBySessionAttemptId(Long sessionAttemptId);
}
