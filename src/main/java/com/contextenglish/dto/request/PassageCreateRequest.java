package com.contextenglish.dto.request;

import com.contextenglish.entity.enums.ContentType;
import com.contextenglish.entity.enums.LevelType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PassageCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String contentText;

    private LevelType level = LevelType.BEGINNER;
    private String topic;
    private ContentType contentType = ContentType.STORY;

    private List<TargetItemRequest> targetItems = new ArrayList<>();
}
