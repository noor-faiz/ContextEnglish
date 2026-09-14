package com.contextenglish.dto.response;

import com.contextenglish.entity.enums.AnswerMode;
import com.contextenglish.entity.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** What the frontend needs to render one clickable target word/phrase and its answer widget. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetItemView {
    private Long id;
    private String surfaceText;
    private ItemType itemType;
    private AnswerMode answerMode;
    private String hintText;
    private List<OptionChoice> options; // populated for MCQ / MULTI_SELECT
}
