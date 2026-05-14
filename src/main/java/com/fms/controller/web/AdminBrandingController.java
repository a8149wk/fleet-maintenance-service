package com.fms.controller.web;

import com.fms.service.BrandingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/branding")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminBrandingController {

    private final BrandingService brandingService;

    @GetMapping
    public String show(Model model) {
        model.addAttribute("hasCustomLogo", brandingService.hasCustomLogo());
        return "admin/branding/index";
    }

    @PostMapping
    public String upload(@RequestParam("logo") MultipartFile logo, RedirectAttributes ra) {
        try {
            brandingService.updateLogo(logo);
            ra.addFlashAttribute("message", "Logo updated successfully.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        } catch (IOException ex) {
            ra.addFlashAttribute("error", "Could not save logo: " + ex.getMessage());
        }
        return "redirect:/admin/branding";
    }

    @PostMapping("/reset")
    public String reset(RedirectAttributes ra) {
        brandingService.resetLogo();
        ra.addFlashAttribute("message", "Logo reset to the bundled default.");
        return "redirect:/admin/branding";
    }
}
