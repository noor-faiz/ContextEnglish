package com.contextenglish.repository;

import com.contextenglish.entity.TargetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TargetItemRepository extends JpaRepository<TargetItem, Long> {
    List<TargetItem> findByPassageId(Long passageId);
}
