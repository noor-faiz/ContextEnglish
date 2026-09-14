package com.contextenglish.util;

public final class ScoreCalculator {

    private ScoreCalculator() {
    }

    public static final double HINT_PENALTY_FACTOR = 0.8;

    public static double applyHintPenalty(double score, boolean hintUsed) {
        return hintUsed ? score * HINT_PENALTY_FACTOR : score;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
