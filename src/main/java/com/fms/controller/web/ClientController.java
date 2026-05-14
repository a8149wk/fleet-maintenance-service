package com.fms.controller.web;

import com.fms.entity.Client;
import com.fms.service.ClientService;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Client> clients = clientService.search(search,
                PageRequest.of(page, 20, Sort.by("name").ascending()));
        model.addAttribute("clients", clients);
        return "client/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("client", new Client());
        return "client/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("client") Client client,
                         BindingResult br,
                         RedirectAttributes attrs) {
        if (br.hasErrors()) {
            return "client/create";
        }
        clientService.save(client);
        attrs.addFlashAttribute("message", "Client saved successfully");
        return "redirect:/clients";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.findById(id));
        return "client/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.findById(id));
        return "client/create";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("client") Client client,
                         BindingResult br,
                         RedirectAttributes attrs) {
        if (br.hasErrors()) {
            return "client/create";
        }
        client.setId(id);
        clientService.save(client);
        attrs.addFlashAttribute("message", "Client updated");
        return "redirect:/clients/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        clientService.delete(id);
        attrs.addFlashAttribute("message", "Client deleted");
        return "redirect:/clients";
    }
}
