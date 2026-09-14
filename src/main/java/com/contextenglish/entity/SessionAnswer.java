package com.contextenglish.entity;

import com.contextenglish.entity.enums.AnswerStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "session_answers")
public class SessionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_attempt_id", nullable = false)
    private SessionAttempt sessionAttempt;

    @ManyToOne
    @JoinColumn(name = "target_item_id", nullable = false)
    private TargetItem targetItem;

    @Column(name = "submitted_text", columnDefinition = "TEXT")
    private String submittedText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnswerStatus status;

    @Column(name = "score_awarded")
    private double scoreAwarded;

    @Column(name = "hint_used", nullable = false)
    private boolean hintUsed = false;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();
}
