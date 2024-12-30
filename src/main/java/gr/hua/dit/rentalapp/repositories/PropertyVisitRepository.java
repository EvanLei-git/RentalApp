package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PropertyVisitRepository extends JpaRepository<PropertyVisit, Long> {

    // If Property has field 'propertyId'
    List<PropertyVisit> findByPropertyPropertyId(Long propertyId);

    // If Tenant has field 'userId'
    List<PropertyVisit> findByTenantUserId(Long tenantId);

    // If Landlord has field 'userId'
    List<PropertyVisit> findByLandlordUserId(Long landlordId);

    // This one is okay because 'visitStatus' is a field on PropertyVisit
    List<PropertyVisit> findByVisitStatus(VisitStatus status);

    // Also okay because 'visitDate' is a field on PropertyVisit
    List<PropertyVisit> findByVisitDateBetween(Date startDate, Date endDate);

    // For combined queries
    List<PropertyVisit> findByPropertyPropertyIdAndVisitStatus(Long propertyId, VisitStatus status);

    List<PropertyVisit> findByTenantUserIdAndVisitStatus(Long tenantId, VisitStatus status);

    List<PropertyVisit> findByLandlordUserIdAndVisitStatus(Long landlordId, VisitStatus status);

    List<PropertyVisit> findByPropertyPropertyIdAndVisitDateBetween(Long propertyId, Date startDate, Date endDate);

   // List<PropertyVisit> findAllByPropertyId(@Param("propertyId") Long propertyId);
}
