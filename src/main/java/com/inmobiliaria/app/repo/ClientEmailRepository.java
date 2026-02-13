package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.ClientEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientEmailRepository extends JpaRepository<ClientEmail, Long> {

    List<ClientEmail> findByClient_IdInOrderByClient_IdAscPositionAsc(List<Long> clientIds);
}
