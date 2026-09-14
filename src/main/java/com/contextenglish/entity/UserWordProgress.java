package com.contextenglish.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_word_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "target_item_id"}))
public class UserWordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "target_item_id", nullable = false)
    private TargetItem targetItem;

    /** 0 = never seen, 5 = mastered. */
    @Column(name = "mastery_level", nullable = false)
    private int masteryLevel = 0;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(name = "correct_count", nullable = false)
    private int correctCount = 0;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;
}
