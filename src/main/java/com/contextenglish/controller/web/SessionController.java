package com.contextenglish.controller.web;

import com.contextenglish.entity.Passage;
import com.contextenglish.entity.SessionAttempt;
import com.contextenglish.entity.User;
import com.contextenglish.entity.Achievement;
import com.contextenglish.exception.ResourceNotFoundException;
import com.contextenglish.repository.SessionAttemptRepository;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.ContentSelectionService;
import com.contextenglish.service.ProgressService;
import com.contextenglish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SessionController {

    private final UserService userService;
    private final ContentSelectionService contentSelectionService;
    private final ProgressService progressService;
    private final SessionAttemptRepository sessionAttemptRepository;

    @GetMapping("/session/start")
    public String startNewSession(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userService.getById(principal.getUserId());
        Passage passage = contentSelectionService.getNextPassage(user);
        SessionAttempt attempt = progressService.startSession(user, passage);
        return "redirect:/session/" + attempt.getId();
    }

    /** Used by the daily micro-feed cards, which link to a specific passage rather than "whatever's next". */
    @GetMapping("/session/start/{passageId}")
    public String startSpecificSession(@PathVariable Long passageId,
                                        @AuthenticationPrincipal CustomUserDetails principal) {
        User user = userService.getById(principal.getUserId());
        Passage passage = contentSelectionService.getPassageById(passageId);
        SessionAttempt attempt = progressService.startSession(user, passage);
        return "redirect:/session/" + attempt.getId();
    }

    @GetMapping("/session/{attemptId}")
    public String viewSession(@PathVariable Long attemptId,
                               @AuthenticationPrincipal CustomUserDetails principal,
                               Model model) {
        SessionAttempt attempt = sessionAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!attempt.getUser().getId().equals(principal.getUserId())) {
            throw new ResourceNotFoundException("Session not found");
        }
        Passage passage = attempt.getPassage();

        model.addAttribute("attempt", attempt);
        model.addAttribute("passage", passage);
        model.addAttribute("highlightedHtml", contentSelectionService.buildHighlightedHtml(passage));
        model.addAttribute("targetItems", contentSelectionService.buildTargetItemViews(passage));
        return "session";
    }

    @PostMapping("/session/{attemptId}/complete")
    public String completeSession(@PathVariable Long attemptId,
                                   @AuthenticationPrincipal CustomUserDetails principal,
                                   RedirectAttributes redirectAttributes) {
        SessionAttempt attempt = sessionAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!attempt.getUser().getId().equals(principal.getUserId())) {
            throw new ResourceNotFoundException("Session not found");
        }
        User user = userService.getById(principal.getUserId());
        List<Achievement> newAchievements = progressService.completeSession(user, attempt);
        redirectAttributes.addFlashAttribute("newAchievements", newAchievements);
        return "redirect:/session/" + attemptId + "/result";
    }

    @GetMapping("/session/{attemptId}/result")
    public String sessionResult(@PathVariable Long attemptId,
                                 @AuthenticationPrincipal CustomUserDetails principal,
                                 Model model) {
        SessionAttempt attempt = sessionAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!attempt.getUser().getId().equals(principal.getUserId())) {
            throw new ResourceNotFoundException("Session not found");
        }
        model.addAttribute("attempt", attempt);
        return "session-result";
    }
}
