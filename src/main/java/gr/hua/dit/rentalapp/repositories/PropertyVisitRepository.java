package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PropertyVisitRepository extends JpaRepository<PropertyVisit, Long> {
    List<PropertyVisit> findByProperty_PropertyId(Long propertyId);

    List<PropertyVisit> findByProperty_PropertyIdAndVisitDateBetweenAndVisitStatusIn(Long propertyId, Date startDate, Date endDate, List<VisitStatus> statuses);

    List<PropertyVisit> findByTenant_Username(String username);

    List<PropertyVisit> findByVisitStatus(VisitStatus visitStatus);

    List<PropertyVisit> findByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(Long propertyId, String username, List<VisitStatus> statuses);

    boolean existsByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(Long propertyId, String username, List<VisitStatus> statuses);

    List<PropertyVisit> findByTenant_UsernameAndVisitStatusIn(String username, List<VisitStatus> statuses);

    List<PropertyVisit> findByProperty_PropertyIdAndTenant_UsernameAndVisitStatus(Long propertyId, String username, VisitStatus status);

    List<PropertyVisit> findByPropertyAndVisitStatusIn(Property property, List<VisitStatus> visitStatuses);
}
