package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.ClientEmail;
import com.inmobiliaria.app.domain.ClientPhone;
import com.inmobiliaria.app.domain.ClientPropertyInteraction;
import com.inmobiliaria.app.domain.ContactChannel;
import com.inmobiliaria.app.domain.InterestStatus;
import com.inmobiliaria.app.repo.ClientEmailRepository;
import com.inmobiliaria.app.repo.ClientPhoneRepository;
import com.inmobiliaria.app.repo.ClientPropertyInteractionRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class InteractionController {

    private final ClientPropertyInteractionRepository interactionRepository;
    private final ClientPhoneRepository clientPhoneRepository;
    private final ClientEmailRepository clientEmailRepository;

    public InteractionController(ClientPropertyInteractionRepository interactionRepository,
                                 ClientPhoneRepository clientPhoneRepository,
                                 ClientEmailRepository clientEmailRepository) {
        this.interactionRepository = interactionRepository;
        this.clientPhoneRepository = clientPhoneRepository;
        this.clientEmailRepository = clientEmailRepository;
    }

    @GetMapping("/interesados")
    public String interactions(
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) ContactChannel channel,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String searchField,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "false") boolean ndaOnly,
            Model model
    ) {
        // Convertir strings a enums
        List<InterestStatus> statusEnums = new ArrayList<>();
        if (statuses != null) {
            for (String s : statuses) {
                try { statusEnums.add(InterestStatus.valueOf(s)); }
                catch (IllegalArgumentException ignore) {}
            }
        }

        // Normalizar searchField
        String field = (searchField == null || searchField.isBlank()) ? "ALL" : searchField.trim().toUpperCase();

        List<ClientPropertyInteraction> items;
        if (statusEnums.isEmpty()) {
            items = interactionRepository.searchWithFilters(null, channel, "ALL".equals(field) ? q : null, from, to);
        } else if (statusEnums.size() == 1) {
            items = interactionRepository.searchWithFilters(statusEnums.get(0), channel, "ALL".equals(field) ? q : null, from, to);
        } else {
            items = interactionRepository.searchWithFilters(null, channel, "ALL".equals(field) ? q : null, from, to)
                    .stream()
                    .filter(i -> statusEnums.contains(i.getStatus()))
                    .collect(Collectors.toList());
        }

        // Filtro por campo específico en memoria
        if (q != null && !q.isBlank() && !"ALL".equals(field)) {
            String lq = q.trim().toLowerCase();
            items = items.stream().filter(it -> switch (field) {
                case "CLIENT"        -> matches(it.getClient().getFullName(), lq)
                                     || matches(it.getClient().getCompanyName(), lq);
                case "PROPERTY_CODE" -> matches(it.getProperty().getPropertyCode(), lq);
                case "MUNICIPALITY"  -> matches(it.getProperty().getMunicipality(), lq);
                case "CHANNEL"       -> it.getChannel() != null
                                     && it.getChannel().name().toLowerCase().contains(lq);
                case "COMMENTS"      -> matches(it.getComments(), lq);
                default              -> true;
            }).collect(Collectors.toList());
        }

        // Filtro NDA
        if (ndaOnly) {
            items = items.stream()
                    .filter(it -> Boolean.TRUE.equals(it.getNdaRequested()))
                    .collect(Collectors.toList());
        }

        List<Long> clientIds = items.stream()
                .map(i -> i.getClient().getId())
                .distinct()
                .toList();

        Map<Long, List<ClientPhone>> phonesByClientId;
        Map<Long, List<ClientEmail>> emailsByClientId;

        if (clientIds.isEmpty()) {
            phonesByClientId = Collections.emptyMap();
            emailsByClientId = Collections.emptyMap();
        } else {
            phonesByClientId = clientPhoneRepository
                    .findByClient_IdInOrderByClient_IdAscPositionAsc(clientIds)
                    .stream()
                    .collect(Collectors.groupingBy(p -> p.getClient().getId()));
            emailsByClientId = clientEmailRepository
                    .findByClient_IdInOrderByClient_IdAscPositionAsc(clientIds)
                    .stream()
                    .collect(Collectors.groupingBy(e -> e.getClient().getId()));
        }

        model.addAttribute("items", items);
        model.addAttribute("phonesByClientId", phonesByClientId);
        model.addAttribute("emailsByClientId", emailsByClientId);
        model.addAttribute("statuses", InterestStatus.values());
        model.addAttribute("channels", ContactChannel.values());
        model.addAttribute("selectedStatuses", statuses != null ? statuses : Collections.emptyList());
        model.addAttribute("selectedChannel", channel);
        model.addAttribute("q", q);
        model.addAttribute("searchField", field);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("ndaOnly", ndaOnly);
        return "interactions";
    }

    private boolean matches(String value, String lq) {
        return value != null && value.toLowerCase().contains(lq);
    }
}
