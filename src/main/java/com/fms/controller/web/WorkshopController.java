package com.fms.controller.web;

import com.fms.entity.Workshop;
import com.fms.enums.WorkshopType;
import com.fms.service.MechanicService;
import com.fms.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;
    private final MechanicService mechanicService;

    @GetMapping
    public String list(@RequestParam(required = false) WorkshopType type,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Workshop> workshops = workshopService.search(type, search,
                PageRequest.of(page, 20, Sort.by("name").ascending()));
        model.addAttribute("workshops", workshops);
        model.addAttribute("types", WorkshopType.values());
        return "workshop/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("workshop", new Workshop());
        model.addAttribute("types", WorkshopType.values());
        return "workshop/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("workshop") Workshop workshop,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            model.addAttribute("types", WorkshopType.values());
            return "workshop/create";
        }
        workshopService.save(workshop);
        attrs.addFlashAttribute("message", "Workshop saved");
        return "redirect:/workshops";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Workshop workshop = workshopService.findById(id);
        model.addAttribute("workshop", workshop);
        model.addAttribute("mechanics", mechanicService.findByWorkshop(id));
        return "workshop/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("workshop", workshopService.findById(id));
        model.addAttribute("types", WorkshopType.values());
        return "workshop/create";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("workshop") Workshop workshop,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            model.addAttribute("types", WorkshopType.values());
            return "workshop/create";
        }
        workshop.setId(id);
        workshopService.save(workshop);
        attrs.addFlashAttribute("message", "Workshop updated");
        return "redirect:/workshops/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        workshopService.delete(id);
        attrs.addFlashAttribute("message", "Workshop deleted");
        return "redirect:/workshops";
    }
}
