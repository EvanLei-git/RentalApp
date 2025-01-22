package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalApplicationRepository extends JpaRepository<RentalApplication, Long> {

    List<RentalApplication> findByApplicantUserId(Long tenantId);

    List<RentalApplication> findByPropertyPropertyId(Long propertyId);
    
    List<RentalApplication> findByPropertyAndStatusAndApplicationIdNot(Property property, ApplicationStatus status, Long applicationId);

}
