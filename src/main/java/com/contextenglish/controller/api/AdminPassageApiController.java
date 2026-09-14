package com.contextenglish.controller.api;

import com.contextenglish.dto.request.PassageCreateRequest;
import com.contextenglish.entity.Passage;
import com.contextenglish.service.AdminContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/passages")
@RequiredArgsConstructor
public class AdminPassageApiController {

    private final AdminContentService adminContentService;

    @PostMapping
    public ResponseEntity<Passage> create(@Valid @RequestBody PassageCreateRequest request) {
        return ResponseEntity.ok(adminContentService.createPassage(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Passage> update(@PathVariable Long id, @Valid @RequestBody PassageCreateRequest request) {
        return ResponseEntity.ok(adminContentService.updatePassage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminContentService.deletePassage(id);
        return ResponseEntity.noContent().build();
    }
}
