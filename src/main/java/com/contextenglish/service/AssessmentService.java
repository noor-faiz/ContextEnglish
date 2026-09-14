package com.contextenglish.service;

import com.contextenglish.entity.AssessmentQuestion;
import com.contextenglish.entity.User;
import com.contextenglish.entity.enums.LevelType;
import com.contextenglish.repository.AssessmentQuestionRepository;
import com.contextenglish.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Runs the short placement test shown right after registration.
 * Scoring is intentionally simple: each question is tagged with the level it
 * represents; the learner's level is decided by how many questions they get
 * right at each difficulty tier, biased toward the highest tier they show
 * solid (>=70%) accuracy in.
 */
@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final UserRepository userRepository;

    public List<AssessmentQuestion> getAllQuestions() {
        return assessmentQuestionRepository.findAll();
    }

    @Transactional
    public LevelType scoreAndAssignLevel(User user, Map<Long, Integer> answers) {
        List<AssessmentQuestion> questions = getAllQuestions();

        int beginnerTotal = 0, beginnerCorrect = 0;
        int intermediateTotal = 0, intermediateCorrect = 0;
        int advancedTotal = 0, advancedCorrect = 0;

        for (AssessmentQuestion q : questions) {
            Integer submitted = answers.get(q.getId());
            boolean correct = submitted != null && submitted == q.getCorrectOption();
            switch (q.getDifficultyLevel()) {
                case BEGINNER -> { beginnerTotal++; if (correct) beginnerCorrect++; }
                case INTERMEDIATE -> { intermediateTotal++; if (correct) intermediateCorrect++; }
                case ADVANCED -> { advancedTotal++; if (correct) advancedCorrect++; }
            }
        }

        LevelType assigned = LevelType.BEGINNER;
        if (beginnerTotal == 0 || beginnerCorrect / (double) beginnerTotal >= 0.6) {
            assigned = LevelType.INTERMEDIATE;
            if (intermediateTotal > 0 && intermediateCorrect / (double) intermediateTotal >= 0.6) {
                assigned = LevelType.ADVANCED;
                if (advancedTotal > 0 && advancedCorrect / (double) advancedTotal < 0.4) {
                    // Not solid enough at the top tier: settle back to intermediate.
                    assigned = LevelType.INTERMEDIATE;
                }
            }
        }

        user.setCurrentLevel(assigned);
        user.setOnboardingComplete(true);
        userRepository.save(user);
        return assigned;
    }
}
