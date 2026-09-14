package com.contextenglish.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A single renderable choice for MCQ/MULTI_SELECT modes, shuffled for display. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionChoice {
    private String key;   // e.g. "A123" for an AcceptableAnswer id, "D45" for a DistractorOption id
    private String text;
}
