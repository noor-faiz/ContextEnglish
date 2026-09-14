package com.contextenglish.controller.web;

import com.contextenglish.dto.request.PreferenceUpdateRequest;
import com.contextenglish.entity.AssessmentQuestion;
import com.contextenglish.entity.User;
import com.contextenglish.entity.enums.LevelType;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.AssessmentService;
import com.contextenglish.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OnboardingController {

    private final UserService userService;
    private final AssessmentService assessmentService;

    @GetMapping("/onboarding/preferences")
    public String preferencesPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("preferenceUpdateRequest", new PreferenceUpdateRequest());
        return "onboarding/preferences";
    }

    @PostMapping("/onboarding/preferences")
    public String savePreferences(@AuthenticationPrincipal CustomUserDetails principal,
                                   @ModelAttribute PreferenceUpdateRequest request) {
        userService.updatePreferences(principal.getUserId(), request);
        return "redirect:/onboarding/assessment";
    }

    @GetMapping("/onboarding/assessment")
    public String assessmentPage(Model model) {
        List<AssessmentQuestion> questions = assessmentService.getAllQuestions();
        model.addAttribute("questions", questions);
        return "onboarding/assessment";
    }

    @PostMapping("/onboarding/assessment")
    public String submitAssessment(@AuthenticationPrincipal CustomUserDetails principal,
                                    HttpServletRequest request,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.getById(principal.getUserId());
        List<AssessmentQuestion> questions = assessmentService.getAllQuestions();

        Map<Long, Integer> answers = new HashMap<>();
        for (AssessmentQuestion q : questions) {
            String param = request.getParameter("q_" + q.getId());
            if (param != null && !param.isBlank()) {
                answers.put(q.getId(), Integer.parseInt(param));
            }
        }

        LevelType assigned = assessmentService.scoreAndAssignLevel(user, answers);
        redirectAttributes.addFlashAttribute("assignedLevel", assigned);
        return "redirect:/dashboard";
    }
}
