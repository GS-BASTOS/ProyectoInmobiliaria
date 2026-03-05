package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    @Query("""
        SELECT DISTINCT s FROM Supplier s
        LEFT JOIN FETCH s.phones
        LEFT JOIN FETCH s.emails
        LEFT JOIN FETCH s.supplierProperties
        WHERE LOWER(s.fullName)    LIKE LOWER(CONCAT('%',:q,'%'))
           OR LOWER(s.companyName) LIKE LOWER(CONCAT('%',:q,'%'))
        """)
    List<Supplier> searchAll(@Param("q") String q);

    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.phones WHERE s.id = :id")
    Optional<Supplier> findWithPhonesById(@Param("id") Long id);

    @Query("SELECT s FROM Supplier s LEFT JOIN FETCH s.emails WHERE s.id = :id")
    Optional<Supplier> findWithEmailsById(@Param("id") Long id);
}
