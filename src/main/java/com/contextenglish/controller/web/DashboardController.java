package com.contextenglish.controller.web;

import com.contextenglish.entity.User;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.ProgressService;
import com.contextenglish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ProgressService progressService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = userService.getById(principal.getUserId());
        if (!user.isOnboardingComplete()) {
            return "redirect:/onboarding/preferences";
        }
        model.addAttribute("user", user);
        model.addAttribute("summary", progressService.getSummary(user));
        return "dashboard";
    }
}
