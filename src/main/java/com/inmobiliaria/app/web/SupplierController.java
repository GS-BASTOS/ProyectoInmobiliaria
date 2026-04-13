package com.inmobiliaria.app.web;

import com.inmobiliaria.app.domain.*;
import com.inmobiliaria.app.repo.*;
import com.inmobiliaria.app.web.dto.SupplierForm;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/proveedores")
public class SupplierController {

    private final SupplierRepository         supplierRepo;
    private final SupplierPhoneRepository    phoneRepo;
    private final SupplierEmailRepository    emailRepo;
    private final SupplierPropertyRepository supplierPropertyRepo;
    private final PropertyRepository         propertyRepo;

    public SupplierController(SupplierRepository supplierRepo,
                               SupplierPhoneRepository phoneRepo,
                               SupplierEmailRepository emailRepo,
                               SupplierPropertyRepository supplierPropertyRepo,
                               PropertyRepository propertyRepo) {
        this.supplierRepo         = supplierRepo;
        this.phoneRepo            = phoneRepo;
        this.emailRepo            = emailRepo;
        this.supplierPropertyRepo = supplierPropertyRepo;
        this.propertyRepo         = propertyRepo;
    }

    /* ══════════════════════════════════════════
       LISTA
    ══════════════════════════════════════════ */
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Supplier> suppliers = supplierRepo.searchAll(q == null ? "" : q);
        suppliers.sort(Comparator.comparing(Supplier::getFullName,
                       String.CASE_INSENSITIVE_ORDER));
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("q", q);
        return "proveedores";
    }

    /* ══════════════════════════════════════════
       FORMULARIO NUEVO
    ══════════════════════════════════════════ */
    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        SupplierForm form = new SupplierForm();
        form.setContactDate(LocalDate.now());
        model.addAttribute("form", form);
        return "proveedor-nuevo";
    }

    /* ══════════════════════════════════════════
       GUARDAR NUEVO
    ══════════════════════════════════════════ */
    @PostMapping("/nuevo")
    public String nuevoSave(@Valid @ModelAttribute("form") SupplierForm form,
                             BindingResult result,
                             Model model) {

        String p1 = normalizePhone(form.getPhone1());
        String p2 = normalizePhone(form.getPhone2());
        String p3 = normalizePhone(form.getPhone3());

        // ── Duplicados entre los campos del propio formulario ─────────
        if (!p1.isBlank() && !p2.isBlank() && p1.equals(p2))
            result.rejectValue("phone2", "dup", "Teléfono repetido.");
        if (!p1.isBlank() && !p3.isBlank() && p1.equals(p3))
            result.rejectValue("phone3", "dup", "Teléfono repetido.");
        if (!p2.isBlank() && !p3.isBlank() && p2.equals(p3))
            result.rejectValue("phone3", "dup", "Teléfono repetido.");

        // ── Duplicados en BD: guardar el proveedor existente si lo hay ─
        Supplier existingSupplier = null;
        if (!p1.isBlank()) existingSupplier = checkPhone(result, "phone1", p1, existingSupplier);
        if (!p2.isBlank()) existingSupplier = checkPhone(result, "phone2", p2, existingSupplier);
        if (!p3.isBlank()) existingSupplier = checkPhone(result, "phone3", p3, existingSupplier);

        if (result.hasErrors()) {
            if (existingSupplier != null) {
                model.addAttribute("existingSupplier", existingSupplier);
            }
            return "proveedor-nuevo";
        }

        // ── Crear proveedor ───────────────────────────────────────────
        Supplier supplier = new Supplier();
        supplier.setFullName(t(form.getFullName()));
        supplier.setCompanyName(t(form.getCompanyName()));
        supplier.setNotes(t(form.getNotes()));
        supplier.setContactDate(form.getContactDate() != null
                ? form.getContactDate() : LocalDate.now());

        try {
            supplier = supplierRepo.save(supplier);
        } catch (DataIntegrityViolationException ex) {
            result.reject("dbError", "Error al guardar el proveedor.");
            return "proveedor-nuevo";
        }

        // ── Teléfonos ─────────────────────────────────────────────────
        int pos = 1;
        for (String num : List.of(p1, p2, p3)) {
            if (!num.isBlank()) {
                SupplierPhone sp = new SupplierPhone();
                sp.setSupplier(supplier);
                sp.setPhoneNumber(num);
                sp.setPosition(pos++);
                try {
                    phoneRepo.save(sp);
                } catch (DataIntegrityViolationException ex) {
                    result.reject("dbError", "El teléfono " + num + " ya está registrado.");
                    supplierRepo.deleteById(supplier.getId());
                    return "proveedor-nuevo";
                }
            }
        }

        // ── Emails ────────────────────────────────────────────────────
        int epos = 1;
        for (String mail : List.of(t(form.getEmail1()), t(form.getEmail2()))) {
            if (!mail.isBlank()) {
                SupplierEmail se = new SupplierEmail();
                se.setSupplier(supplier);
                se.setEmail(mail);
                se.setPosition(epos++);
                emailRepo.save(se);
            }
        }

        // ── Inmueble (si se informó código) ───────────────────────────
        String code = t(form.getPropertyCode());
        if (!code.isBlank()) {
            Property property = propertyRepo.findByPropertyCode(code)
                    .orElseGet(() -> {
                        Property np = new Property();
                        np.setPropertyCode(code);
                        np.setPropertyType(t(form.getPropertyType()));
                        np.setAddress(t(form.getAddress()));
                        np.setMunicipality(t(form.getMunicipality()));
                        np.setProvince(t(form.getProvince()));
                        np.setDescription(t(form.getPropertyDescription()));
                        np.setNotes(t(form.getPropertyNotes()));
                        return np;
                    });

            // ✅ El precio se guarda en Property, no en el link
            if (form.getAskingPrice() != null) {
                property.setPrecio(form.getAskingPrice().intValue());
            }
            propertyRepo.save(property);

            SupplierProperty link = new SupplierProperty();
            link.setSupplier(supplier);
            link.setProperty(property);
            link.setLinkedDate(LocalDate.now());
            link.setNotes(t(form.getPropertyNotes()));
            supplierPropertyRepo.save(link);
        }

        return "redirect:/proveedores/" + supplier.getId();
    }

    /* ══════════════════════════════════════════
       FICHA DETALLE
    ══════════════════════════════════════════ */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Supplier supplier = supplierRepo.findWithPhonesById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        supplierRepo.findWithEmailsById(id)
                .ifPresent(s -> supplier.setEmails(s.getEmails()));

        List<SupplierProperty> links = supplierPropertyRepo.findBySupplierId(id);

        model.addAttribute("supplier", supplier);
        model.addAttribute("links",    links);
        return "proveedor-detail";
    }

    /* ══════════════════════════════════════════
       AÑADIR INMUEBLE A PROVEEDOR EXISTENTE
    ══════════════════════════════════════════ */
    @PostMapping("/{id}/inmuebles")
    public String addProperty(@PathVariable Long id,
                               @RequestParam String propertyCode,
                               @RequestParam(required = false) String propertyType,
                               @RequestParam(required = false) String address,
                               @RequestParam(required = false) String municipality,
                               @RequestParam(required = false) String province,
                               @RequestParam(required = false) String propertyNotes,
                               @RequestParam(required = false) BigDecimal askingPrice) {

        Supplier supplier = supplierRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String code = t(propertyCode);
        if (code.isBlank()) return "redirect:/proveedores/" + id;

        Property property = propertyRepo.findByPropertyCode(code)
                .orElseGet(() -> {
                    Property np = new Property();
                    np.setPropertyCode(code);
                    np.setPropertyType(t(propertyType));
                    np.setAddress(t(address));
                    np.setMunicipality(t(municipality));
                    np.setProvince(t(province));
                    np.setNotes(t(propertyNotes));
                    return np;
                });

        // ✅ El precio se guarda en Property, no en el link
        if (askingPrice != null) {
            property.setPrecio(askingPrice.intValue());
        }
        propertyRepo.save(property);

        SupplierProperty link = new SupplierProperty();
        link.setSupplier(supplier);
        link.setProperty(property);
        link.setLinkedDate(LocalDate.now());
        link.setNotes(t(propertyNotes));
        supplierPropertyRepo.save(link);

        return "redirect:/proveedores/" + id;
    }

    /* ══════════════════════════════════════════
       ELIMINAR VÍNCULO PROVEEDOR ↔ INMUEBLE
    ══════════════════════════════════════════ */
    @PostMapping("/{id}/inmuebles/{linkId}/eliminar")
    public String removeProperty(@PathVariable Long id,
                                  @PathVariable Long linkId) {
        supplierPropertyRepo.deleteById(linkId);
        return "redirect:/proveedores/" + id;
    }

    /* ══════════════════════════════════════════
       EDITAR PRECIO DE UN VÍNCULO
    ══════════════════════════════════════════ */
    @PostMapping("/{id}/inmuebles/{linkId}/precio")
    public String updatePrice(@PathVariable Long id,
                               @PathVariable Long linkId,
                               @RequestParam(required = false) BigDecimal askingPrice) {

        // ✅ Buscamos con eager del property para poder acceder a él
        supplierPropertyRepo.findById(linkId).ifPresent(link -> {
            Property property = propertyRepo.findById(link.getProperty().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            property.setPrecio(askingPrice != null ? askingPrice.intValue() : null);
            propertyRepo.save(property);
        });

        return "redirect:/proveedores/" + id;
    }

    /* ── Helpers ──────────────────────────────────────────────────────── */
    private Supplier checkPhone(BindingResult result, String field,
                                 String phone, Supplier existingSupplier) {
        return phoneRepo.findFirstByPhoneNumber(phone)
                .map(found -> {
                    Supplier owner = found.getSupplier();
                    result.rejectValue(field, "phoneExists",
                            "Este teléfono ya pertenece al proveedor: " + owner.getFullName());
                    return owner;
                })
                .orElse(existingSupplier);
    }

    private static String normalizePhone(String s) {
        if (s == null) return "";
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return "";
        boolean hasDdi = trimmed.startsWith("+");
        String digits  = trimmed.replaceAll("[^0-9]", "");
        return hasDdi ? "+" + digits : digits;
    }

    private static String t(String s) { return s == null ? "" : s.trim(); }
}