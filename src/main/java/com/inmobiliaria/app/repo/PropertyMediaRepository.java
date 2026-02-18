package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.PropertyMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PropertyMediaRepository extends JpaRepository<PropertyMedia, Long> {
    List<PropertyMedia> findByPropertyIdOrderByIdAsc(Long propertyId);
    void deleteByPropertyId(Long propertyId);
}
