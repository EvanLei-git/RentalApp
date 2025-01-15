package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PropertyVisitRepository extends JpaRepository<PropertyVisit, Long> {
    List<PropertyVisit> findByProperty_PropertyId(Long propertyId);

    PropertyVisit findByProperty_PropertyIdAndTenant_Username(Long propertyId, String username);

    boolean existsByProperty_PropertyIdAndTenant_Username(Long propertyId, String username);

    List<PropertyVisit> findByProperty_PropertyIdAndVisitDateBetween(Long propertyId, Date startDate, Date endDate);

    List<PropertyVisit> findByProperty_PropertyIdAndVisitDateBetweenAndVisitStatusNot(Long propertyId, Date startDate, Date endDate, VisitStatus status);

    List<PropertyVisit> findByProperty_PropertyIdAndVisitDateBetweenAndVisitStatusIn(Long propertyId, Date startDate, Date endDate, List<VisitStatus> statuses);

    PropertyVisit findByProperty_PropertyIdAndTenant_UsernameAndVisitStatusNot(Long propertyId, String username, VisitStatus status);

    PropertyVisit findFirstByProperty_PropertyIdAndTenant_UsernameAndVisitStatusNotOrderByVisitDateDesc(Long propertyId, String username, VisitStatus status);

    boolean existsByProperty_PropertyIdAndTenant_UsernameAndVisitStatusNot(Long propertyId, String username, VisitStatus status);

    List<PropertyVisit> findByTenant_Username(String username);

    List<PropertyVisit> findByTenant_UsernameAndVisitStatusNot(String username, VisitStatus status);

    List<PropertyVisit> findByProperty_PropertyIdAndVisitStatus(Long propertyId, VisitStatus visitStatus);

    List<PropertyVisit> findByProperty_PropertyIdAndVisitStatusNot(Long propertyId, VisitStatus visitStatus);

    List<PropertyVisit> findByVisitStatus(VisitStatus visitStatus);

    List<PropertyVisit> findByVisitStatusNot(VisitStatus visitStatus);

    List<PropertyVisit> findByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(Long propertyId, String username, List<VisitStatus> statuses);

    boolean existsByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(Long propertyId, String username, List<VisitStatus> statuses);

    List<PropertyVisit> findByTenant_UsernameAndVisitStatusIn(String username, List<VisitStatus> statuses);

    List<PropertyVisit> findByProperty_PropertyIdAndTenant_UsernameAndVisitStatus(Long propertyId, String username, VisitStatus status);
}
