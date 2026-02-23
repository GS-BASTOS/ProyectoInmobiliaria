package com.inmobiliaria.app.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "agenda_notes")
public class AgendaNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDate noteDate;

    @Column(nullable = false, length = 1000)
    private String content;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public LocalDate getNoteDate() { return noteDate; }
    public void setNoteDate(LocalDate noteDate) { this.noteDate = noteDate; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
