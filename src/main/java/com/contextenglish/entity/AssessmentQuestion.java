package com.contextenglish.entity;

import com.contextenglish.entity.enums.LevelType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Self-contained placement-test question: 4 fixed options + which one is correct.
 * Kept simple (no child tables) since this is a small, fixed, admin-curated question bank.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "option_1", nullable = false)
    private String option1;
    @Column(name = "option_2", nullable = false)
    private String option2;
    @Column(name = "option_3", nullable = false)
    private String option3;
    @Column(name = "option_4", nullable = false)
    private String option4;

    /** 1-based index (1-4) of the correct option. */
    @Column(name = "correct_option", nullable = false)
    private int correctOption;

    /** The level this question is pitched at / contributes toward if answered correctly. */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private LevelType difficultyLevel;
}
