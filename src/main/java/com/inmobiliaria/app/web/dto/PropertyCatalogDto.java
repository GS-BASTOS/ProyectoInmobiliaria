package com.inmobiliaria.app.web.dto;

public class PropertyCatalogDto {
    private Long   id;
    private String propertyCode;
    private String propertyType;
    private String address;
    private String municipality;
    private boolean preVendido;
    private boolean sold;

    public PropertyCatalogDto(Long id, String propertyCode,
                               String propertyType, String address,
                               String municipality,
                               boolean preVendido, boolean sold) {
        this.id           = id;
        this.propertyCode = propertyCode;
        this.propertyType = propertyType;
        this.address      = address;
        this.municipality = municipality;
        this.preVendido   = preVendido;
        this.sold         = sold;
    }

    // Getters
    public Long    getId()           { return id; }
    public String  getPropertyCode() { return propertyCode; }
    public String  getPropertyType() { return propertyType; }
    public String  getAddress()      { return address; }
    public String  getMunicipality() { return municipality; }
    public boolean isPreVendido()    { return preVendido; }
    public boolean isSold()          { return sold; }
}
