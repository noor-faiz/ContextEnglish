package com.contextenglish.controller.api;

import com.contextenglish.entity.Passage;
import com.contextenglish.entity.User;
import com.contextenglish.security.CustomUserDetails;
import com.contextenglish.service.ContentSelectionService;
import com.contextenglish.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedApiController {

    private final ContentSelectionService contentSelectionService;
    private final UserService userService;

    @GetMapping("/daily")
    public List<FeedCard> dailyFeed(@AuthenticationPrincipal CustomUserDetails principal) {
        User user = userService.getById(principal.getUserId());
        List<Passage> feed = contentSelectionService.getDailyFeed(user);
        return feed.stream()
                .map(p -> new FeedCard(p.getId(), p.getTitle(), p.getTopic()))
                .collect(Collectors.toList());
    }

    public record FeedCard(Long id, String title, String topic) {}
}
