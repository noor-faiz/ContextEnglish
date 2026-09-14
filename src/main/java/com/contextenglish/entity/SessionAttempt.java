package com.contextenglish.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "session_attempts")
public class SessionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "passage_id", nullable = false)
    private Passage passage;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Percentage score 0-100, set once the session is completed. */
    private Double score;

    @OneToMany(mappedBy = "sessionAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionAnswer> answers = new ArrayList<>();
}
