package com.contextenglish.service;

import com.contextenglish.dto.response.OptionChoice;
import com.contextenglish.dto.response.TargetItemView;
import com.contextenglish.entity.AcceptableAnswer;
import com.contextenglish.entity.DistractorOption;
import com.contextenglish.entity.Passage;
import com.contextenglish.entity.TargetItem;
import com.contextenglish.entity.User;
import com.contextenglish.entity.UserPreference;
import com.contextenglish.entity.enums.ContentType;
import com.contextenglish.entity.enums.LevelType;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.PassageRepository;
import com.contextenglish.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Decides *which* passage a learner sees next (personalization + adaptive
 * difficulty + spaced word-resurfacing) and prepares a Passage for rendering
 * (highlighted HTML + the answer-widget data for each target item).
 */
@Service
@RequiredArgsConstructor
public class ContentSelectionService {

    private final PassageRepository passageRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    public Passage getPassageById(Long id) {
        return passageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passage not found"));
    }

    public Passage getNextPassage(User user) {
        // 1. Prioritize passages containing words the learner is due to review (spaced resurfacing).
        List<Passage> dueWordPassages = passageRepository.findPassagesWithDueWords(user.getId());
        if (!dueWordPassages.isEmpty()) {
            return dueWordPassages.get(0);
        }

        // 2. Otherwise pick by level (with stretch/comfort adjustment) among unseen passages.
        LevelType targetLevel = resolveTargetLevel(user);
        List<Passage> unseen = passageRepository.findUnseenByLevel(
                user.getId(), targetLevel, ContentType.STORY, LocalDateTime.now().minusDays(3));
        if (!unseen.isEmpty()) {
            Collections.shuffle(unseen);
            return unseen.get(0);
        }

        // 3. Fall back to any passage at that level (allow repeats if the bank is small).
        List<Passage> anyAtLevel = passageRepository.findByLevelAndContentType(targetLevel, ContentType.STORY);
        if (!anyAtLevel.isEmpty()) {
            Collections.shuffle(anyAtLevel);
            return anyAtLevel.get(0);
        }

        // 4. Last resort: any story passage at all.
        List<Passage> any = passageRepository.findByContentType(ContentType.STORY);
        if (any.isEmpty()) {
            throw new ResourceNotFoundException("No passages available yet. Please ask an admin to add content.");
        }
        Collections.shuffle(any);
        return any.get(0);
    }

    public List<Passage> getDailyFeed(User user) {
        List<Passage> feed = passageRepository.findByLevelAndContentType(user.getCurrentLevel(), ContentType.MICRO);
        Collections.shuffle(feed);
        return feed.stream().limit(5).collect(Collectors.toList());
    }

    private LevelType resolveTargetLevel(User user) {
        LevelType base = user.getCurrentLevel();
        UserPreference pref = userPreferenceRepository.findByUserId(user.getId()).orElse(null);
        if (pref != null && pref.isStretchMode()) {
            return switch (base) {
                case BEGINNER -> LevelType.INTERMEDIATE;
                case INTERMEDIATE -> LevelType.ADVANCED;
                case ADVANCED -> LevelType.ADVANCED;
            };
        }
        return base;
    }

    /** Wraps every occurrence of each TargetItem's surface text in a clickable, data-tagged span. */
    public String buildHighlightedHtml(Passage passage) {
        String html = escapeHtml(passage.getContentText());
        for (TargetItem item : passage.getTargetItems()) {
            String word = Pattern.quote(escapeHtml(item.getSurfaceText()));
            Pattern pattern = Pattern.compile("(?i)\\b" + word + "\\b");
            Matcher matcher = pattern.matcher(html);
            StringBuilder sb = new StringBuilder();
            boolean replacedOnce = false;
            while (matcher.find()) {
                if (replacedOnce) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group()));
                    continue;
                }
                String replacement = "<span class=\"target-word\" data-item-id=\"" + item.getId() + "\">"
                        + matcher.group() + "</span>";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                replacedOnce = true;
            }
            matcher.appendTail(sb);
            html = sb.toString();
        }
        return html;
    }

    public List<TargetItemView> buildTargetItemViews(Passage passage) {
        List<TargetItemView> views = new ArrayList<>();
        for (TargetItem item : passage.getTargetItems()) {
            List<OptionChoice> options = new ArrayList<>();
            if (item.getAnswerMode() != com.contextenglish.entity.enums.AnswerMode.FREE_TEXT) {
                for (AcceptableAnswer a : item.getAcceptableAnswers()) {
                    if (a.isRequired()) {
                        options.add(new OptionChoice("A" + a.getId(), a.getAnswerText()));
                    }
                }
                for (DistractorOption d : item.getDistractorOptions()) {
                    options.add(new OptionChoice("D" + d.getId(), d.getOptionText()));
                }
                Collections.shuffle(options);
            }
            views.add(new TargetItemView(
                    item.getId(), item.getSurfaceText(), item.getItemType(),
                    item.getAnswerMode(), item.getHintText(), options));
        }
        return views;
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
