package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.*;
import com.inmobiliaria.app.repo.ClientEmailRepository;
import com.inmobiliaria.app.repo.ClientPhoneRepository;
import com.inmobiliaria.app.repo.ClientPropertyInteractionRepository;
import com.inmobiliaria.app.repo.ClientRepository;
import com.inmobiliaria.app.web.view.ClientRowView;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
public class ClientController {

    private final ClientRepository clientRepository;
    private final ClientPhoneRepository clientPhoneRepository;
    private final ClientEmailRepository clientEmailRepository;
    private final ClientPropertyInteractionRepository interactionRepository;

    public ClientController(ClientRepository clientRepository,
                            ClientPhoneRepository clientPhoneRepository,
                            ClientEmailRepository clientEmailRepository,
                            ClientPropertyInteractionRepository interactionRepository) {
        this.clientRepository = clientRepository;
        this.clientPhoneRepository = clientPhoneRepository;
        this.clientEmailRepository = clientEmailRepository;
        this.interactionRepository = interactionRepository;
    }

    @GetMapping("/clientes")
    public String allClients(Model model,
                             @RequestParam(name = "q", required = false) String q,
                             @RequestParam(name = "type", required = false) String type,
                             @RequestParam(name = "from", required = false) LocalDate from,
                             @RequestParam(name = "to", required = false) LocalDate to,
                             HttpServletResponse response) {

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        List<Client> clients = clientRepository.findAllByOrderByFullNameAsc();

        // OJO: Stream.toList() devuelve lista inmutable (Java 16+). Si luego ordenas, peta.
        List<Long> ids = clients.stream()
                .map(Client::getId)
                .collect(Collectors.toList()); // mutable

        Map<Long, List<ClientPhone>> phonesByClientId = ids.isEmpty()
                ? Collections.emptyMap()
                : clientPhoneRepository.findByClient_IdInOrderByClient_IdAscPositionAsc(ids)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getClient().getId()));

        Map<Long, List<ClientEmail>> emailsByClientId = ids.isEmpty()
                ? Collections.emptyMap()
                : clientEmailRepository.findByClient_IdInOrderByClient_IdAscPositionAsc(ids)
                .stream()
                .collect(Collectors.groupingBy(e -> e.getClient().getId()));

        List<Long> lastInteractionIds = interactionRepository.findLastInteractionIdsPerClientByDateExact();

        // IMPORTANTE: que esta lista sea mutable porque la ordenas abajo.
        List<ClientPropertyInteraction> lastInteractions = lastInteractionIds.isEmpty()
                ? new ArrayList<>()
                : new ArrayList<>(interactionRepository.findByIdInWithClientAndProperty(lastInteractionIds));

        lastInteractions.sort(Comparator
                .comparing(ClientPropertyInteraction::getContactDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ClientPropertyInteraction::getId, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        Map<Long, ClientPropertyInteraction> lastByClientId = new HashMap<>();
        for (ClientPropertyInteraction i : lastInteractions) {
            if (i.getClient() != null && i.getClient().getId() != null) {
                lastByClientId.putIfAbsent(i.getClient().getId(), i);
            }
        }

        List<ClientRowView> rows = new ArrayList<>(clients.size());

        for (Client c : clients) {
            ClientPropertyInteraction li = lastByClientId.get(c.getId());

            ClientRowView r = new ClientRowView();
            r.setClientId(c.getId());
            r.setClientType(c.getClientType());
            r.setFullName(c.getFullName());
            r.setGeneralNotes(c.getGeneralNotes());

            List<ClientPhone> phs = phonesByClientId.getOrDefault(c.getId(), List.of());
            r.setPhones(phs.stream()
                    .map(ClientPhone::getPhoneNumber)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.toList())); // por si luego lo usas mutable

            List<ClientEmail> ems = emailsByClientId.getOrDefault(c.getId(), List.of());
            r.setEmails(ems.stream()
                    .map(ClientEmail::getEmail)
                    .filter(s -> s != null && !s.isBlank())
                    .collect(Collectors.toList()));

            // Solvia ahora pertenece al cliente (no a la interacción)
            r.setSolviaCode(c.getSolviaCode());

            if (li != null) {
                r.setLastContactDate(li.getContactDate());
                r.setChannel(li.getChannel());
                r.setStatus(li.getStatus());

                if (li.getProperty() != null) {
                    r.setLastPropertyType(li.getProperty().getPropertyType());
                    r.setLastPropertyCode(li.getProperty().getPropertyCode());
                    r.setLastPropertyAddress(li.getProperty().getAddress());
                    r.setLastPropertyMunicipality(li.getProperty().getMunicipality());
                }
            }

            rows.add(r);
        }

        rows.sort(Comparator
                .comparing(ClientRowView::getLastContactDate, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ClientRowView::getClientId, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        Predicate<ClientRowView> pred = rr -> true;

        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            try {
                ClientType wanted = ClientType.valueOf(type.toUpperCase(Locale.ROOT));
                pred = pred.and(rr -> rr.getClientType() == wanted);
            } catch (IllegalArgumentException ignore) {
            }
        }

        if (from != null) pred = pred.and(rr -> rr.getLastContactDate() != null && !rr.getLastContactDate().isBefore(from));
        if (to != null) pred = pred.and(rr -> rr.getLastContactDate() != null && !rr.getLastContactDate().isAfter(to));

        if (q != null && !q.isBlank()) {
            String qq = q.trim().toLowerCase(Locale.ROOT);
            String qqDigits = onlyDigits(qq);

            pred = pred.and(rr ->
                    contains(rr.getFullName(), qq) ||
                    contains(rr.getSolviaCode(), qq) ||
                    contains(rr.getGeneralNotes(), qq) ||

                    containsEnum(rr.getStatus(), qq) ||
                    containsEnum(rr.getChannel(), qq) ||
                    containsEnum(rr.getClientType(), qq) ||

                    contains(rr.getLastPropertyCode(), qq) ||
                    contains(rr.getLastPropertyType(), qq) ||
                    contains(rr.getLastPropertyAddress(), qq) ||
                    contains(rr.getLastPropertyMunicipality(), qq) ||

                    anyContains(rr.getPhones(), qq, qqDigits) ||
                    anyContains(rr.getEmails(), qq, null)
            );
        }

        // Si no vas a ordenar filtered, puede ser inmutable sin problema.
        List<ClientRowView> filtered = rows.stream().filter(pred).toList();

        model.addAttribute("rows", filtered);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("type", type == null ? "ALL" : type);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("clientTypes", ClientType.values());

        return "clients";
    }

    private boolean contains(String s, String q) {
        return s != null && s.toLowerCase(Locale.ROOT).contains(q);
    }

    private boolean containsEnum(Enum<?> e, String q) {
        if (e == null || q == null) return false;
        String name = e.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String raw = e.name().toLowerCase(Locale.ROOT);
        return raw.contains(q) || name.contains(q);
    }

    private boolean anyContains(List<String> list, String q, String qDigits) {
        if (list == null || list.isEmpty()) return false;
        for (String it : list) {
            if (it == null) continue;
            String low = it.toLowerCase(Locale.ROOT);
            if (low.contains(q)) return true;

            if (qDigits != null && !qDigits.isBlank()) {
                String digits = onlyDigits(it);
                if (digits.contains(qDigits)) return true;
            }
        }
        return false;
    }

    private String onlyDigits(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') b.append(ch);
        }
        return b.toString();
    }
}
