package com.contextenglish.entity;

import com.contextenglish.entity.enums.AnswerMode;
import com.contextenglish.entity.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "target_items")
public class TargetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "passage_id", nullable = false)
    private Passage passage;

    /** The exact word/phrase as it appears in the passage text (matched case-insensitively for highlighting). */
    @Column(name = "surface_text", nullable = false, length = 150)
    private String surfaceText;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_mode", nullable = false, length = 20)
    private AnswerMode answerMode = AnswerMode.MCQ;

    @Column(name = "hint_text")
    private String hintText;

    @OneToMany(mappedBy = "targetItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcceptableAnswer> acceptableAnswers = new ArrayList<>();

    @OneToMany(mappedBy = "targetItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DistractorOption> distractorOptions = new ArrayList<>();

    @OneToOne(mappedBy = "targetItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private Explanation explanation;
}
