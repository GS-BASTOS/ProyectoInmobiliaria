// SupplierPropertyRepository.java
package com.inmobiliaria.app.repo;

import com.inmobiliaria.app.domain.SupplierProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SupplierPropertyRepository extends JpaRepository<SupplierProperty, Long> {

    @Query("SELECT sp FROM SupplierProperty sp JOIN FETCH sp.property WHERE sp.supplier.id = :supplierId")
    List<SupplierProperty> findBySupplierId(@Param("supplierId") Long supplierId);

    @Query("SELECT sp FROM SupplierProperty sp JOIN FETCH sp.supplier WHERE sp.property.id = :propertyId")
    List<SupplierProperty> findByPropertyId(@Param("propertyId") Long propertyId);
}
