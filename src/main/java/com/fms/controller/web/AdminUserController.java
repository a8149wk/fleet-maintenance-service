package com.fms.controller.web;

import com.fms.entity.User;
import com.fms.service.RoleService;
import com.fms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        Page<User> users = userService.search(q, PageRequest.of(page, 20));
        model.addAttribute("users", users);
        model.addAttribute("keyword", q);
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("selectedRoleIds", Set.of());
        return "admin/users/form";
    }

    @PostMapping
    public String create(@ModelAttribute("user") User user,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "roleIds", required = false) Set<Long> roleIds,
                         RedirectAttributes ra,
                         Model model) {
        try {
            userService.adminCreate(user, password, roleIds);
            ra.addFlashAttribute("message", "User created.");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("allRoles", roleService.findAll());
            model.addAttribute("selectedRoleIds", roleIds == null ? Set.of() : roleIds);
            return "admin/users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("selectedRoleIds",
                user.getRoles().stream().map(r -> r.getId()).toList());
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("user") User user,
                         @RequestParam(value = "password", required = false) String password,
                         @RequestParam(value = "roleIds", required = false) Set<Long> roleIds,
                         RedirectAttributes ra) {
        userService.adminUpdate(id, user, password, roleIds);
        ra.addFlashAttribute("message", "User updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        userService.toggleActive(id);
        ra.addFlashAttribute("message", "User status updated.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("message", "User deleted.");
        return "redirect:/admin/users";
    }
}
