package com.contextenglish.controller.api;

import com.contextenglish.dto.response.ProgressSummaryResponse;
import com.contextenglish.dto.response.WeakAreaResponse;
import com.contextenglish.entity.User;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.ProgressService;
import com.contextenglish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressApiController {

    private final ProgressService progressService;
    private final UserService userService;

    @GetMapping("/summary")
    public ProgressSummaryResponse summary(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userService.getById(principal.getUserId());
        return progressService.getSummary(user);
    }

    @GetMapping("/weak-areas")
    public List<WeakAreaResponse> weakAreas(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userService.getById(principal.getUserId());
        return progressService.getWeakAreas(user);
    }
}
