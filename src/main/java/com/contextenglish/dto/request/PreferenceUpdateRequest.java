package com.contextenglish.dto.request;

import lombok.Data;

@Data
public class PreferenceUpdateRequest {
    private String nativeLanguage;
    private Integer dailyGoalMinutes;
    private boolean stretchMode;
    private String topicsOfInterest;
}
