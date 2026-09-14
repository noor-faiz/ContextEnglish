package com.contextenglish.service;

import com.contextenglish.dto.request.AnswerSubmitRequest;
import com.contextenglish.dto.response.AnswerResultResponse;
import com.contextenglish.entity.AcceptableAnswer;
import com.contextenglish.entity.Explanation;
import com.contextenglish.entity.TargetItem;
import com.contextenglish.entity.enums.AnswerStatus;
import com.contextenglish.entity.enums.AnswerType;
import com.contextenglish.util.ScoreCalculator;
import com.contextenglish.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Rule-based answer evaluation engine — no NLP model, no external API.
 * Every TargetItem carries admin-curated AcceptableAnswer rows (EXACT / SYNONYM /
 * KEYWORD / CATEGORY) plus DistractorOption rows for MCQ/MULTI_SELECT display.
 * This class turns one submitted answer into a FULLY_CORRECT / PARTIALLY_CORRECT /
 * INCORRECT verdict plus a score and the matching explanation.
 */
@Service
public class EvaluationService {

    public AnswerResultResponse evaluate(TargetItem item, AnswerSubmitRequest request) {
        AnswerResultResponse result = switch (item.getAnswerMode()) {
            case MCQ -> evaluateSingleChoice(item, request.getSelectedOptionKey());
            case MULTI_SELECT -> evaluateMultiSelect(item, request.getSelectedOptionKeys());
            case FREE_TEXT -> evaluateFreeText(item, request.getFreeText());
        };

        double finalScore = ScoreCalculator.applyHintPenalty(result.getScoreAwarded(), request.isHintUsed());
        result.setScoreAwarded(finalScore);
        attachExplanation(item, result);
        return result;
    }

    // ---------------------------------------------------------------- MCQ

    private AnswerResultResponse evaluateSingleChoice(TargetItem item, String selectedKey) {
        if (selectedKey == null || selectedKey.isBlank()) {
            return AnswerResultResponse.of(AnswerStatus.INCORRECT, 0, null, null, correctDisplay(item));
        }
        boolean correct = selectedKey.startsWith("A")
                && item.getAcceptableAnswers().stream()
                        .anyMatch(a -> a.isRequired() && ("A" + a.getId()).equals(selectedKey));

        return correct
                ? AnswerResultResponse.of(AnswerStatus.FULLY_CORRECT, 100, null, null, correctDisplay(item))
                : AnswerResultResponse.of(AnswerStatus.INCORRECT, 0, null, null, correctDisplay(item));
    }

    // ---------------------------------------------------------- MULTI_SELECT

    private AnswerResultResponse evaluateMultiSelect(TargetItem item, List<String> selectedKeys) {
        Set<String> selected = selectedKeys == null ? Set.of() : Set.copyOf(selectedKeys);

        Set<String> requiredKeys = item.getAcceptableAnswers().stream()
                .filter(AcceptableAnswer::isRequired)
                .map(a -> "A" + a.getId())
                .collect(Collectors.toSet());

        if (requiredKeys.isEmpty()) {
            return AnswerResultResponse.of(AnswerStatus.INCORRECT, 0, null, null, correctDisplay(item));
        }

        long correctSelections = selected.stream().filter(requiredKeys::contains).count();
        long wrongSelections = selected.stream().filter(k -> !requiredKeys.contains(k)).count();

        double rawScore = (correctSelections - wrongSelections) / (double) requiredKeys.size();
        double normalizedScore = ScoreCalculator.clamp(rawScore, 0.0, 1.0) * 100;

        AnswerStatus status = normalizedScore >= 99.99 ? AnswerStatus.FULLY_CORRECT
                : normalizedScore > 0 ? AnswerStatus.PARTIALLY_CORRECT
                : AnswerStatus.INCORRECT;

        return AnswerResultResponse.of(status, normalizedScore, null, null, correctDisplay(item));
    }

    // -------------------------------------------------------------- FREE_TEXT

    private AnswerResultResponse evaluateFreeText(TargetItem item, String rawInput) {
        String normalizedInput = TextNormalizer.normalize(rawInput);
        if (normalizedInput.isBlank()) {
            return AnswerResultResponse.of(AnswerStatus.INCORRECT, 0, null, null, correctDisplay(item));
        }

        // Pass 1: EXACT / SYNONYM -> full credit on a direct normalized match.
        for (AcceptableAnswer ans : item.getAcceptableAnswers()) {
            if (ans.getAnswerType() == AnswerType.EXACT || ans.getAnswerType() == AnswerType.SYNONYM) {
                if (normalizedInput.equals(TextNormalizer.normalize(ans.getAnswerText()))
                        || TextNormalizer.normalizeWord(normalizedInput).equals(TextNormalizer.normalizeWord(ans.getAnswerText()))) {
                    return AnswerResultResponse.of(AnswerStatus.FULLY_CORRECT, 100 * ans.getPointsWeight(),
                            null, null, correctDisplay(item));
                }
            }
        }

        // Pass 2: KEYWORD -> the input merely needs to contain the key idea -> partial credit.
        for (AcceptableAnswer ans : item.getAcceptableAnswers()) {
            if (ans.getAnswerType() == AnswerType.KEYWORD
                    && TextNormalizer.containsKeyword(normalizedInput, ans.getAnswerText())) {
                return AnswerResultResponse.of(AnswerStatus.PARTIALLY_CORRECT, 50 * ans.getPointsWeight(),
                        null, null, correctDisplay(item));
            }
        }

        // Pass 3: CATEGORY answers are meant for a picker UI, not free text, but we still
        // give partial credit if the learner happened to type the category name itself.
        for (AcceptableAnswer ans : item.getAcceptableAnswers()) {
            if (ans.getAnswerType() == AnswerType.CATEGORY
                    && normalizedInput.equals(TextNormalizer.normalize(ans.getAnswerText()))) {
                return AnswerResultResponse.of(AnswerStatus.PARTIALLY_CORRECT, 50 * ans.getPointsWeight(),
                        null, null, correctDisplay(item));
            }
        }

        return AnswerResultResponse.of(AnswerStatus.INCORRECT, 0, null, null, correctDisplay(item));
    }

    // ------------------------------------------------------------------ shared

    private String correctDisplay(TargetItem item) {
        return item.getAcceptableAnswers().stream()
                .filter(AcceptableAnswer::isRequired)
                .map(AcceptableAnswer::getAnswerText)
                .collect(Collectors.joining(", "));
    }

    private void attachExplanation(TargetItem item, AnswerResultResponse result) {
        Explanation explanation = item.getExplanation();
        if (explanation != null) {
            result.setExplanationText(explanation.getExplanationText());
            result.setExplanationNative(explanation.getExplanationNative());
        }
    }
}
