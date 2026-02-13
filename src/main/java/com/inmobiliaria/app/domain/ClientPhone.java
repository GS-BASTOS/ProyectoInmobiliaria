package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "client_phones",
        uniqueConstraints = @UniqueConstraint(name = "uk_client_phone_number", columnNames = "phoneNumber"),
        indexes = @Index(name = "idx_client_phones_number", columnList = "phoneNumber"))
public class ClientPhone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String phoneNumber;

    @Column(nullable = false)
    private Integer position; // 1..3

    public Long getId() { return id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
