package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyVisitRepository extends JpaRepository<PropertyVisit, Long> {
    List<PropertyVisit> findByPropertyId(Long propertyId);
    List<PropertyVisit> findByTenantId(Long tenantId);
    List<PropertyVisit> findByLandlordId(Long landlordId);
}