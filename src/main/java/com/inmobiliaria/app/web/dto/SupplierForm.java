package com.inmobiliaria.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SupplierForm {

    // ── Datos del proveedor ───────────────────────
    private Long id;

    @NotBlank(message = "El nombre es obligatorio.")
    private String fullName;

    private String companyName;
    private String phone1;
    private String phone2;
    private String phone3;
    private String email1;
    private String email2;
    private LocalDate contactDate;
    private String notes;

    // ── Datos del inmueble (opcional al crear) ────
    private String propertyCode;
    private String propertyType;
    private String address;
    private String municipality;
    private String province;
    private String propertyDescription;
    private String propertyNotes;
    private BigDecimal askingPrice;

    // ── Getters y setters ─────────────────────────
    public Long getId()                              { return id; }
    public void setId(Long id)                       { this.id = id; }
    public String getFullName()                      { return fullName; }
    public void setFullName(String fullName)         { this.fullName = fullName; }
    public String getCompanyName()                   { return companyName; }
    public void setCompanyName(String v)             { this.companyName = v; }
    public String getPhone1()                        { return phone1; }
    public void setPhone1(String phone1)             { this.phone1 = phone1; }
    public String getPhone2()                        { return phone2; }
    public void setPhone2(String phone2)             { this.phone2 = phone2; }
    public String getPhone3()                        { return phone3; }
    public void setPhone3(String phone3)             { this.phone3 = phone3; }
    public String getEmail1()                        { return email1; }
    public void setEmail1(String email1)             { this.email1 = email1; }
    public String getEmail2()                        { return email2; }
    public void setEmail2(String email2)             { this.email2 = email2; }
    public LocalDate getContactDate()                { return contactDate; }
    public void setContactDate(LocalDate contactDate){ this.contactDate = contactDate; }
    public String getNotes()                         { return notes; }
    public void setNotes(String notes)               { this.notes = notes; }
    public String getPropertyCode()                  { return propertyCode; }
    public void setPropertyCode(String propertyCode) { this.propertyCode = propertyCode; }
    public String getPropertyType()                  { return propertyType; }
    public void setPropertyType(String propertyType) { this.propertyType = propertyType; }
    public String getAddress()                       { return address; }
    public void setAddress(String address)           { this.address = address; }
    public String getMunicipality()                  { return municipality; }
    public void setMunicipality(String municipality) { this.municipality = municipality; }
    public String getProvince()                      { return province; }
    public void setProvince(String province)         { this.province = province; }
    public String getPropertyNotes()                 { return propertyNotes; }
    public void setPropertyNotes(String propertyNotes){ this.propertyNotes = propertyNotes; }
    public BigDecimal getAskingPrice()               { return askingPrice; }
    public void setAskingPrice(BigDecimal askingPrice){ this.askingPrice = askingPrice; }
    public String getPropertyDescription()                    { return propertyDescription; }
    public void setPropertyDescription(String v)              { this.propertyDescription = v; }
}
