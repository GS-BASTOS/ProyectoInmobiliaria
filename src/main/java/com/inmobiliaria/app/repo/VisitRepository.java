package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.Visit;
import com.inmobiliaria.app.domain.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    List<Visit> findByStatusOrderByVisitAtAsc(VisitStatus status);

    List<Visit> findByClient_IdOrderByVisitAtDescIdDesc(Long clientId);
}
