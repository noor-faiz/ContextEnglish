package com.contextenglish.dto.request;

import com.contextenglish.entity.enums.AnswerMode;
import com.contextenglish.entity.enums.ItemType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TargetItemRequest {
    private String surfaceText;
    private ItemType itemType = ItemType.WORD;
    private AnswerMode answerMode = AnswerMode.MCQ;
    private String hintText;
    private List<AcceptableAnswerRequest> acceptableAnswers = new ArrayList<>();
    private List<String> distractorOptions = new ArrayList<>();
    private String explanationText;
    private String explanationNative;
}
