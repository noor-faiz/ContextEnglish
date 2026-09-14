package com.contextenglish.repository;

import com.contextenglish.entity.AcceptableAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcceptableAnswerRepository extends JpaRepository<AcceptableAnswer, Long> {
    List<AcceptableAnswer> findByTargetItemId(Long targetItemId);
}
