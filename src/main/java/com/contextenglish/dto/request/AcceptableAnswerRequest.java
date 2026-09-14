package com.contextenglish.dto.request;

import com.contextenglish.entity.enums.AnswerType;
import lombok.Data;

@Data
public class AcceptableAnswerRequest {
    private String answerText;
    private AnswerType answerType = AnswerType.EXACT;
    private boolean required = true;
    private double pointsWeight = 1.0;
}
