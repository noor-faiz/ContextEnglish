package com.contextenglish.service;

import com.contextenglish.dto.request.AnswerSubmitRequest;
import com.contextenglish.dto.response.AnswerResultResponse;
import com.contextenglish.dto.response.ProgressSummaryResponse;
import com.contextenglish.dto.response.WeakAreaResponse;
import com.contextenglish.entity.*;
import com.contextenglish.entity.enums.AnswerStatus;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final SessionAttemptRepository sessionAttemptRepository;
    private final SessionAnswerRepository sessionAnswerRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserProgressRepository userProgressRepository;
    private final TargetItemRepository targetItemRepository;
    private final AdaptiveDifficultyService adaptiveDifficultyService;
    private final AchievementService achievementService;

    private static final int[] REVIEW_INTERVALS_DAYS = {1, 2, 4, 7, 14, 30};

    @Transactional
    public SessionAttempt startSession(User user, Passage passage) {
        SessionAttempt attempt = new SessionAttempt();
        attempt.setUser(user);
        attempt.setPassage(passage);
        return sessionAttemptRepository.save(attempt);
    }

    @Transactional
    public void recordAnswer(User user, SessionAttempt attempt, TargetItem item,
                              AnswerSubmitRequest request, AnswerResultResponse result) {
        SessionAnswer answer = new SessionAnswer();
        answer.setSessionAttempt(attempt);
        answer.setTargetItem(item);
        answer.setSubmittedText(request.getFreeText());
        answer.setStatus(result.getStatus());
        answer.setScoreAwarded(result.getScoreAwarded());
        answer.setHintUsed(request.isHintUsed());
        sessionAnswerRepository.save(answer);

        updateWordProgress(user, item, result.getStatus());
    }

    private void updateWordProgress(User user, TargetItem item, AnswerStatus status) {
        UserWordProgress progress = userWordProgressRepository
                .findByUserIdAndTargetItemId(user.getId(), item.getId())
                .orElseGet(() -> {
                    UserWordProgress p = new UserWordProgress();
                    p.setUser(user);
                    p.setTargetItem(item);
                    return p;
                });

        progress.setAttempts(progress.getAttempts() + 1);
        if (status == AnswerStatus.FULLY_CORRECT) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
            progress.setMasteryLevel(Math.min(5, progress.getMasteryLevel() + 1));
        } else if (status == AnswerStatus.INCORRECT) {
            progress.setMasteryLevel(Math.max(0, progress.getMasteryLevel() - 1));
        }
        int interval = REVIEW_INTERVALS_DAYS[Math.min(progress.getMasteryLevel(), REVIEW_INTERVALS_DAYS.length - 1)];
        progress.setNextReviewDate(status == AnswerStatus.FULLY_CORRECT
                ? LocalDate.now().plusDays(interval)
                : LocalDate.now().plusDays(1));

        userWordProgressRepository.save(progress);
    }

    @Transactional
    public List<Achievement> completeSession(User user, SessionAttempt attempt) {
        List<SessionAnswer> answers = sessionAnswerRepository.findBySessionAttemptId(attempt.getId());
        double avgScore = answers.isEmpty() ? 0 :
                answers.stream().mapToDouble(SessionAnswer::getScoreAwarded).average().orElse(0);

        attempt.setCompletedAt(java.time.LocalDateTime.now());
        attempt.setScore(avgScore);
        sessionAttemptRepository.save(attempt);

        UserProgress progress = userProgressRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress record not found"));

        progress.setTotalScore(progress.getTotalScore() + avgScore);
        progress.setSessionsCompleted(progress.getSessionsCompleted() + 1);
        updateStreak(progress);
        userProgressRepository.save(progress);

        adaptiveDifficultyService.recalculate(user, progress);
        userProgressRepository.save(progress);

        return achievementService.checkAndAwardAfterSession(user, progress);
    }

    private void updateStreak(UserProgress progress) {
        LocalDate today = LocalDate.now();
        LocalDate last = progress.getLastActiveDate();
        if (last == null || last.isBefore(today.minusDays(1))) {
            progress.setCurrentStreak(1);
        } else if (last.equals(today.minusDays(1))) {
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
        }
        // if last == today, streak already counted for today: leave unchanged
        progress.setLastActiveDate(today);
    }

    public ProgressSummaryResponse getSummary(User user) {
        UserProgress progress = userProgressRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Progress record not found"));

        List<SessionAttempt> completed = sessionAttemptRepository.findCompletedByUserOrderByDateDesc(user.getId());
        Collections.reverse(completed); // oldest first for a left-to-right chart
        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        int start = Math.max(0, completed.size() - 14);
        for (SessionAttempt sa : completed.subList(start, completed.size())) {
            labels.add(sa.getCompletedAt().format(fmt));
            values.add(sa.getScore());
        }

        long wordsLearned = userWordProgressRepository.countByUserIdAndMasteryLevelGreaterThanEqual(user.getId(), 3);

        return new ProgressSummaryResponse(labels, values, progress.getCurrentStreak(),
                (int) wordsLearned, user.getCurrentLevel(), progress.getDifficultyScore(),
                progress.getSessionsCompleted());
    }

    public List<WeakAreaResponse> getWeakAreas(User user) {
        List<Object[]> rows = userWordProgressRepository.findAccuracyByItemType(user.getId());
        List<WeakAreaResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            String itemType = row[0].toString();
            long correct = ((Number) row[1]).longValue();
            long attempts = ((Number) row[2]).longValue();
            double accuracy = attempts == 0 ? 0 : (correct / (double) attempts) * 100;
            result.add(new WeakAreaResponse(itemType, accuracy, (int) attempts));
        }
        result.sort((a, b) -> Double.compare(a.getAccuracyPercent(), b.getAccuracyPercent()));
        return result;
    }
}
