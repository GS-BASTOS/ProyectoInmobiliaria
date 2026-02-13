package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    Optional<Property> findByPropertyCode(String propertyCode);
}
