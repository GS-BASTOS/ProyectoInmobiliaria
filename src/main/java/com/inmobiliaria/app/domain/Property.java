package com.inmobiliaria.app.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "properties")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_code", nullable = false, unique = true, length = 60)
    private String propertyCode;

    @Column(name = "property_type", length = 80)
    private String propertyType;

    @Column(name = "address", length = 160)
    private String address;

    @Column(name = "municipality", length = 80)
    private String municipality;

    @Column(name = "notes", length = 300)
    private String notes;

    @Column(name = "sold")
    private boolean sold = false;

    @Column(name = "pre_vendido")
    private boolean preVendido = false;

    @Column(name = "province", length = 80)
    private String province;

    @Column(name = "occupied")
    private boolean occupied = false;

    @Column(name = "has_alarm")
    private boolean hasAlarm = false;

    @Column(name = "alarm_code", length = 60)
    private String alarmCode;

    @Column(name = "description", length = 1000)
    private String description;

    // ── Cliente que marcó pre-vendido ──────────────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pre_vendido_client_id")
    private Client preVendidoClient;

    // ── Cliente que compró (vendido definitivo) ────────────
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sold_client_id")
    private Client soldClient;

    // ── Getters y setters ──────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }

    public boolean isPreVendido() { return preVendido; }
    public void setPreVendido(boolean preVendido) { this.preVendido = preVendido; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    public boolean isHasAlarm() { return hasAlarm; }
    public void setHasAlarm(boolean hasAlarm) { this.hasAlarm = hasAlarm; }

    public String getAlarmCode() { return alarmCode; }
    public void setAlarmCode(String alarmCode) { this.alarmCode = alarmCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Client getPreVendidoClient() { return preVendidoClient; }
    public void setPreVendidoClient(Client preVendidoClient) { this.preVendidoClient = preVendidoClient; }

    public Client getSoldClient() { return soldClient; }
    public void setSoldClient(Client soldClient) { this.soldClient = soldClient; }
}
