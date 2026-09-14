package com.contextenglish.entity;

import com.contextenglish.entity.enums.AnswerType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "acceptable_answers")
public class AcceptableAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "target_item_id", nullable = false)
    private TargetItem targetItem;

    @Column(name = "answer_text", nullable = false, length = 200)
    private String answerText;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_type", nullable = false, length = 20)
    private AnswerType answerType = AnswerType.EXACT;

    /** For MCQ/MULTI_SELECT: whether this is one of the correct choices to display + score against. */
    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(name = "points_weight")
    private double pointsWeight = 1.0;
}
