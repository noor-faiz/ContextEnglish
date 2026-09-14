package com.contextenglish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeakAreaResponse {
    private String itemType;
    private double accuracyPercent;
    private int totalAttempts;
}
