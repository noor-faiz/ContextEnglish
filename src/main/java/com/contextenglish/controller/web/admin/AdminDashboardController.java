package com.contextenglish.controller.web.admin;

import com.contextenglish.repository.PassageRepository;
import com.contextenglish.repository.SessionAttemptRepository;
import com.contextenglish.repository.UserRepository;
import com.contextenglish.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final PassageRepository passageRepository;
    private final SessionAttemptRepository sessionAttemptRepository;

    @GetMapping("/admin")
    public String adminHome(Model model) {
        long totalLearners = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.LEARNER).count();
        long totalPassages = passageRepository.findAll().size();
        long totalSessionsCompleted = sessionAttemptRepository.findAll().stream()
                .filter(sa -> sa.getCompletedAt() != null).count();

        model.addAttribute("totalLearners", totalLearners);
        model.addAttribute("totalPassages", totalPassages);
        model.addAttribute("totalSessionsCompleted", totalSessionsCompleted);
        return "admin/analytics";
    }
}
