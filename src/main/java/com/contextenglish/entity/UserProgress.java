package com.contextenglish.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_progress")
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "total_score")
    private double totalScore = 0.0;

    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    /** Continuous 0.0 - 3.0 scale that drives gradual level movement; mapped to LevelType. */
    @Column(name = "difficulty_score")
    private double difficultyScore = 0.5;

    @Column(name = "sessions_completed")
    private int sessionsCompleted = 0;
}
