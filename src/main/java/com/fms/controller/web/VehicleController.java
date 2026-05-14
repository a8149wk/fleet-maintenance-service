package com.fms.controller.web;

import com.fms.entity.Vehicle;
import com.fms.enums.FuelType;
import com.fms.enums.TransmissionType;
import com.fms.repository.WorkOrderRepository;
import com.fms.service.ClientService;
import com.fms.service.VehicleService;
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
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final ClientService clientService;
    private final WorkOrderRepository workOrderRepository;

    @GetMapping
    public String list(@RequestParam(required = false) Long clientId,
                       @RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Page<Vehicle> vehicles = vehicleService.search(clientId, search,
                PageRequest.of(page, 20, Sort.by("licensePlate").ascending()));
        model.addAttribute("vehicles", vehicles);
        model.addAttribute("clients", clientService.findActive());
        return "vehicle/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        prepareForm(model);
        return "vehicle/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("vehicle") Vehicle vehicle,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            prepareForm(model);
            return "vehicle/create";
        }
        vehicleService.save(vehicle);
        attrs.addFlashAttribute("message", "Vehicle saved");
        return "redirect:/vehicles";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("workOrders", workOrderRepository.findByVehicleId(id));
        return "vehicle/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("vehicle", vehicleService.findById(id));
        prepareForm(model);
        return "vehicle/create";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("vehicle") Vehicle vehicle,
                         BindingResult br, Model model, RedirectAttributes attrs) {
        if (br.hasErrors()) {
            prepareForm(model);
            return "vehicle/create";
        }
        vehicle.setId(id);
        vehicleService.save(vehicle);
        attrs.addFlashAttribute("message", "Vehicle updated");
        return "redirect:/vehicles/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attrs) {
        vehicleService.delete(id);
        attrs.addFlashAttribute("message", "Vehicle deleted");
        return "redirect:/vehicles";
    }

    @GetMapping("/{id}/history")
    public String maintenanceHistory(@PathVariable Long id, Model model) {
        Vehicle vehicle = vehicleService.findById(id);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("workOrders", workOrderRepository.findByVehicleId(id));
        return "vehicle/maintenance-history";
    }

    private void prepareForm(Model model) {
        model.addAttribute("clients", clientService.findActive());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("transmissionTypes", TransmissionType.values());
    }
}
