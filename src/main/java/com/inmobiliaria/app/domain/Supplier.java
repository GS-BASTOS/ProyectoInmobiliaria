package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "suppliers")
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "company_name")
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "contact_date")
    private LocalDate contactDate;

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<SupplierPhone> phones = new LinkedHashSet<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private Set<SupplierEmail> emails = new LinkedHashSet<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SupplierProperty> supplierProperties = new LinkedHashSet<>();

    // ── Getters / Setters ─────────────────────────
    public Long getId()                                      { return id; }
    public void setId(Long id)                               { this.id = id; }

    public String getFullName()                              { return fullName; }
    public void setFullName(String fullName)                 { this.fullName = fullName; }

    public String getCompanyName()                           { return companyName; }
    public void setCompanyName(String companyName)           { this.companyName = companyName; }

    public String getNotes()                                 { return notes; }
    public void setNotes(String notes)                       { this.notes = notes; }

    public LocalDate getContactDate()                        { return contactDate; }
    public void setContactDate(LocalDate contactDate)        { this.contactDate = contactDate; }

    public Set<SupplierPhone> getPhones()                    { return phones; }
    public void setPhones(Set<SupplierPhone> phones)         { this.phones = phones; }

    public Set<SupplierEmail> getEmails()                    { return emails; }
    public void setEmails(Set<SupplierEmail> emails)         { this.emails = emails; }

    public Set<SupplierProperty> getSupplierProperties()     { return supplierProperties; }
    public void setSupplierProperties(Set<SupplierProperty> sp) { this.supplierProperties = sp; }
}
