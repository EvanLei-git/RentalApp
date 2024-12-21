package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PropertyVisitRepository extends JpaRepository<PropertyVisit, Long> {
    List<PropertyVisit> findByPropertyId(Long propertyId);
    List<PropertyVisit> findByTenantId(Long tenantId);
    List<PropertyVisit> findByLandlordId(Long landlordId);
    List<PropertyVisit> findByVisitStatus(VisitStatus status);
    List<PropertyVisit> findByVisitDateBetween(Date startDate, Date endDate);
    List<PropertyVisit> findByPropertyIdAndVisitStatus(Long propertyId, VisitStatus status);
    List<PropertyVisit> findByTenantIdAndVisitStatus(Long tenantId, VisitStatus status);
    List<PropertyVisit> findByLandlordIdAndVisitStatus(Long landlordId, VisitStatus status);
    List<PropertyVisit> findByPropertyIdAndVisitDateBetween(Long propertyId, Date startDate, Date endDate);
}