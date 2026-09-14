package com.contextenglish.dto.response;

import com.contextenglish.entity.enums.LevelType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressSummaryResponse {
    private List<String> scoreLabels;
    private List<Double> scoreValues;
    private int currentStreak;
    private int totalWordsLearned;
    private LevelType currentLevel;
    private double difficultyScore;
    private int sessionsCompleted;
}
