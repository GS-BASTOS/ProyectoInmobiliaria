package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.Client;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @EntityGraph(attributePaths = {"phones"})
    List<Client> findAllByOrderByFullNameAsc();

    @EntityGraph(attributePaths = {"phones"})
    Optional<Client> findWithPhonesById(Long id);

    @EntityGraph(attributePaths = {"emails"})
    Optional<Client> findWithEmailsById(Long id);
}
