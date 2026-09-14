package com.contextenglish.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** ISO-ish language code / label used to show native-language explanations, e.g. "bn" for Bangla. */
    @Column(name = "native_language", length = 30)
    private String nativeLanguage = "bn";

    @Column(name = "daily_goal_minutes")
    private Integer dailyGoalMinutes = 10;

    @Column(name = "stretch_mode", nullable = false)
    private boolean stretchMode = false;

    @Column(name = "topics_of_interest")
    private String topicsOfInterest;
}
