package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    Optional<Property> findByPropertyCode(String propertyCode);

    List<Property> findAllByOrderByPropertyCodeAsc();

    // ── Web pública: solo inmuebles publicados y no vendidos ──
    List<Property> findByPublicadoTrueAndSoldFalseOrderByIdDesc();

    // ── Filtro por tipo (para el catálogo público) ──
    List<Property> findByPublicadoTrueAndSoldFalseAndPropertyTypeOrderByIdDesc(String propertyType);

    // ── Tipos distintos publicados (para el filtro del catálogo) ──
    @Query("SELECT DISTINCT p.propertyType FROM Property p " +
           "WHERE p.publicado = true AND p.sold = false " +
           "AND p.propertyType IS NOT NULL " +
           "ORDER BY p.propertyType ASC")
    List<String> findTiposPublicados();
}
