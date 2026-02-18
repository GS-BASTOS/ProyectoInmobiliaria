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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        // Convertir strings a enums ignorando valores inválidos
        List<InterestStatus> statusEnums = new ArrayList<>();
        if (statuses != null) {
            for (String s : statuses) {
                try { statusEnums.add(InterestStatus.valueOf(s)); }
                catch (IllegalArgumentException ignore) {}
            }
        }

        List<ClientPropertyInteraction> items;

        if (statusEnums.isEmpty()) {
            // Sin filtro de estado: todos los resultados
            items = interactionRepository.searchWithFilters(null, channel, q, from, to);
        } else if (statusEnums.size() == 1) {
            // Un solo estado: usa el método existente directamente
            items = interactionRepository.searchWithFilters(statusEnums.get(0), channel, q, from, to);
        } else {
            // Varios estados: filtramos en memoria
            items = interactionRepository.searchWithFilters(null, channel, q, from, to)
                    .stream()
                    .filter(i -> statusEnums.contains(i.getStatus()))
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
        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "interactions";
    }
}
