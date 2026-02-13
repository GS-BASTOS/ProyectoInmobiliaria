package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "properties", indexes = {
        @Index(name = "idx_properties_code", columnList = "propertyCode"),
        @Index(name = "idx_properties_municipality", columnList = "municipality")
})
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60, unique = true)
    private String propertyCode;

    @Size(max = 80)
    @Column(length = 80)
    private String propertyType;

    @Size(max = 160)
    @Column(length = 160)
    private String address;

    @Size(max = 80)
    @Column(length = 80)
    private String municipality;

    @Size(max = 300)
    @Column(length = 300)
    private String notes;

    public Long getId() { return id; }

    public String getPropertyCode() { return propertyCode; }
    public void setPropertyCode(String propertyCode) { this.propertyCode = propertyCode; }

    public String getPropertyType() { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMunicipality() { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
