package com.inmobiliaria.app.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "supplier_phones",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_supplier_phone_number",
           columnNames = "phone_number"))
public class SupplierPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "phone_number", nullable = false, length = 30)
    private String phoneNumber;

    private Integer position;

    public Long getId()                          { return id; }
    public Supplier getSupplier()                { return supplier; }
    public void setSupplier(Supplier supplier)   { this.supplier = supplier; }
    public String getPhoneNumber()               { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
    public Integer getPosition()                 { return position; }
    public void setPosition(Integer position)    { this.position = position; }
}
