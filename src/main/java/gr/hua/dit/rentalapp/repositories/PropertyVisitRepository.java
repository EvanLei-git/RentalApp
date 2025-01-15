package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
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

    List<PropertyVisit> findByTenant_Username(String username);
}
