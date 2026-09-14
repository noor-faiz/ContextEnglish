package com.contextenglish.entity;

import com.contextenglish.entity.enums.AchievementCriteria;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "achievements")
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "criteria_type", nullable = false, length = 30)
    private AchievementCriteria criteriaType;

    @Column(name = "criteria_value")
    private Integer criteriaValue;

    @Column(length = 10)
    private String icon = "🏆";
}
