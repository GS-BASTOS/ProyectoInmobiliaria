package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.AgendaNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AgendaNoteRepository extends JpaRepository<AgendaNote, Long> {
    List<AgendaNote> findByUsernameOrderByNoteDateAsc(String username);
    List<AgendaNote> findByUsernameAndNoteDateOrderByNoteDateAsc(
            String username, java.time.LocalDate date);
}
