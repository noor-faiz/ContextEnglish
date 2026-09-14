package com.contextenglish.service;

import com.contextenglish.dto.request.AcceptableAnswerRequest;
import com.contextenglish.dto.request.PassageCreateRequest;
import com.contextenglish.dto.request.TargetItemRequest;
import com.contextenglish.entity.*;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.PassageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin CRUD for learning content. A passage is always saved together with
 * all of its target items / acceptable answers / distractors / explanations
 * in one call — on edit, existing target items are replaced wholesale, which
 * keeps the admin form (and this service) simple for a content model this size.
 */
@Service
@RequiredArgsConstructor
public class AdminContentService {

    private final PassageRepository passageRepository;

    public List<Passage> getAllPassages() {
        return passageRepository.findAll();
    }

    public Passage getPassageById(Long id) {
        return passageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Passage not found"));
    }

    @Transactional
    public Passage createPassage(PassageCreateRequest request) {
        Passage passage = new Passage();
        applyRequest(passage, request);
        return passageRepository.save(passage);
    }

    @Transactional
    public Passage updatePassage(Long id, PassageCreateRequest request) {
        Passage passage = getPassageById(id);
        applyRequest(passage, request);
        return passageRepository.save(passage);
    }

    @Transactional
    public void deletePassage(Long id) {
        Passage passage = getPassageById(id);
        passageRepository.delete(passage);
    }

    private void applyRequest(Passage passage, PassageCreateRequest request) {
        passage.setTitle(request.getTitle());
        passage.setContentText(request.getContentText());
        passage.setLevel(request.getLevel());
        passage.setTopic(request.getTopic());
        passage.setContentType(request.getContentType());

        List<TargetItem> items = new ArrayList<>();
        for (TargetItemRequest tiReq : request.getTargetItems()) {
            TargetItem item = new TargetItem();
            item.setPassage(passage);
            item.setSurfaceText(tiReq.getSurfaceText());
            item.setItemType(tiReq.getItemType());
            item.setAnswerMode(tiReq.getAnswerMode());
            item.setHintText(tiReq.getHintText());

            List<AcceptableAnswer> answers = new ArrayList<>();
            for (AcceptableAnswerRequest aReq : tiReq.getAcceptableAnswers()) {
                AcceptableAnswer answer = new AcceptableAnswer();
                answer.setTargetItem(item);
                answer.setAnswerText(aReq.getAnswerText());
                answer.setAnswerType(aReq.getAnswerType());
                answer.setRequired(aReq.isRequired());
                answer.setPointsWeight(aReq.getPointsWeight());
                answers.add(answer);
            }
            item.setAcceptableAnswers(answers);

            List<DistractorOption> distractors = new ArrayList<>();
            for (String text : tiReq.getDistractorOptions()) {
                if (text == null || text.isBlank()) continue;
                DistractorOption d = new DistractorOption();
                d.setTargetItem(item);
                d.setOptionText(text);
                distractors.add(d);
            }
            item.setDistractorOptions(distractors);

            if (tiReq.getExplanationText() != null && !tiReq.getExplanationText().isBlank()) {
                Explanation explanation = new Explanation();
                explanation.setTargetItem(item);
                explanation.setExplanationText(tiReq.getExplanationText());
                explanation.setExplanationNative(tiReq.getExplanationNative());
                item.setExplanation(explanation);
            }

            items.add(item);
        }
        // Clear + re-populate the *existing* managed collection in place (rather than
        // assigning a new List via a setter) so Hibernate's orphanRemoval correctly
        // deletes any children that were removed, both on create and on edit.
        passage.getTargetItems().clear();
        passage.getTargetItems().addAll(items);
    }
}
