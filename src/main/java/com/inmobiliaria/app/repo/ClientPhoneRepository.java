package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.ClientPhone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientPhoneRepository extends JpaRepository<ClientPhone, Long> {

    Optional<ClientPhone> findFirstByPhoneNumber(String phoneNumber);

    List<ClientPhone> findByClient_IdInOrderByClient_IdAscPositionAsc(List<Long> clientIds);
}
