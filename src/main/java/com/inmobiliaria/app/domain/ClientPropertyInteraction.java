package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Entity
@Table(name = "client_property_interactions", indexes = {
        @Index(name = "idx_cpi_contact_date", columnList = "contactDate"),
        @Index(name = "idx_cpi_status", columnList = "status")
})
public class ClientPropertyInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Client client;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Property property;

    @NotNull
    @Column(nullable = false)
    private LocalDate contactDate;

    @Size(max = 60)
    @Column(length = 60)
    private String solviaCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContactChannel channel = ContactChannel.OTRO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private InterestStatus status = InterestStatus.VERDE_PENSANDO;

    @Size(max = 1200)
    @Column(length = 1200)
    private String comments;

    public Long getId() { return id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public LocalDate getContactDate() { return contactDate; }
    public void setContactDate(LocalDate contactDate) { this.contactDate = contactDate; }

    public String getSolviaCode() { return solviaCode; }
    public void setSolviaCode(String solviaCode) { this.solviaCode = solviaCode; }

    public ContactChannel getChannel() { return channel; }
    public void setChannel(ContactChannel channel) { this.channel = channel; }

    public InterestStatus getStatus() { return status; }
    public void setStatus(InterestStatus status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
