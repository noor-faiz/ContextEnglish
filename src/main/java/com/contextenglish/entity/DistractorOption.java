package com.contextenglish.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "distractor_options")
public class DistractorOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "target_item_id", nullable = false)
    private TargetItem targetItem;

    @Column(name = "option_text", nullable = false, length = 200)
    private String optionText;
}
