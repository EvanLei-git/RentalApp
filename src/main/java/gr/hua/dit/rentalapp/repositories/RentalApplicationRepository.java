package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalApplicationRepository extends JpaRepository<RentalApplication, Long> {

    // 'applicant' is of type Tenant, which has 'userId'
    List<RentalApplication> findByApplicantUserId(Long tenantId);

    // 'property' is of type Property, which has 'propertyId'
    List<RentalApplication> findByPropertyPropertyId(Long propertyId);

    // This works because 'status' is an enum field on RentalApplication
    List<RentalApplication> findByStatus(ApplicationStatus status);

    List<RentalApplication> findByApplicantUserIdAndStatus(Long tenantId, ApplicationStatus status);

    List<RentalApplication> findByPropertyPropertyIdAndStatus(Long propertyId, ApplicationStatus status);


}

