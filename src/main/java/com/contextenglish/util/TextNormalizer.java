package com.contextenglish.util;

/**
 * Lightweight, dependency-free text normalization used by the evaluation engine
 * for free-text answer matching. No NLP library involved: lowercase, strip
 * punctuation, collapse whitespace, and apply a few common suffix-stripping rules
 * so that "tired" / "tiredness"-ish variants and simple plural/verb forms line up
 * with admin-curated acceptable answers without requiring an exact string match.
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        String s = input.toLowerCase().trim();
        s = s.replaceAll("[^a-z0-9\\s]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    /** Normalizes a single word/token further with basic suffix stripping (used for word-level compares). */
    public static String normalizeWord(String word) {
        String s = normalize(word);
        if (s.contains(" ")) {
            return s; // multi-word phrase: don't stem
        }
        return basicStem(s);
    }

    private static String basicStem(String word) {
        if (word.length() > 5 && word.endsWith("ing")) return word.substring(0, word.length() - 3);
        if (word.length() > 4 && word.endsWith("ed")) return word.substring(0, word.length() - 2);
        if (word.length() > 4 && word.endsWith("es")) return word.substring(0, word.length() - 2);
        if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss")) return word.substring(0, word.length() - 1);
        return word;
    }

    /** True if the normalized haystack contains the normalized needle as a whole word/phrase substring. */
    public static boolean containsKeyword(String normalizedHaystack, String rawKeyword) {
        String needle = normalize(rawKeyword);
        if (needle.isEmpty()) {
            return false;
        }
        return (" " + normalizedHaystack + " ").contains(" " + needle + " ")
                || normalizedHaystack.contains(needle);
    }
}
