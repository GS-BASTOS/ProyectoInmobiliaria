package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.Property;
import com.inmobiliaria.app.repo.ClientPropertyInteractionRepository;
import com.inmobiliaria.app.repo.PropertyRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PropertyCatalogController {

    private final PropertyRepository propertyRepository;
    private final ClientPropertyInteractionRepository interactionRepository;

    public PropertyCatalogController(PropertyRepository propertyRepository,
                                     ClientPropertyInteractionRepository interactionRepository) {
        this.propertyRepository     = propertyRepository;
        this.interactionRepository  = interactionRepository;
    }

    // ── Helper: construye el mapa id → nº interesados ────────
    private Map<Long, Long> buildInterestMap(List<Property> properties) {
        return properties.stream().collect(Collectors.toMap(
                Property::getId,
                p -> interactionRepository.countByPropertyId(p.getId())
        ));
    }

    @GetMapping("/inmuebles")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false, defaultValue = "ALL") String soldFilter,
                       Model model) {
        List<Property> all = propertyRepository.findAllByOrderByPropertyCodeAsc();

        if (q != null && !q.isBlank()) {
            String lq = q.trim().toLowerCase();
            all = all.stream().filter(p ->
                    (p.getPropertyCode()  != null && p.getPropertyCode().toLowerCase().contains(lq)) ||
                    (p.getAddress()       != null && p.getAddress().toLowerCase().contains(lq))      ||
                    (p.getMunicipality()  != null && p.getMunicipality().toLowerCase().contains(lq)) ||
                    (p.getPropertyType()  != null && p.getPropertyType().toLowerCase().contains(lq))
            ).collect(Collectors.toList());
        }

        if ("SOLD".equals(soldFilter)) {
            all = all.stream().filter(Property::isSold).collect(Collectors.toList());
        } else if ("ACTIVE".equals(soldFilter)) {
            all = all.stream().filter(p -> !p.isSold()).collect(Collectors.toList());
        }

        model.addAttribute("properties",        all);
        model.addAttribute("interestCountById", buildInterestMap(all));
        model.addAttribute("form",              new Property());
        model.addAttribute("q",                 q);
        model.addAttribute("soldFilter",        soldFilter);
        return "property_catalog";
    }

    @PostMapping("/inmuebles")
    public String create(@Valid @ModelAttribute("form") Property form,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            List<Property> all = propertyRepository.findAllByOrderByPropertyCodeAsc();
            model.addAttribute("properties",        all);
            model.addAttribute("interestCountById", buildInterestMap(all));
            model.addAttribute("q",                 null);
            model.addAttribute("soldFilter",        "ALL");
            return "property_catalog";
        }
        propertyRepository.save(form);
        return "redirect:/inmuebles";
    }

    @GetMapping("/inmuebles/{id}/editar")
    public String editForm(@PathVariable Long id, Model model) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<Property> all = propertyRepository.findAllByOrderByPropertyCodeAsc();
        model.addAttribute("properties",        all);
        model.addAttribute("interestCountById", buildInterestMap(all));
        model.addAttribute("form",              p);
        model.addAttribute("editing",           true);
        model.addAttribute("q",                 null);
        model.addAttribute("soldFilter",        "ALL");
        return "property_catalog";
    }

    @PostMapping("/inmuebles/{id}/editar")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") Property form,
                         BindingResult br, Model model) {
        if (br.hasErrors()) {
            List<Property> all = propertyRepository.findAllByOrderByPropertyCodeAsc();
            model.addAttribute("properties",        all);
            model.addAttribute("interestCountById", buildInterestMap(all));
            model.addAttribute("editing",           true);
            model.addAttribute("q",                 null);
            model.addAttribute("soldFilter",        "ALL");
            return "property_catalog";
        }
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        existing.setPropertyCode(form.getPropertyCode());
        existing.setPropertyType(form.getPropertyType());
        existing.setAddress(form.getAddress());
        existing.setMunicipality(form.getMunicipality());
        existing.setNotes(form.getNotes());
        propertyRepository.save(existing);
        return "redirect:/inmuebles";
    }

    @PostMapping("/inmuebles/{id}/eliminar")
    public String delete(@PathVariable Long id) {
        propertyRepository.deleteById(id);
        return "redirect:/inmuebles";
    }

    @PostMapping("/inmuebles/{id}/vendido")
    @ResponseBody
    public void toggleVendido(@PathVariable Long id,
                               @RequestParam("sold") boolean sold) {
        Property p = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        p.setSold(sold);
        propertyRepository.save(p);
    }

    @GetMapping("/api/inmuebles")
    @ResponseBody
    public List<Property> apiList() {
        return propertyRepository.findAllByOrderByPropertyCodeAsc()
                .stream()
                .filter(p -> !p.isSold())
                .collect(Collectors.toList());
    }
}
