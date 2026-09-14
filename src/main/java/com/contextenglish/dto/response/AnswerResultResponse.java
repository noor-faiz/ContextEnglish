package com.contextenglish.dto.response;

import com.contextenglish.entity.enums.AnswerStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResultResponse {

    private AnswerStatus status;
    private double scoreAwarded;
    private String explanationText;
    private String explanationNative;
    private String correctAnswerDisplay;

    public static AnswerResultResponse of(AnswerStatus status, double score,
                                           String explanationText, String explanationNative,
                                           String correctAnswerDisplay) {
        return new AnswerResultResponse(status, score, explanationText, explanationNative, correctAnswerDisplay);
    }
}
