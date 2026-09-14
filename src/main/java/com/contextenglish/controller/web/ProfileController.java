package com.contextenglish.controller.web;

import com.contextenglish.dto.request.ChangePasswordRequest;
import com.contextenglish.dto.request.PreferenceUpdateRequest;
import com.contextenglish.entity.User;
import com.contextenglish.entity.UserPreference;
import com.contextenglish.repository.UserPreferenceRepository;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final UserPreferenceRepository userPreferenceRepository;

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        User user = userService.getById(principal.getUserId());
        UserPreference preference = userPreferenceRepository.findByUserId(user.getId()).orElse(new UserPreference());
        model.addAttribute("user", user);
        model.addAttribute("preferenceUpdateRequest", toRequest(preference));
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "profile";
    }

    @PostMapping("/profile/preferences")
    public String updatePreferences(@AuthenticationPrincipal CustomUserDetails principal,
                                     @ModelAttribute PreferenceUpdateRequest request,
                                     RedirectAttributes redirectAttributes) {
        userService.updatePreferences(principal.getUserId(), request);
        redirectAttributes.addFlashAttribute("preferencesSaved", true);
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails principal,
                                  @ModelAttribute ChangePasswordRequest request,
                                  RedirectAttributes redirectAttributes) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "New password must be at least 6 characters.");
            return "redirect:/profile";
        }
        try {
            userService.changePassword(principal.getUserId(), request);
            redirectAttributes.addFlashAttribute("passwordChanged", true);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("passwordError", ex.getMessage());
        }
        return "redirect:/profile";
    }

    private PreferenceUpdateRequest toRequest(UserPreference pref) {
        PreferenceUpdateRequest r = new PreferenceUpdateRequest();
        r.setNativeLanguage(pref.getNativeLanguage());
        r.setDailyGoalMinutes(pref.getDailyGoalMinutes());
        r.setStretchMode(pref.isStretchMode());
        r.setTopicsOfInterest(pref.getTopicsOfInterest());
        return r;
    }
}
