package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "supplier_properties")
public class SupplierProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(precision = 12, scale = 2)
    private BigDecimal askingPrice;

    @Column(length = 500)
    private String notes;

    private LocalDate linkedDate;

    // ── Getters y setters ──────────────────────────
    public Long getId()                              { return id; }
    public Supplier getSupplier()                    { return supplier; }
    public void setSupplier(Supplier supplier)       { this.supplier = supplier; }
    public Property getProperty()                    { return property; }
    public void setProperty(Property property)       { this.property = property; }
    public BigDecimal getAskingPrice()               { return askingPrice; }
    public void setAskingPrice(BigDecimal askingPrice){ this.askingPrice = askingPrice; }
    public String getNotes()                         { return notes; }
    public void setNotes(String notes)               { this.notes = notes; }
    public LocalDate getLinkedDate()                 { return linkedDate; }
    public void setLinkedDate(LocalDate linkedDate)  { this.linkedDate = linkedDate; }
}
