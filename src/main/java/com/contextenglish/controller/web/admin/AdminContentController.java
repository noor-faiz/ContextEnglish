package com.contextenglish.controller.web.admin;

import com.contextenglish.entity.Passage;
import com.contextenglish.service.AdminContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping("/admin/content")
    public String listContent(Model model) {
        model.addAttribute("passages", adminContentService.getAllPassages());
        return "admin/content-list";
    }

    @GetMapping("/admin/content/new")
    public String newContent(Model model) {
        model.addAttribute("passageId", null);
        model.addAttribute("passage", null);
        return "admin/content-edit";
    }

    @GetMapping("/admin/content/edit/{id}")
    public String editContent(@PathVariable Long id, Model model) {
        Passage passage = adminContentService.getPassageById(id);
        model.addAttribute("passageId", id);
        model.addAttribute("passage", passage);
        return "admin/content-edit";
    }

    @PostMapping("/admin/content/{id}/delete")
    public String deleteContent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminContentService.deletePassage(id);
        redirectAttributes.addFlashAttribute("deletedOk", true);
        return "redirect:/admin/content";
    }
}
