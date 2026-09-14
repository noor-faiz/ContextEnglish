package com.contextenglish.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AnswerSubmitRequest {

    @NotNull
    private Long targetItemId;

    @NotNull
    private Long sessionAttemptId;

    /** For FREE_TEXT mode. */
    private String freeText;

    /** For MCQ mode: the id of the single AcceptableAnswer or DistractorOption chosen. Encoded as "A123"/"D45". */
    private String selectedOptionKey;

    /** For MULTI_SELECT mode: encoded keys of every option the learner checked. */
    private List<String> selectedOptionKeys;

    private boolean hintUsed;
}
