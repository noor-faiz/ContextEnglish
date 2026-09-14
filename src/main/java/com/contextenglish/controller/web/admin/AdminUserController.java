package com.contextenglish.controller.web.admin;

import com.contextenglish.entity.enums.Role;
import com.contextenglish.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("users", adminUserService.getAllUsers());
        return "admin/user-list";
    }

    @PostMapping("/admin/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam Role role,
                              RedirectAttributes redirectAttributes) {
        try {
            adminUserService.changeRole(id, role);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("userError", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminUserService.deleteUser(id);
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("userError", ex.getMessage());
        }
        return "redirect:/admin/users";
    }
}
