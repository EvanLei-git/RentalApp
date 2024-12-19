package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.RentalApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalApplicationRepository extends JpaRepository<RentalApplication, Long> {
    List<RentalApplication> findByApplicantId(Long applicantId);
    List<RentalApplication> findByPropertyId(Long propertyId);
}