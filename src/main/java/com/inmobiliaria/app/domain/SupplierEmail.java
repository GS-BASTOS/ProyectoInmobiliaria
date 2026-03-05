package com.inmobiliaria.app.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier_emails")
public class SupplierEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false, length = 120)
    private String email;

    private Integer position;

    public Long getId()                          { return id; }
    public Supplier getSupplier()                { return supplier; }
    public void setSupplier(Supplier supplier)   { this.supplier = supplier; }
    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
    public Integer getPosition()                 { return position; }
    public void setPosition(Integer position)    { this.position = position; }
}
