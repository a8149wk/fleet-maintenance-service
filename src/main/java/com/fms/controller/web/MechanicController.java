package com.fms.controller.web;

import com.fms.entity.Mechanic;
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
@RequestMapping("/mechanics")
@RequiredArgsConstructor
public class MechanicController {

    private final MechanicService mechanicService;
    private final WorkshopService workshopService;

    @GetMapping
    public String list(@RequestParam(required = false) Long workshopId,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Mechanic> mechanics = mechanicService.search(workshopId, search,
                PageRequest.of(page, 20, Sort.by("fullName").ascending()));
        model.addAttribute("mechanics", mechanics);
        model.addAttribute("workshops", workshopService.findActive());
        return "mechanic/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("mechanic", new Mechanic());
        model.addAttribute("workshops", workshopService.findActive());
        return "mechanic/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("mechanic") Mechanic mechanic,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            model.addAttribute("workshops", workshopService.findActive());
            return "mechanic/create";
        }
        mechanicService.save(mechanic);
        attrs.addFlashAttribute("message", "Mechanic saved");
        return "redirect:/mechanics";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("mechanic", mechanicService.findById(id));
        return "mechanic/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("mechanic", mechanicService.findById(id));
        model.addAttribute("workshops", workshopService.findActive());
        return "mechanic/create";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("mechanic") Mechanic mechanic,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            model.addAttribute("workshops", workshopService.findActive());
            return "mechanic/create";
        }
        mechanic.setId(id);
        mechanicService.save(mechanic);
        attrs.addFlashAttribute("message", "Mechanic updated");
        return "redirect:/mechanics/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        mechanicService.delete(id);
        attrs.addFlashAttribute("message", "Mechanic deleted");
        return "redirect:/mechanics";
    }
}
