package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.SupplierPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SupplierPhoneRepository extends JpaRepository<SupplierPhone, Long> {

    @Query("SELECT sp FROM SupplierPhone sp JOIN FETCH sp.supplier WHERE sp.phoneNumber = :phone")
    Optional<SupplierPhone> findFirstByPhoneNumber(@Param("phone") String phone);
}
