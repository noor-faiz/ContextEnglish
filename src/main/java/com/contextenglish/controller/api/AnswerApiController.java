package com.contextenglish.controller.api;

import com.contextenglish.dto.request.AnswerSubmitRequest;
import com.contextenglish.dto.response.AnswerResultResponse;
import com.contextenglish.entity.SessionAttempt;
import com.contextenglish.entity.TargetItem;
import com.contextenglish.entity.User;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.SessionAttemptRepository;
import com.contextenglish.repository.TargetItemRepository;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.EvaluationService;
import com.contextenglish.service.ProgressService;
import com.contextenglish.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerApiController {

    private final EvaluationService evaluationService;
    private final ProgressService progressService;
    private final TargetItemRepository targetItemRepository;
    private final SessionAttemptRepository sessionAttemptRepository;
    private final UserService userService;

    @PostMapping("/submit")
    public ResponseEntity<AnswerResultResponse> submit(@Valid @RequestBody AnswerSubmitRequest request,
                                                        @AuthenticationPrincipal CustomUserDetails principal) {
        TargetItem item = targetItemRepository.findById(request.getTargetItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Target item not found"));

        SessionAttempt attempt = sessionAttemptRepository.findById(request.getSessionAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!attempt.getUser().getId().equals(principal.getUserId())) {
            throw new ResourceNotFoundException("Session not found");
        }

        User user = userService.getById(principal.getUserId());
        AnswerResultResponse result = evaluationService.evaluate(item, request);
        progressService.recordAnswer(user, attempt, item, request, result);

        return ResponseEntity.ok(result);
    }
}
