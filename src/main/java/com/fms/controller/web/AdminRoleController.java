package com.fms.controller.web;

import com.fms.entity.Role;
import com.fms.service.MenuService;
import com.fms.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;
    private final MenuService menuService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("roles", roleService.findAll());
        return "admin/roles/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("role", new Role());
        return "admin/roles/form";
    }

    @PostMapping
    public String create(@ModelAttribute("role") Role role, RedirectAttributes ra, Model model) {
        try {
            roleService.create(role);
            ra.addFlashAttribute("message", "Role created.");
            return "redirect:/admin/roles";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/roles/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Role role = roleService.findByIdWithMenus(id);
        model.addAttribute("role", role);
        model.addAttribute("groupedMenus", menuService.findAllGroupedBySection());
        model.addAttribute("selectedMenuIds",
                role.getMenus().stream().map(m -> m.getId()).collect(Collectors.toSet()));
        return "admin/roles/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("role") Role form,
                         @RequestParam(value = "menuIds", required = false) Set<Long> menuIds,
                         RedirectAttributes ra) {
        roleService.update(id, form);
        menuService.updateRoleMenus(id, menuIds);
        ra.addFlashAttribute("message", "Role and menu mapping updated.");
        return "redirect:/admin/roles";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        roleService.delete(id);
        ra.addFlashAttribute("message", "Role deleted.");
        return "redirect:/admin/roles";
    }
}
