package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "client_emails",
        indexes = @Index(name = "idx_client_emails_value", columnList = "email"))
public class ClientEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @NotBlank
    @Email
    @Size(max = 140)
    @Column(nullable = false, length = 140)
    private String email;

    @Column(nullable = false)
    private Integer position; // 1..2

    public Long getId() { return id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
}
