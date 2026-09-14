package com.contextenglish.controller.web;

import com.contextenglish.entity.User;
import com.contextenglish.entity.UserAchievement;
import com.contextenglish.repository.AchievementRepository;
import com.contextenglish.repository.UserAchievementRepository;
import com.contextenglish.repository.UserWordProgressRepository;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.ProgressService;
import com.contextenglish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProgressController {

    private final UserService userService;
    private final ProgressService progressService;
    private final UserWordProgressRepository userWordProgressRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRepository achievementRepository;

    @GetMapping("/progress")
    public String progressPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = userService.getById(principal.getUserId());
        model.addAttribute("user", user);
        model.addAttribute("summary", progressService.getSummary(user));
        model.addAttribute("weakAreas", progressService.getWeakAreas(user));
        return "progress";
    }

    @GetMapping("/vocabulary")
    public String vocabularyPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        model.addAttribute("words",
                userWordProgressRepository.findByUserIdOrderByMasteryLevelAsc(principal.getUserId()));
        return "vocabulary";
    }

    @GetMapping("/achievements")
    public String achievementsPage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        List<UserAchievement> earned =
                userAchievementRepository.findByUserIdOrderByEarnedAtDesc(principal.getUserId());
        java.util.Set<Long> earnedIds = earned.stream()
                .map(ua -> ua.getAchievement().getId()).collect(java.util.stream.Collectors.toSet());

        model.addAttribute("earned", earned);
        model.addAttribute("earnedIds", earnedIds);
        model.addAttribute("allAchievements", achievementRepository.findAll());
        return "achievements";
    }
}
