package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.Property;
import com.inmobiliaria.app.web.dto.PropertyCatalogDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // ── Catálogo combo: búsqueda AJAX ligera, solo campos necesarios ──
    @Query("SELECT new com.inmobiliaria.app.web.dto.PropertyCatalogDto(" +
           "p.id, p.propertyCode, p.propertyType, p.address, p.municipality, " +
           "p.preVendido, p.sold) " +
           "FROM Property p " +
           "WHERE p.sold = false " +
           "AND (LOWER(p.propertyCode) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(p.propertyType) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(p.municipality) LIKE LOWER(CONCAT('%',:q,'%')) " +
           "  OR LOWER(p.address)      LIKE LOWER(CONCAT('%',:q,'%'))) " +
           "ORDER BY p.propertyCode ASC")
    List<PropertyCatalogDto> searchCatalog(@Param("q") String query);
}