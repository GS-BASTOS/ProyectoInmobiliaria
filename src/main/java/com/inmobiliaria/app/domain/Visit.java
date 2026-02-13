package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "visits", indexes = {
        @Index(name = "idx_visits_when", columnList = "visitAt"),
        @Index(name = "idx_visits_status", columnList = "status")
})
public class Visit {

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
    private LocalDateTime visitAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisitStatus status = VisitStatus.PROGRAMADA;

    @Size(max = 800)
    @Column(length = 800)
    private String notes;

    public Long getId() { return id; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public LocalDateTime getVisitAt() { return visitAt; }
    public void setVisitAt(LocalDateTime visitAt) { this.visitAt = visitAt; }

    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
