package com.contextenglish.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "explanations")
public class Explanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "target_item_id", nullable = false, unique = true)
    private TargetItem targetItem;

    @Column(name = "explanation_text", nullable = false, columnDefinition = "TEXT")
    private String explanationText;

    /** Optional native-language (e.g. Bangla) explanation shown based on the learner's preference. */
    @Column(name = "explanation_native", columnDefinition = "TEXT")
    private String explanationNative;
}
