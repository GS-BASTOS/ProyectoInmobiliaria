package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.*;
import com.inmobiliaria.app.repo.ClientPhoneRepository;
import com.inmobiliaria.app.repo.ClientPropertyInteractionRepository;
import com.inmobiliaria.app.repo.ClientRepository;
import com.inmobiliaria.app.repo.PropertyRepository;
import com.inmobiliaria.app.repo.VisitRepository;
import com.inmobiliaria.app.web.dto.ClientEditForm;
import com.inmobiliaria.app.web.dto.NewInteractionForm;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class ClientDetailController {

    private final ClientRepository clientRepository;
    private final ClientPhoneRepository clientPhoneRepository;
    private final ClientPropertyInteractionRepository interactionRepository;
    private final VisitRepository visitRepository;
    private final PropertyRepository propertyRepository;

    public ClientDetailController(ClientRepository clientRepository,
                                  ClientPhoneRepository clientPhoneRepository,
                                  ClientPropertyInteractionRepository interactionRepository,
                                  VisitRepository visitRepository,
                                  PropertyRepository propertyRepository) {
        this.clientRepository = clientRepository;
        this.clientPhoneRepository = clientPhoneRepository;
        this.interactionRepository = interactionRepository;
        this.visitRepository = visitRepository;
        this.propertyRepository = propertyRepository;
    }

    @GetMapping("/clientes/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Client client = clientRepository.findWithPhonesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        clientRepository.findWithEmailsById(id).ifPresent(c2 -> client.setEmails(c2.getEmails()));

        ClientEditForm form = buildEditForm(client);

        List<ClientPropertyInteraction> interactions =
                interactionRepository.findByClientIdWithPropertyOrderByContactDateDesc(client.getId());

        NewInteractionForm ni = buildPrefilledInteractionForm(client, interactions);

        model.addAttribute("client", client);
        model.addAttribute("form", form);
        model.addAttribute("clientTypes", ClientType.values());

        model.addAttribute("newInteraction", ni);
        model.addAttribute("channels", ContactChannel.values());
        model.addAttribute("statuses", InterestStatus.values());
        model.addAttribute("interactions", interactions);

        model.addAttribute("visits",
                visitRepository.findByClient_IdOrderByVisitAtDescIdDesc(client.getId())
        );

        return "client_detail";
    }

    @PostMapping("/clientes/{id}/editar")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ClientEditForm form,
                         BindingResult br,
                         Model model) {

        Client client = clientRepository.findWithPhonesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        clientRepository.findWithEmailsById(id).ifPresent(c2 -> client.setEmails(c2.getEmails()));

        String p1 = t(form.getPhone1());
        String p2 = t(form.getPhone2());
        String p3 = t(form.getPhone3());

        if (!p2.isBlank() && p2.equals(p1)) br.rejectValue("phone2", "dup", "Teléfono repetido.");
        if (!p3.isBlank() && (p3.equals(p1) || p3.equals(p2))) br.rejectValue("phone3", "dup", "Teléfono repetido.");

        checkPhoneUnique(br, "phone1", p1, client.getId());
        if (!p2.isBlank()) checkPhoneUnique(br, "phone2", p2, client.getId());
        if (!p3.isBlank()) checkPhoneUnique(br, "phone3", p3, client.getId());

        if (br.hasErrors()) {
            repopulateDetailModel(model, client, form,
                    buildPrefilledInteractionForm(client,
                            interactionRepository.findByClientIdWithPropertyOrderByContactDateDesc(client.getId())));
            return "client_detail";
        }

        client.setClientType(form.getClientType());
        client.setFullName(form.getFullName());
        client.setGeneralNotes(form.getGeneralNotes());
        client.setSolviaCode(t(form.getSolviaCode()));

        upsertPhone(client, 1, p1);
        upsertPhone(client, 2, p2);
        upsertPhone(client, 3, p3);

        String e1 = t(form.getEmail1());
        String e2 = t(form.getEmail2());
        upsertEmail(client, 1, e1);
        upsertEmail(client, 2, e2);

        try {
            clientRepository.save(client);
        } catch (DataIntegrityViolationException ex) {
            br.reject("dbUnique", "No se pudo guardar: hay un teléfono repetido ya asignado en el sistema.");
            repopulateDetailModel(model, client, form,
                    buildPrefilledInteractionForm(client,
                            interactionRepository.findByClientIdWithPropertyOrderByContactDateDesc(client.getId())));
            return "client_detail";
        }

        return "redirect:/clientes/" + id;
    }

    @PostMapping("/clientes/{id}/interacciones")
    public String addInteraction(@PathVariable Long id,
                                 @Valid @ModelAttribute("newInteraction") NewInteractionForm form,
                                 BindingResult br,
                                 Model model) {

        Client client = clientRepository.findWithPhonesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        clientRepository.findWithEmailsById(id).ifPresent(c2 -> client.setEmails(c2.getEmails()));

        List<ClientPropertyInteraction> interactions =
                interactionRepository.findByClientIdWithPropertyOrderByContactDateDesc(client.getId());

        if (br.hasErrors()) {
            ClientEditForm editForm = buildEditForm(client);
            repopulateDetailModel(model, client, editForm, form);
            model.addAttribute("interactions", interactions);
            return "client_detail";
        }

        String code = t(form.getPropertyCode());

        Property property = propertyRepository.findByPropertyCode(code)
                .map(existing -> {
                    String pt = t(form.getPropertyType());
                    String addr = t(form.getAddress());
                    String mun = t(form.getMunicipality());

                    if (!pt.isBlank()) existing.setPropertyType(pt);
                    if (!addr.isBlank()) existing.setAddress(addr);
                    if (!mun.isBlank()) existing.setMunicipality(mun);

                    return propertyRepository.save(existing);
                })
                .orElseGet(() -> {
                    Property p = new Property();
                    p.setPropertyCode(code);
                    p.setPropertyType(t(form.getPropertyType()));
                    p.setAddress(t(form.getAddress()));
                    p.setMunicipality(t(form.getMunicipality()));
                    return propertyRepository.save(p);
                });

        ClientPropertyInteraction interaction = new ClientPropertyInteraction();
        interaction.setClient(client);
        interaction.setProperty(property);
        interaction.setContactDate(form.getContactDate());
        interaction.setChannel(form.getChannel());
        interaction.setStatus(form.getStatus());
        interaction.setSolviaCode(t(form.getSolviaCode())); // <-- IMPORTANTE
        interaction.setComments(t(form.getComments()));
        interactionRepository.save(interaction);

        return "redirect:/clientes/" + id;
    }

    private NewInteractionForm buildPrefilledInteractionForm(Client client, List<ClientPropertyInteraction> interactions) {
        NewInteractionForm ni = new NewInteractionForm();

        String phone1 = client.getPhones().stream()
                .filter(p -> p.getPosition() != null && p.getPosition() == 1)
                .map(ClientPhone::getPhoneNumber)
                .findFirst().orElse("");

        String email1 = client.getEmails().stream()
                .filter(e -> e.getPosition() != null && e.getPosition() == 1)
                .map(ClientEmail::getEmail)
                .findFirst().orElse("");

        ni.setPhone(phone1);
        ni.setEmail(email1);

        // Por defecto, usa el código Solvia del cliente (editable en el form si hace falta)
        ni.setSolviaCode(t(client.getSolviaCode()));

        if (interactions != null && !interactions.isEmpty()) {
            ClientPropertyInteraction last = interactions.get(0);

            if (last.getChannel() != null) ni.setChannel(last.getChannel());
            if (last.getStatus() != null) ni.setStatus(last.getStatus());

            if (last.getProperty() != null) {
                Property p = last.getProperty();
                if (p.getPropertyCode() != null && !p.getPropertyCode().isBlank()) ni.setPropertyCode(p.getPropertyCode());
                if (p.getPropertyType() != null && !p.getPropertyType().isBlank()) ni.setPropertyType(p.getPropertyType());
                if (p.getAddress() != null && !p.getAddress().isBlank()) ni.setAddress(p.getAddress());
                if (p.getMunicipality() != null && !p.getMunicipality().isBlank()) ni.setMunicipality(p.getMunicipality());
            }
        }

        return ni;
    }

    private ClientEditForm buildEditForm(Client client) {
        ClientEditForm form = new ClientEditForm();
        form.setId(client.getId());
        form.setClientType(client.getClientType());
        form.setFullName(client.getFullName());
        form.setGeneralNotes(client.getGeneralNotes());
        form.setSolviaCode(client.getSolviaCode());

        form.setPhone1(client.getPhones().stream().filter(p -> p.getPosition() == 1).map(ClientPhone::getPhoneNumber).findFirst().orElse(""));
        form.setPhone2(client.getPhones().stream().filter(p -> p.getPosition() == 2).map(ClientPhone::getPhoneNumber).findFirst().orElse(""));
        form.setPhone3(client.getPhones().stream().filter(p -> p.getPosition() == 3).map(ClientPhone::getPhoneNumber).findFirst().orElse(""));

        form.setEmail1(client.getEmails().stream().filter(e -> e.getPosition() == 1).map(ClientEmail::getEmail).findFirst().orElse(""));
        form.setEmail2(client.getEmails().stream().filter(e -> e.getPosition() == 2).map(ClientEmail::getEmail).findFirst().orElse(""));

        return form;
    }

    private void repopulateDetailModel(Model model,
                                       Client client,
                                       ClientEditForm editForm,
                                       NewInteractionForm newInteractionForm) {

        model.addAttribute("client", client);
        model.addAttribute("form", editForm);
        model.addAttribute("clientTypes", ClientType.values());

        model.addAttribute("newInteraction", newInteractionForm);
        model.addAttribute("channels", ContactChannel.values());
        model.addAttribute("statuses", InterestStatus.values());

        model.addAttribute("interactions",
                interactionRepository.findByClientIdWithPropertyOrderByContactDateDesc(client.getId())
        );

        model.addAttribute("visits",
                visitRepository.findByClient_IdOrderByVisitAtDescIdDesc(client.getId())
        );
    }

    private void upsertPhone(Client client, int position, String number) {
        String n = t(number);

        ClientPhone existing = client.getPhones().stream()
                .filter(p -> p.getPosition() != null && p.getPosition() == position)
                .findFirst()
                .orElse(null);

        if (n.isBlank()) {
            if (existing != null) client.getPhones().remove(existing);
            return;
        }

        if (existing == null) {
            ClientPhone p = new ClientPhone();
            p.setClient(client);
            p.setPosition(position);
            p.setPhoneNumber(n);
            client.getPhones().add(p);
        } else {
            existing.setPhoneNumber(n);
        }
    }

    private void upsertEmail(Client client, int position, String email) {
        String e = t(email);

        ClientEmail existing = client.getEmails().stream()
                .filter(x -> x.getPosition() != null && x.getPosition() == position)
                .findFirst()
                .orElse(null);

        if (e.isBlank()) {
            if (existing != null) client.getEmails().remove(existing);
            return;
        }

        if (existing == null) {
            ClientEmail ce = new ClientEmail();
            ce.setClient(client);
            ce.setPosition(position);
            ce.setEmail(e);
            client.getEmails().add(ce);
        } else {
            existing.setEmail(e);
        }
    }

    private void checkPhoneUnique(BindingResult br, String fieldName, String phone, Long editingClientId) {
        if (phone == null || phone.isBlank()) return;

        clientPhoneRepository.findFirstByPhoneNumber(phone).ifPresent(found -> {
            Long ownerId = found.getClient().getId();
            if (!ownerId.equals(editingClientId)) {
                br.rejectValue(fieldName, "phoneExists",
                        "Ese teléfono ya está asignado a otro cliente (ID " + ownerId + ").");
            }
        });
    }

    private static String t(String s) {
        return s == null ? "" : s.trim();
    }
}
