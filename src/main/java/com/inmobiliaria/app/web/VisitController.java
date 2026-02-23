package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.*;
import com.inmobiliaria.app.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class VisitController {

    private final VisitRepository visitRepository;
    private final ClientRepository clientRepository;
    private final ClientPhoneRepository clientPhoneRepository;
    private final ClientEmailRepository clientEmailRepository;
    private final PropertyRepository propertyRepository;
    private final ClientPropertyInteractionRepository interactionRepository;

    public VisitController(VisitRepository visitRepository,
                           ClientRepository clientRepository,
                           ClientPhoneRepository clientPhoneRepository,
                           ClientEmailRepository clientEmailRepository,
                           PropertyRepository propertyRepository,
                           ClientPropertyInteractionRepository interactionRepository) {
        this.visitRepository = visitRepository;
        this.clientRepository = clientRepository;
        this.clientPhoneRepository = clientPhoneRepository;
        this.clientEmailRepository = clientEmailRepository;
        this.propertyRepository = propertyRepository;
        this.interactionRepository = interactionRepository;
    }

    // ── GET /visitas/programadas ─────────────────────────────
    @GetMapping("/visitas/programadas")
    public String scheduled(Model model) {
        List<Visit> visits = visitRepository.findByStatusOrderByDateDescTimeAsc(VisitStatus.PROGRAMADA);
        enrichModel(model, visits);

        // DTO simple para el buscador JS — evita LazyInitializationException
        List<Map<String, Object>> propertiesDto = propertyRepository
                .findAllByOrderByPropertyCodeAsc()
                .stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id",           p.getId());
                    m.put("propertyCode", p.getPropertyCode());
                    m.put("municipality", p.getMunicipality());
                    return m;
                })
                .collect(Collectors.toList());

        model.addAttribute("allProperties", propertiesDto);
        return "visits_scheduled";
    }

    // ── GET /visitas/realizadas ──────────────────────────────
    @GetMapping("/visitas/realizadas")
    public String done(Model model) {
        List<Visit> visits = visitRepository.findByStatusOrderByDateDescTimeAsc(VisitStatus.REALIZADA);
        enrichModel(model, visits);
        return "visits_done";
    }

    // ── GET /visitas/clientes-por-inmueble ───────────────────
    @GetMapping("/visitas/clientes-por-inmueble")
    @ResponseBody
    public List<Map<String, Object>> clientesPorInmueble(
            @RequestParam("propertyId") Long propertyId) {

        return interactionRepository
                .findByProperty_IdOrderByContactDateDesc(propertyId)
                .stream()
                .map(i -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("clientId",      i.getClient().getId());
                    m.put("clientName",    i.getClient().getFullName());
                    m.put("interactionId", i.getId());
                    return m;
                })
                .collect(Collectors.collectingAndThen(
                    Collectors.toMap(
                        m -> (Long) m.get("clientId"),
                        m -> m,
                        (a, b) -> a,
                        LinkedHashMap::new
                    ),
                    map -> new ArrayList<>(map.values())
                ));
    }

    // ── POST /visitas/crear ──────────────────────────────────
    @PostMapping("/visitas/crear")
    public String create(@RequestParam("interactionId") Long interactionId,
                         @RequestParam("visitAt") String visitAt,
                         @RequestParam(value = "notes", required = false) String notes) {

        ClientPropertyInteraction interaction = interactionRepository.findById(interactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        LocalDateTime dateTime = LocalDateTime.parse(visitAt);

        Visit visit = new Visit();
        visit.setClient(interaction.getClient());
        visit.setProperty(interaction.getProperty());
        visit.setVisitAt(dateTime);
        visit.setStatus(VisitStatus.PROGRAMADA);
        visit.setNotes(notes == null ? "" : notes.trim());
        visitRepository.save(visit);

        interaction.setStatus(InterestStatus.AZUL_VISITA_PROGRAMADA);
        interactionRepository.save(interaction);

        return "redirect:/visitas/programadas";
    }

    // ── POST /visitas/crear-directo ──────────────────────────
    @PostMapping("/visitas/crear-directo")
    public String createDirect(@RequestParam("clientId")   Long clientId,
                               @RequestParam("propertyId") Long propertyId,
                               @RequestParam("visitAt")    String visitAt,
                               @RequestParam(value = "notes", required = false) String notes) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Visit visit = new Visit();
        visit.setClient(client);
        visit.setProperty(property);
        visit.setVisitAt(LocalDateTime.parse(visitAt));
        visit.setStatus(VisitStatus.PROGRAMADA);
        visit.setNotes(notes == null ? "" : notes.trim());
        visitRepository.save(visit);

        return "redirect:/visitas/programadas";
    }

    // ── POST /visitas/{id}/realizada ─────────────────────────
    @PostMapping("/visitas/{id}/realizada")
    @ResponseBody
    public void markDone(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        visit.setStatus(VisitStatus.REALIZADA);
        visitRepository.save(visit);

        interactionRepository
                .findByClientIdWithPropertyOrderByContactDateDesc(visit.getClient().getId())
                .stream()
                .filter(i -> i.getProperty().getId().equals(visit.getProperty().getId()))
                .findFirst()
                .ifPresent(i -> {
                    i.setStatus(InterestStatus.AZUL_VISITA_REALIZADA);
                    interactionRepository.save(i);
                });
    }

    // ── Helper ───────────────────────────────────────────────
    private void enrichModel(Model model, List<Visit> visits) {
        List<Long> clientIds = visits.stream()
                .map(v -> v.getClient().getId())
                .distinct()
                .toList();

        Map<Long, List<ClientPhone>> phones = clientIds.isEmpty()
                ? Collections.emptyMap()
                : clientPhoneRepository
                        .findByClient_IdInOrderByClient_IdAscPositionAsc(clientIds)
                        .stream()
                        .collect(Collectors.groupingBy(p -> p.getClient().getId()));

        Map<Long, List<ClientEmail>> emails = clientIds.isEmpty()
                ? Collections.emptyMap()
                : clientEmailRepository
                        .findByClient_IdInOrderByClient_IdAscPositionAsc(clientIds)
                        .stream()
                        .collect(Collectors.groupingBy(e -> e.getClient().getId()));

        model.addAttribute("visits", visits);
        model.addAttribute("phonesByClientId", phones);
        model.addAttribute("emailsByClientId", emails);
    }
}
